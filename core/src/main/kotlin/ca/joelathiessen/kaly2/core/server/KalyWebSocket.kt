package ca.joelathiessen.kaly2.core.server

import ca.joelathiessen.kaly2.core.server.messages.ConnectionOpenResponse
import ca.joelathiessen.kaly2.core.server.messages.RTMsg
import ca.joelathiessen.kaly2.core.server.messages.PastSlamInfosRequest
import ca.joelathiessen.kaly2.core.server.messages.RobotSessionSettingsRequest
import ca.joelathiessen.kaly2.core.server.messages.RobotSessionSettingsResponse
import ca.joelathiessen.kaly2.core.server.messages.RobotSessionSubscribeRequest
import ca.joelathiessen.kaly2.core.server.messages.RobotSessionSubscribeResponse
import ca.joelathiessen.kaly2.core.server.messages.RobotSessionUnsubscribeRequest
import ca.joelathiessen.kaly2.core.server.messages.RobotSessionUnsubscribeResponse
import ca.joelathiessen.kaly2.core.server.messages.SlamSettingsResponse
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
        connection.sendMessage(gson.toJson(RTMsg(ConnectionOpenResponse())))
    }

    override fun onMessage(data: String) {
        println("Message received: $data")

        val dataJson = JsonParser().parse(data).obj
        val msgType = dataJson[MSG_TYPE].string
        val msg = dataJson[MSG_LABEL]

        when (msgType) {
            RobotSessionSubscribeRequest.MSG_TYPE_NAME -> {
                var alternateServerAddress: String? = null
                val sessionReq = gson.fromJson<RobotSessionSubscribeRequest>(msg)
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
                connection.sendMessage(gson.toJson(RTMsg(RobotSessionSubscribeResponse(returnSessionID, success,
                        alternateServerAddress))))
            }
            RobotSessionUnsubscribeRequest.MSG_TYPE_NAME -> {
                val sessionRelReq = gson.fromJson<RobotSessionUnsubscribeRequest>(msg)
                session?.unsubscribeFromRTEvents(handleRTMessageCaller)
                session = null
                val successMsg = RobotSessionUnsubscribeResponse(sessionRelReq.sessionID, true)
                connection.sendMessage(gson.toJson(RTMsg(successMsg)))
            }
            RobotSessionSettingsRequest.MSG_TYPE_NAME -> {
                val settings = gson.fromJson<RobotSessionSettingsRequest>(msg)
                val running = session?.applyRobotSessionSettings(settings)
                connection.sendMessage(gson.toJson(RTMsg(RobotSessionSettingsResponse(session?.sid, running ?: false))))
            }
            SlamSettingsResponse.MSG_TYPE_NAME -> {
                val settings = gson.fromJson<SlamSettingsResponse>(msg)
                session?.applySlamSettings(settings)
            }
            PastSlamInfosRequest.MSG_TYPE_NAME -> {
                val pastIterationsReq = gson.fromJson<PastSlamInfosRequest>(msg)
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