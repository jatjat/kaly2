package ca.joelathiessen.kaly2.server

import org.eclipse.jetty.websocket.WebSocket
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class KalyWebSocket() : WebSocket.OnTextMessage {
    private val connectionExecutor: Executor
    private lateinit var connection: WebSocket.Connection

    init {
        connectionExecutor = Executors.newSingleThreadExecutor()!!
    }

    override fun onOpen(connection: WebSocket.Connection) {
        this.connection = connection
        connection.sendMessage("hello WebSocket world!")
    }

    override fun onMessage(data: String) {
    }

    override fun onClose(closeCode: Int, message: String?) {
    }

    // Unlike Jetty 9, Jetty 7 does not support asynchronous message sending
    fun sendMessage(message: String) {
        connectionExecutor.execute { connection.sendMessage(message) }
    }
}