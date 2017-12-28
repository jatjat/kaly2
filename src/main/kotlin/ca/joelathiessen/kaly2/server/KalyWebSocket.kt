package ca.joelathiessen.kaly2.server

import ca.joelathiessen.kaly2.server.messages.*
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import org.eclipse.jetty.websocket.WebSocket
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class KalyWebSocket(private val robotSessionManager: RobotSessionManager) : WebSocket.OnTextMessage {
    val TERMINATION_TIMOUT_SECONDS = 10L
    val MSG_TYPE = "msgType"
    val MSG_LABEL = "msg"
    private val connectionExecutor = Executors.newSingleThreadExecutor()!!
    private lateinit var connection: WebSocket.Connection
    private var robotSession: RobotSession? = null
    private val handleRTMessageCaller = { sender: Any, msg: RTMsg -> HandleRTMessage(sender, msg) } // can't pass HandleRTMessage directly
    private val gson = {
        val builder = GsonBuilder()
        builder.registerTypeAdapter(RobotSessionSettingsReqMsg::class.java, RobotSessionSettingsMsgDeserializer())
        builder.create()
    }()
    private var closedLock = Any()

    override fun onOpen(connection: WebSocket.Connection) {
        this.connection = connection
        robotSession?.subscribeToRTEvents(handleRTMessageCaller)
    }

    override fun onMessage(data: String) {
        println("Message received: $data")

        val dataJson = JsonParser().parse(data).obj
        val msgType = dataJson[MSG_TYPE].string
        val msg = dataJson[MSG_LABEL]

        if (msgType == RobotSessionSettingsReqMsg.MSG_TYPE_NAME) {
            val settings = gson.fromJson<RobotSessionSettingsReqMsg>(msg)
            updateRobotSession(settings.sessionID)
            this.robotSession?.applyRobotSessionSettings(settings)
        } else if (msgType == SlamSettingsMsg.MSG_TYPE_NAME) {
            val settings = gson.fromJson<SlamSettingsMsg>(msg)
            updateRobotSession(settings.sessionID)
            this.robotSession?.applySlamSettings(settings)
        }
    }

    private fun updateRobotSession(sessionID: Long?) {
        val shouldReplace = sessionID != null && sessionID != robotSession?.rid
        val shouldStart = robotSession == null && sessionID == null
        if(shouldReplace || shouldStart) {
            robotSession?.unsubscribeFromRTEvents(handleRTMessageCaller)
            robotSession = robotSessionManager.getHandler(sessionID ?: robotSessionManager.getUnspecifiedSID())
            robotSession?.subscribeToRTEvents(handleRTMessageCaller)
        }
    }

    override fun onClose(closeCode: Int, message: String?) {
        robotSession?.unsubscribeFromRTEvents(handleRTMessageCaller)
        synchronized(closedLock) {
            connectionExecutor.shutdownNow()
            connectionExecutor.awaitTermination(TERMINATION_TIMOUT_SECONDS, TimeUnit.SECONDS)
        }
    }

    // Unlike Jetty 9, Jetty 7 does not support asynchronous message sending
    // See http://jetty.4.x6.nabble.com/jetty-dev-WebSocket-Async-Read-Write-td4466406.html
    fun HandleRTMessage(@Suppress("UNUSED_PARAMETER") sender: Any, message: RTMsg) {
        connectionExecutor.execute {
            synchronized(closedLock) {
                if (!connectionExecutor.isShutdown()) {
                    connection.sendMessage(gson.toJson(message))
                }
            }
        }
    }
}