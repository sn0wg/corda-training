package br.com.seven.training.corda

import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCConnection
import net.corda.client.rpc.GracefulReconnect
import net.corda.core.crypto.SecureHash
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.loggerFor
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.InputStream
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class NodeRPCConnection(
        @Value("\${config.rpc.host}") private val host: String,
        @Value("\${config.rpc.username}") private val username: String,
        @Value("\${config.rpc.password}") private val password: String,
        @Value("\${config.rpc.port}") private val rpcPort: String) : AutoCloseable {

    companion object {
        val logger = loggerFor<NodeRPCConnection>()
    }

    lateinit var rpcConnection: CordaRPCConnection
    lateinit var proxy: CordaRPCOps

    @PostConstruct
    fun initialiseNodeRPCConnection() {
        println("Trying to connect on node $host:$rpcPort with username $username and password $password")
        val gracefulReconnect = GracefulReconnect(
                onDisconnect = {
                    disconnectedRPC()
                },
                onReconnect = {
                    reconnectedRPC()
                }, maxAttempts = 100)
        val rpcAddress = NetworkHostAndPort.parse("$host:$rpcPort")
        val rpcClient = CordaRPCClient(rpcAddress)
        val rpcConnection = rpcClient.start(username, password, gracefulReconnect = gracefulReconnect)
        proxy = rpcConnection.proxy
        println(proxy.networkParameters)

        logger.info(proxy.currentNodeTime().toString())
    }

    @PreDestroy
    override fun close() {
        rpcConnection.notifyServerAndClose()
    }

    fun disconnectedRPC() {
        println("Connection RPC was disconnected in SERVER. :(")
    }

    fun reconnectedRPC() {
        println("Connection RPC was reconnected in SERVER. :)")
    }

    fun saveAttachment(attachment: Attachment): SecureHash {
        return attachment.use { att ->
            att.inputStream.use {
                this.proxy.uploadAttachment(it)
            }
        }
    }

    fun openAttachment(hash: SecureHash): InputStream = proxy.openAttachment(hash)
}