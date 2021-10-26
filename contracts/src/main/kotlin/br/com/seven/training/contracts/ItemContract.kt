package br.com.seven.training.contracts

import br.com.seven.training.state.ItemOwnershipState
import br.com.seven.training.state.ItemState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

class ItemContract: Contract {
    override fun verify(tx: LedgerTransaction) = requireThat {
        val command = tx.commands.requireSingleCommand<Commands>()

        when(command.value) {
            is Commands.Create -> {
                "Não devem existir entradas na transação de criação" using (tx.inputs.isEmpty())
                val itemOutputs = tx.outputsOfType<ItemState>()
                "Deve ser criado um item por transação" using (itemOutputs.size == 1)
                val itemOutput = itemOutputs.single()
                "Deve existir exatamente um anexo na transação" using (tx.attachments.filter { it !is ContractAttachment}.size == 1)
                "A Hash do anexo deve ser a mesma que está no state de saída" using (itemOutput.itemHash == tx.attachments.single { it !is ContractAttachment }.id)
                "O número de assinantes deve ser o mesmo que o número de participantes" using (command.signers.containsAll(itemOutput.participants.map { it.owningKey }))
                val itemOwnershipOutputs = tx.outputsOfType<ItemOwnershipState>()
                "Deve ser fornecido um state de histórico do  item" using (itemOwnershipOutputs.size == 1)
                val itemOwnershipOutput = itemOwnershipOutputs.single()
                "O dono no histórico deve ser o mesmo que o dono no item final" using (itemOwnershipOutput.owner == itemOutput.owner)
                "O external ID do histórico deve ser a hash do anexo" using (itemOwnershipOutput.linearId.externalId == itemOutput.itemHash.toString())
                "O número de assinantes deve ser o mesmo que o número de participantes" using (command.signers.containsAll(itemOwnershipOutput.participants.map { it.owningKey }))
            }
            is Commands.Transfer -> {
                val itemInputs = tx.inputsOfType<ItemState>()
                "Ao efetuar uma transferência deve ser utilizado um item como entrada" using (itemInputs.size == 1)
                val itemInput = itemInputs.single()
                "Deve existir exatamente um anexo na transação" using (tx.attachments.filter { it !is ContractAttachment}.size == 1)
                "A Hash do anexo deve ser a mesma que está no state de entrada" using (itemInput.itemHash == tx.attachments.single { it !is ContractAttachment }.id)
                val itemOwnershipInputs = tx.inputsOfType<ItemOwnershipState>()
                "Ao efetuar uma transferência deve ser utilizado um state de histórico item como entrada" using (itemOwnershipInputs.size == 1)
                val itemOwnershipInput = itemOwnershipInputs.single()
                "O dono no histórico deve ser o mesmo que o dono no item final na entrada" using (itemOwnershipInput.owner == itemInput.owner)
                "O external ID do histórico de entrada deve ser a hash do anexo" using (itemOwnershipInput.linearId.externalId == itemInput.itemHash.toString())

                val itemOutputs = tx.outputsOfType<ItemState>()
                "Deve ser criado um item por transação" using (itemOutputs.size == 1)
                val itemOutput = itemOutputs.single()
                "Deve existir exatamente um anexo na transação" using (tx.attachments.filter { it !is ContractAttachment}.size == 1)
                "A Hash do anexo deve ser a mesma que está no state de saída" using (itemOutput.itemHash == tx.attachments.single { it !is ContractAttachment }.id)
                val itemOwnershipOutputs = tx.outputsOfType<ItemOwnershipState>()
                "Deve ser fornecido um state de histórico do  item" using (itemOwnershipOutputs.size == 1)
                val itemOwnershipOutput = itemOwnershipOutputs.single()
                "O dono no histórico deve ser o mesmo que o dono no item final" using (itemOwnershipOutput.owner == itemOutput.owner)
                "O external ID do histórico deve ser a hash do anexo" using (itemOwnershipOutput.linearId.externalId == itemOutput.itemHash.toString())
            }
        }
    }

    interface Commands: CommandData {
        class Create: Commands
        class Transfer: Commands
    }
}