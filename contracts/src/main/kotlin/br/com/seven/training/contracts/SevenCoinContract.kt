package br.com.seven.training.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.transactions.LedgerTransaction

class SevenCoinContract: Contract {

    override fun verify(tx: LedgerTransaction) {}

    interface Commands : CommandData {
        class Enroll : Commands
        class Transfer: Commands
    }
}