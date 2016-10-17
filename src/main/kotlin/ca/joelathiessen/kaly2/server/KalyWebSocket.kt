package ca.joelathiessen.kaly2.server

import ca.joelathiessen.kaly2.server.messages.FastSlamSettingsMsg
import ca.joelathiessen.kaly2.server.messages.RTMsg
import ca.joelathiessen.kaly2.server.messages.RobotSettingsMsg
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import org.eclipse.jetty.websocket.WebSocket
import java.util.concurrent.Executors

class KalyWebSocket(private val robotsManager: RobotsManager, private val rid: Long) : WebSocket.OnTextMessage {
    val MSG_TYPE = "msgType"
    val MSG_LABEL = "msg"
    private val connectionExecutor = Executors.newSingleThreadExecutor()!!
    private lateinit var connection: WebSocket.Connection
    private lateinit var robotHandler: RobotHandler
    private val handleRTMessageCaller = { sender: Any, msg: RTMsg -> HandleRTMessage(sender, msg) }
    private val gson = GsonBuilder().create()!!
    private var closed = false
    private var closedLock = Any()

    override fun onOpen(connection: WebSocket.Connection) {
        this.connection = connection
        this.robotHandler = robotsManager.getHandler(rid)

        robotHandler.rtUpdateEvent += handleRTMessageCaller
    }

    override fun onMessage(data: String) {
        println("Message received: $data")

        val dataJson = JsonParser().parse(data).obj
        val msgType = dataJson[MSG_TYPE].string
        val msg = dataJson[MSG_LABEL]

        if (msgType == RobotSettingsMsg.MSG_TYPE_NAME) {
            this.robotHandler.applyRobotSettings(gson.fromJson<RobotSettingsMsg>(msg))
        } else if (msgType == FastSlamSettingsMsg.MSG_TYPE_NAME) {
            this.robotHandler.applyFastSlamSettings(gson.fromJson<FastSlamSettingsMsg>(msg))
        }
    }

    override fun onClose(closeCode: Int, message: String?) {
        robotHandler.rtUpdateEvent -= handleRTMessageCaller
        if (robotHandler.rtUpdateEvent.length == 0) {
            robotsManager.removeHandler(rid)
        }
        synchronized(closedLock) {
            closed = true
            connectionExecutor.shutdownNow()
        }
    }

    // Unlike Jetty 9, Jetty 7 does not support asynchronous message sending
    // See http://jetty.4.x6.nabble.com/jetty-dev-WebSocket-Async-Read-Write-td4466406.html
    fun HandleRTMessage(sender: Any, message: RTMsg) {
        connectionExecutor.execute {
            synchronized(closedLock) {
                if (!closed) {
                    connection.sendMessage(gson.toJson(message))
                }
            }
        }
    }
}