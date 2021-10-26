package br.com.seven.training.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

class ProposalContract: Contract {

    override fun verify(tx: LedgerTransaction) {}

    interface Commands : CommandData {
        class Create : Commands
        class Accept: Commands
        class Refuse: Commands
        class Pay: Commands
    }
}