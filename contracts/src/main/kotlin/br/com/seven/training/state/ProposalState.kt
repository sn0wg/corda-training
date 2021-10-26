package br.com.seven.training.state

import br.com.seven.training.contracts.ProposalContract
import br.com.seven.training.models.ProposalStatus
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.serialization.CordaSerializable

@BelongsToContract(ProposalContract::class)
@CordaSerializable
data class ProposalState(
        val value: Long,
        val itemID: UniqueIdentifier,
        val buyer: AbstractParty,
        val seller: AbstractParty,
        val status: ProposalStatus = ProposalStatus.CREATED,
        override val linearId: UniqueIdentifier = UniqueIdentifier(),
        override val participants: List<AbstractParty> = listOf(buyer, seller)) : LinearState