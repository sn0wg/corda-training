package br.com.seven.training.controller

import br.com.seven.training.corda.NodeRPCConnection
import br.com.seven.training.state.SevenCoinState
import net.corda.core.internal.sumByLong
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import net.corda.core.messaging.vaultQueryBy

@RestController
@RequestMapping("wallet")
class SevenCoinController {
    @Autowired
    private lateinit var nodeRPCConnection: NodeRPCConnection

    @GetMapping
    fun getWalletBalance(): Long {
        return this.nodeRPCConnection.proxy.vaultQueryBy<SevenCoinState>().states.map { it.state.data.amount }.sumByLong { it.quantity }
    }
}