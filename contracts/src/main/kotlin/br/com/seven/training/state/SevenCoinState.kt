package br.com.seven.training.state

import br.com.seven.training.contracts.SevenCoinContract
import br.com.seven.training.models.SevenCoin
import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.FungibleState
import net.corda.core.identity.AbstractParty
import net.corda.core.serialization.CordaSerializable

@BelongsToContract(SevenCoinContract::class)
@CordaSerializable
data class SevenCoinState(
        val wallet: AbstractParty,
        override val amount: Amount<SevenCoin>,
        override val participants: List<AbstractParty>) : FungibleState<SevenCoin>

