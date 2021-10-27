package br.com.seven.training.controller

import br.com.seven.training.corda.NodeRPCConnection
import br.com.seven.training.flows.proposal.ProposalCreationFlow
import br.com.seven.training.flows.proposal.ProposalResponseFlow
import br.com.seven.training.flows.proposal.ProposalStatePayFlow
import br.com.seven.training.models.ProposalResponse
import br.com.seven.training.state.ProposalState
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import net.corda.core.messaging.vaultQueryBy

@RestController
@RequestMapping("proposal")
class ProposalController {
    @Autowired
    private lateinit var nodeRPCConnection: NodeRPCConnection

    @PostMapping(consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun createProposal(@RequestParam("hash", required = true) hash: String, @RequestParam("value", required = true) value: Long): SignedTransaction {
        return this.nodeRPCConnection.proxy.startTrackedFlowDynamic(ProposalCreationFlow.Initiator::class.java, hash, value).returnValue.getOrThrow()
    }

    @GetMapping("/from",consumes = [], produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun getProposal(): List<ProposalState> {
        return this.nodeRPCConnection.proxy.vaultQueryBy<ProposalState>().states
                .map { it.state.data }
                .filter { it.buyer == nodeRPCConnection.proxy.nodeInfo().legalIdentities.first() }
    }

    @GetMapping("/for",consumes = [], produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun getProposalForMe(): List<ProposalState> {
        return this.nodeRPCConnection.proxy.vaultQueryBy<ProposalState>().states
                .map { it.state.data }
                .filter { it.seller == nodeRPCConnection.proxy.nodeInfo().legalIdentities.first() }
    }

    @PostMapping("/{ID}/answer", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun answer(@PathVariable("ID", required = true) id: String, @RequestParam("RESPONSE", required = true) responseString: String): SignedTransaction {
        val response = ProposalResponse.valueOf(responseString)
        return this.nodeRPCConnection.proxy.startTrackedFlowDynamic(ProposalResponseFlow.Initiator::class.java, id, response).returnValue.getOrThrow()
    }

    @PostMapping("/{ID}/pay", consumes = [])
    fun pay(@PathVariable("ID", required = true) id: String): SignedTransaction {
        return this.nodeRPCConnection.proxy.startTrackedFlowDynamic(ProposalStatePayFlow.Initiator::class.java, id).returnValue.getOrThrow()
    }
}