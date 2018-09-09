package ca.joelathiessen.kaly2.server

import ca.joelathiessen.kaly2.server.messages.RTConnectionOpenMsg
import ca.joelathiessen.kaly2.server.messages.RTMsg
import ca.joelathiessen.kaly2.server.messages.RTPastSlamInfosReqMsg
import ca.joelathiessen.kaly2.server.messages.RTRobotSessionSettingsReqMsg
import ca.joelathiessen.kaly2.server.messages.RTRobotSessionSubscribeReqMsg
import ca.joelathiessen.kaly2.server.messages.RTRobotSessionSubscribeRespMsg
import ca.joelathiessen.kaly2.server.messages.RTRobotSessionUnsubscribeReqMsg
import ca.joelathiessen.kaly2.server.messages.RTRobotSessionUnsubscribeRespMsg
import ca.joelathiessen.kaly2.server.messages.RTSlamSettingsMsg
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
    private val handleRTMessageCaller = { sender: Any, msg: RTMsg -> handleRTMessage(sender, msg) } // can't pass handleRTMessage directly
    private val gson = {
        val builder = GsonBuilder()
        builder.create()
    }()
    private var closedLock = Any()
    private var session: RobotSession? = null

    override fun onOpen(connection: WebSocket.Connection) {
        this.connection = connection
        connection.sendMessage(gson.toJson(RTMsg(RTConnectionOpenMsg())))
    }

    override fun onMessage(data: String) {
        println("Message received: $data")

        val dataJson = JsonParser().parse(data).obj
        val msgType = dataJson[MSG_TYPE].string
        val msg = dataJson[MSG_LABEL]

        when (msgType) {
            RTRobotSessionSubscribeReqMsg.MSG_TYPE_NAME -> {
                var alternateServerAddress: String? = null
                val sessionReq = gson.fromJson<RTRobotSessionSubscribeReqMsg>(msg)
                val robotSessionResult = robotSessionManager.getHandler(sessionReq.sessionID)
                var success = false
                var returnSessionID: Long? = sessionReq.sessionID
                when (robotSessionResult) {
                    is RobotSessionManager.GetHandlerResult.RobotSessionResult -> {
                        returnSessionID = robotSessionResult.session.sid
                        robotSessionResult.session.subscribeToRTEvents(handleRTMessageCaller)
                        session = robotSessionResult.session
                        success = true
                    }
                    is RobotSessionManager.GetHandlerResult.RemoteRobotSessionAddressResult -> {
                        alternateServerAddress = robotSessionResult.address
                    }
                }
                connection.sendMessage(gson.toJson(RTMsg(RTRobotSessionSubscribeRespMsg(returnSessionID, success,
                        alternateServerAddress))))
            }
            RTRobotSessionUnsubscribeReqMsg.MSG_TYPE_NAME -> {
                val sessionRelReq = gson.fromJson<RTRobotSessionUnsubscribeReqMsg>(msg)
                session?.unsubscribeFromRTEvents(handleRTMessageCaller)
                session = null
                val successMsg = RTRobotSessionUnsubscribeRespMsg(sessionRelReq.sessionID, true)
                connection.sendMessage(gson.toJson(RTMsg(successMsg)))
            }
            RTRobotSessionSettingsReqMsg.MSG_TYPE_NAME -> {
                val settings = gson.fromJson<RTRobotSessionSettingsReqMsg>(msg)
                session?.applyRobotSessionSettings(settings)
            }
            RTSlamSettingsMsg.MSG_TYPE_NAME -> {
                val settings = gson.fromJson<RTSlamSettingsMsg>(msg)
                session?.applySlamSettings(settings)
            }
            RTPastSlamInfosReqMsg.MSG_TYPE_NAME -> {
                val pastIterationsReq = gson.fromJson<RTPastSlamInfosReqMsg>(msg)
                val pastIterations = session?.getPastIterations(pastIterationsReq)
                if (pastIterations != null) {
                    connection.sendMessage(gson.toJson(RTMsg(pastIterations)))
                }
            }
        }
    }

    override fun onClose(closeCode: Int, message: String?) {
        robotSessionManager.unsubscribeHandlerFromAllRTEvents(handleRTMessageCaller)
        synchronized(closedLock) {
            connectionExecutor.shutdown()
            connectionExecutor.awaitTermination(TERMINATION_TIMOUT_SECONDS, TimeUnit.SECONDS)
        }
    }

    // Unlike Jetty 9, Jetty 7 does not support asynchronous message sending
    // See http://jetty.4.x6.nabble.com/jetty-dev-WebSocket-Async-Read-Write-td4466406.html
    fun handleRTMessage(@Suppress("UNUSED_PARAMETER") sender: Any, message: RTMsg) {
        if (message.requestingNoNetworkSend == false) {
            connectionExecutor.execute {
                synchronized(closedLock) {
                    if (!connectionExecutor.isShutdown()) {
                        if (connection.isOpen) {
                            connection.sendMessage(gson.toJson(message))
                        }
                    }
                }
            }
        }
    }
}