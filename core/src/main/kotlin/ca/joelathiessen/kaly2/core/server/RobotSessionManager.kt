package ca.joelathiessen.kaly2.core.server

import ca.joelathiessen.kaly2.core.server.messages.RTMsg
import java.util.HashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class RobotSessionManager(
    private val realRobotSessionFactory: RealRobotSessionFactory?,
    private val simRobotSessionFactory: SimRobotSessionFactory?
) {
    private val HEARTBEAT_TIME = 250L
    private val robotSessions = HashMap<Long, RobotSession>()
    private val shouldPerformHeartbeats = AtomicBoolean()

    init {
        thread(name = "Heartbeat") {
            while (shouldPerformHeartbeats.get()) {
                attemptHeartbeats()
                Thread.sleep(HEARTBEAT_TIME)
            }
        }
    }

    /**
     * Gets a robot session, or creates it if it does not already exist
     */
    sealed class GetHandlerResult {
        class RemoteRobotSessionAddressResult(val address: String) : GetHandlerResult()
        class RobotSessionResult(val session: RobotSession) : GetHandlerResult()
        class RobotSessionCreationError(val description: String? = null) : GetHandlerResult()
    }
    @Synchronized
    fun getHandler(sid: Long?, isReal: Boolean? = false): GetHandlerResult? {
        var chosenSid = sid
        var getHandlerResult: GetHandlerResult = GetHandlerResult.RobotSessionCreationError()
        if (chosenSid == null || robotSessions.containsKey(chosenSid) == false) {
            val factory: RobotSessionFactory
            if (isReal == true) {
                factory = checkNotNull(realRobotSessionFactory) {
                    "No factory to create a session with a real robot was provided"
                }
            } else {
                factory = checkNotNull(simRobotSessionFactory) {
                    "No factory to create a session with a simulated robot was provided"
                }
            }
            val sessionResult = factory.makeRobotSession(sid, { stopSid: Long -> handleSessionStoppedWithNoSubscribers(stopSid) })
            when (sessionResult) {
                is RobotSessionFactoryResult.LocalRobotSession -> {
                    chosenSid = sessionResult.session.sid
                    robotSessions[chosenSid] = sessionResult.session
                    printManagingMsg()
                    getHandlerResult = GetHandlerResult.RobotSessionResult(sessionResult.session)
                }
                is RobotSessionFactoryResult.RemoteRobotSessionAddress -> {
                    printManagingMsg()
                    getHandlerResult = GetHandlerResult.RemoteRobotSessionAddressResult(sessionResult.address)
                }
                is RobotSessionFactoryResult.RobotSessionCreationError -> {
                    printManagingMsg()
                    getHandlerResult = GetHandlerResult.RobotSessionCreationError(sessionResult.description)
                }
            }
        } else {
            val session = robotSessions[chosenSid]
            if (session != null) {
                getHandlerResult = GetHandlerResult.RobotSessionResult(session)
            }
        }
        return getHandlerResult
    }

    @Synchronized
    fun removeSession(sid: Long) {
        val session = robotSessions.get(sid)
        robotSessions.remove(sid)
        session?.releaseSessionHistory()
        printManagingMsg()
    }

    @Synchronized
    private fun handleSessionStoppedWithNoSubscribers(sid: Long) {
        removeSession(sid)
    }

    @Synchronized
    fun unsubscribeHandlerFromAllRTEvents(handler: (sender: Any, eventArgs: RTMsg) -> Unit) {
        robotSessions.values.forEach {
            it.unsubscribeFromRTEvents(handler)
        }
    }

    @Synchronized
    private fun attemptHeartbeats() {
        robotSessions.forEach {
            if (it.value.attemptHeartBeat() == false) {
                it.value.unsubscribeFromRTEvents()
            }
        }
    }

    private fun printManagingMsg() {
        println("Currently managing ${robotSessions.size} robot session${if (robotSessions.size != 1) "s" else ""}")
    }
}