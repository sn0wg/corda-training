package br.com.seven.training.flows.proposal

import br.com.seven.training.contracts.ProposalContract
import br.com.seven.training.contracts.SevenCoinContract
import br.com.seven.training.models.ProposalStatus
import br.com.seven.training.models.SevenCoin
import br.com.seven.training.state.ItemOwnershipState
import br.com.seven.training.state.ItemState
import br.com.seven.training.state.ProposalState
import br.com.seven.training.state.SevenCoinState
import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.*
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.*
import net.corda.core.identity.AbstractParty
import net.corda.core.internal.sumByLong
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.lang.Exception
import net.corda.core.utilities.unwrap
import java.math.BigDecimal
import java.util.*

object ProposalStatePayFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(
            private val proposalID: String
    ): FlowLogic<SignedTransaction>() {
        companion object {
            object GENERATING_TRANSACTION : ProgressTracker.Step("Generating a new transaction.")
            object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
            object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
            object GATHERING_SIGS : ProgressTracker.Step("Gathering the counterparty's signature.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }

            object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                    GENERATING_TRANSACTION,
                    VERIFYING_TRANSACTION,
                    SIGNING_TRANSACTION,
                    GATHERING_SIGS,
                    FINALISING_TRANSACTION
            )
        }

        override val progressTracker = tracker()

        @Suspendable
        override fun call(): SignedTransaction {
            val notary = serviceHub.networkMapCache.notaryIdentities.single()
            val me = serviceHub.myInfo.legalIdentities.single()
            val uuid = UUID.fromString(proposalID)
            val criteria = QueryCriteria.LinearStateQueryCriteria().withUuid(listOf(uuid))
            val responseList = serviceHub.vaultService.queryBy(ProposalState::class.java, criteria)

            if(responseList.states.isEmpty())
                throw Exception("Não foi possível encontrar a proposta $proposalID")

            progressTracker.currentStep = GENERATING_TRANSACTION
            val proposalStateAndRef = responseList.states.single()
            val proposalState = proposalStateAndRef.state.data.copy(status = ProposalStatus.PAID)

            val itemOwnershipCriteria = QueryCriteria.LinearStateQueryCriteria().withExternalId(listOf(proposalState.itemID.externalId!!))
            val itemOwnershipStateList = serviceHub.vaultService.queryBy(ItemOwnershipState::class.java, itemOwnershipCriteria)
            if(itemOwnershipStateList.states.isEmpty())
                throw Exception("Não foi possível encontrar o histórico do item ${proposalState.itemID.externalId}")
            val itemOwnershipStateAndRef = itemOwnershipStateList.states.single();
            val itemOwnershipState = itemOwnershipStateAndRef.state.data.copy(owner = proposalState.buyer)

            val itemStateAndRef = subFlow(GetItemStateAndRef(proposalState.seller, proposalState.itemID.externalId!!))
            val itemCommandAndState = itemStateAndRef.state.data.withNewOwner(me)

            val fungibleStatesAndRefs = serviceHub.vaultService.queryBy(SevenCoinState::class.java)
            val total = fungibleStatesAndRefs.states.map { it.state.data }.sumByLong { it.amount.quantity }
            val outputBalance = total - proposalState.value

            val outputBalanceState = fungibleStatesAndRefs.states.first()
                    .state.data.copy(amount = Amount.fromDecimal(BigDecimal.valueOf(outputBalance), SevenCoin()))

            val issuer = outputBalanceState.participants.filter { it != me }.single()

            val paymentState = SevenCoinState(
                    proposalState.seller,
                    Amount.fromDecimal(BigDecimal.valueOf(proposalState.value), SevenCoin()),
                    listOf(proposalState.seller, issuer))

            val proposalCommand = Command(ProposalContract.Commands.Pay(), proposalState.participants.map { it.owningKey })
            val transferCommand = Command(SevenCoinContract.Commands.Transfer(), listOf(me, issuer, proposalState.seller).map { it.owningKey })

            val secureHash = SecureHash.parse(proposalState.itemID.externalId!!)

            val txBuilder = TransactionBuilder(notary)
                    .addInputState(proposalStateAndRef)
                    .addInputState(itemOwnershipStateAndRef)
                    .addInputState(itemStateAndRef)
                    .addOutputState(proposalState)
                    .addOutputState(itemOwnershipState)
                    .addOutputState(itemCommandAndState.ownableState)
                    .addOutputState(outputBalanceState)
                    .addOutputState(paymentState)
                    .addCommand(proposalCommand)
                    .addCommand(transferCommand)
                    .addCommand(itemCommandAndState.command, proposalState.participants.map { it.owningKey })
                    .addAttachment(secureHash)

            fungibleStatesAndRefs.states.forEach { txBuilder.addInputState(it) }


            progressTracker.currentStep = VERIFYING_TRANSACTION
            txBuilder.verify(serviceHub)

            progressTracker.currentStep = SIGNING_TRANSACTION
            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

            progressTracker.currentStep = GATHERING_SIGS
            val sessions = setOf(initiateFlow(proposalState.seller), initiateFlow(issuer))
            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, sessions, GATHERING_SIGS.childProgressTracker()))

            progressTracker.currentStep = FINALISING_TRANSACTION
            return subFlow(FinalityFlow(fullySignedTx, sessions, FINALISING_TRANSACTION.childProgressTracker()))
        }
    }

    @InitiatedBy(Initiator::class)
    class Acceptor(val otherPartySession: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartySession) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                }
            }
            val txId = subFlow(signTransactionFlow).id

            return subFlow(ReceiveFinalityFlow(otherPartySession, expectedTxId = txId))
        }
    }

    @InitiatingFlow
    class GetItemStateAndRef(private val party: AbstractParty, private val hash: String): FlowLogic<StateAndRef<ItemState>>() {
        @Suspendable
        override fun call(): StateAndRef<ItemState> {
            val ownerRequest = OwnerRequest(hash)
            return initiateFlow(party).sendAndReceive<StateAndRef<ItemState>>(ownerRequest).unwrap { it}
        }
    }

    @InitiatedBy(GetItemStateAndRef::class)
    class GetMarginResponder(private val session: FlowSession): FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            val ownerRequest = session.receive<OwnerRequest>().unwrap { it }
            val me = serviceHub.myInfo.legalIdentities.single()
//            val criteria = QueryCriteria.FungibleAssetQueryCriteria().withOwner(listOf(me))
            val itemStateAndRefList = serviceHub.vaultService.queryBy(ItemState::class.java).states.filter { it.state.data.itemHash.toString() == ownerRequest.hash && it.state.data.owner == me}

            if(itemStateAndRefList.isEmpty())
                throw Exception("Não foi possivel encontrar o item com ID ${ownerRequest.hash}")

            session.send(itemStateAndRefList.single())

        }

    }

    @CordaSerializable
    data class OwnerRequest(
            val hash: String
    )
}