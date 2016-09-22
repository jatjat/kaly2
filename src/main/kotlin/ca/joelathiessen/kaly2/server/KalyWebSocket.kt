package ca.joelathiessen.kaly2.server

import com.google.gson.GsonBuilder
import org.eclipse.jetty.websocket.WebSocket
import java.util.concurrent.Executors

class KalyWebSocket(private val robotsManager: RobotsManager, private val rid: Long) : WebSocket.OnTextMessage {
    private val connectionExecutor = Executors.newSingleThreadExecutor()!!
    private lateinit var connection: WebSocket.Connection
    private lateinit var robotHandler: RobotHandler
    private val handleRTMessageCaller = { sender: Any, msg: RTMsg -> HandleRTMessage(sender, msg) }
    private val gson = GsonBuilder().create()!!

    override fun onOpen(connection: WebSocket.Connection) {
        this.connection = connection
        this.robotHandler = robotsManager.getHandler(rid)

        robotHandler.rtUpdateEvent += handleRTMessageCaller
    }

    override fun onMessage(data: String) {
        println("Message received: $data")
    }

    override fun onClose(closeCode: Int, message: String?) {
        robotHandler.rtUpdateEvent -= handleRTMessageCaller
        if(robotHandler.rtUpdateEvent.length == 0) {
            robotsManager.removeHandler(rid)
        }
    }

    // Unlike Jetty 9, as far as I'm aware, Jetty 7 does not support asynchronous message sending
    fun HandleRTMessage(sender: Any, message: RTMsg) {
        connectionExecutor.execute {
            connection.sendMessage(gson.toJson(message))
        }
    }
}