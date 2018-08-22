package ca.joelathiessen.kaly2.server

import ca.joelathiessen.kaly2.server.messages.RTMsg
import java.util.HashMap

class RobotSessionManager(
    private val realRobotSessionFactory: RobotSessionFactory?,
    private val simRobotSessionFactory: RobotSessionFactory?
) {
    private val robotSessions = HashMap<Long, RobotSession>()
    private var nextSID = 1L

    /**
     * Gets a robot session, or creates it if it does not already exist
     */
    @Synchronized
    fun getHandler(sid: Long?): RobotSession? {
        var chosenSid = sid
        if (chosenSid == null || !robotSessions.containsKey(chosenSid)) {
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
    fun getUnspecifiedSID(): Long {
        val sid = nextSID
        nextSID++
        return sid
    }

    @Synchronized
    fun unsubscribeHandlerFromAllRTEvents(handler: (sender: Any, eventArgs: RTMsg) -> Unit) {
        robotSessions.values.forEach {
            it.unsubscribeFromRTEvents(handler)
        }
    }

    private fun printManagingMsg() {
        println("Currently managing ${robotSessions.size} robot session${if (robotSessions.size != 1) "s" else ""}")
    }
}