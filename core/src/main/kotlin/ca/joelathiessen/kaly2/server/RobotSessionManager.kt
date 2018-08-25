package ca.joelathiessen.kaly2.server

import ca.joelathiessen.kaly2.server.messages.RTMsg
import java.util.HashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class RobotSessionManager(
    private val realRobotSessionFactory: RobotSessionFactory?,
    private val simRobotSessionFactory: RobotSessionFactory?
) {
    private val HEARTBEAT_TIME = 250L
    private val robotSessions = HashMap<Long, RobotSession>()
    private val shouldPerformHeartbeats = AtomicBoolean()

    init {
        thread {
            while (shouldPerformHeartbeats.get()) {
                attemptHeartbeats()
                Thread.sleep(HEARTBEAT_TIME)
            }
        }
    }

    /**
     * Gets a robot session, or creates it if it does not already exist
     */
    @Synchronized
    fun getHandler(sid: Long?): RobotSession? {
        var chosenSid = sid
        if (chosenSid == null || robotSessions.containsKey(chosenSid) == false) {
            val factory: RobotSessionFactory
            if (chosenSid == 0L) {
                factory = checkNotNull(realRobotSessionFactory) {
                    "No factory to create a session with a real robot was provided"
                }
            } else {
                factory = checkNotNull(simRobotSessionFactory) {
                    "No factory to create a session with a simulated robot was provided"
                }
            }
            val session = factory.makeRobotSession(sid, { stopSid: Long -> handleSessionStoppedWithNoSubscribers(stopSid) })
            if (session != null) {
                chosenSid = session.sid
                robotSessions[chosenSid] = session
            }
        }
        printManagingMsg()
        return robotSessions[chosenSid]
    }

    @Synchronized
    fun removeSession(sid: Long) {
        robotSessions.remove(sid)
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

    @Synchronized
    fun shutDown() {
        robotSessions.values.forEach {
            it.unsubscribeFromRTEvents()
            removeSession(it.sid)
        }
        shouldPerformHeartbeats.set(false)
    }

    private fun printManagingMsg() {
        println("Currently managing ${robotSessions.size} robot session${if (robotSessions.size != 1) "s" else ""}")
    }
}