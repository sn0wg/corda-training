package br.com.seven.training.flows.proposal

import br.com.seven.training.contracts.ProposalContract
import br.com.seven.training.state.ItemOwnershipState
import br.com.seven.training.state.ProposalState
import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.*
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.*
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.lang.Exception

object ProposalCreationFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(
            private val hash: String,
            private val value: Long
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
            val criteria = QueryCriteria.LinearStateQueryCriteria().withExternalId(listOf(hash))
            val responseList = serviceHub.vaultService.queryBy(ItemOwnershipState::class.java, criteria)

            if(responseList.states.isEmpty())
                throw Exception("Não foi possível encontrar o item com hash $hash")

            val secureHash = SecureHash.parse(hash)

            if(!serviceHub.attachments.hasAttachment(secureHash))
                throw Exception("Anexo ID $secureHash não foi encontrado")

            progressTracker.currentStep = GENERATING_TRANSACTION
            val itemState = responseList.states.single()
            val me = serviceHub.myInfo.legalIdentities.single()
            val proposalState = ProposalState(value, itemState.state.data.linearId, me, itemState.state.data.owner)

            val reference = ReferencedStateAndRef(StateAndRef(itemState.state, itemState.ref))
            val txCommand = Command(ProposalContract.Commands.Create(), proposalState.participants.map { it.owningKey })
            val txBuilder = TransactionBuilder(notary)
                    .addOutputState(proposalState)
                    .addCommand(txCommand)
                    .addAttachment(secureHash)
                    .addReferenceState(reference)

            progressTracker.currentStep = VERIFYING_TRANSACTION
            txBuilder.verify(serviceHub)

            progressTracker.currentStep = SIGNING_TRANSACTION
            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

            progressTracker.currentStep = GATHERING_SIGS
            val sessions = initiateFlow(itemState.state.data.owner)
            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, setOf(sessions), GATHERING_SIGS.childProgressTracker()))

            progressTracker.currentStep = FINALISING_TRANSACTION
            return subFlow(FinalityFlow(fullySignedTx, setOf(sessions), FINALISING_TRANSACTION.childProgressTracker()))
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
}