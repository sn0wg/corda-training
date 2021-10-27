package br.com.seven.training.controller

import br.com.seven.training.corda.NodeRPCConnection
import br.com.seven.training.flows.item.ItemCreationFlow
import br.com.seven.training.state.ItemOwnershipState
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("item")
class ItemController {
    @Autowired
    private lateinit var nodeRPCConnection: NodeRPCConnection

    @PostMapping(consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun createItem(@RequestParam("hash", required = true) hash: String): SignedTransaction {
        return this.nodeRPCConnection.proxy.startTrackedFlowDynamic(ItemCreationFlow.Initiator::class.java, hash).returnValue.getOrThrow()
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun getItens(): List<ItemOwnershipState> {
        return this.nodeRPCConnection.proxy.vaultQueryBy<ItemOwnershipState>().states.map { it.state.data }
    }

    @GetMapping("/{ID}", produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun getHistoric(@PathVariable("ID", required = true) id: String): List<ItemOwnershipState> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(status = Vault.StateStatus.ALL).withExternalId(listOf(id))
        return this.nodeRPCConnection.proxy.vaultQueryBy<ItemOwnershipState>(criteria).states.map { it.state.data }
    }
}