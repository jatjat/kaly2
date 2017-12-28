package ca.joelathiessen.kaly2.server

import java.util.HashMap

class RobotSessionManager {
    private val robotSessions = HashMap<Long, RobotSession>()
    private var nextSID = 1L

    /**
     * Gets a robot session, or creates it if it does not already exist
     */
    @Synchronized
    fun getHandler(sid: Long): RobotSession {
        if (!robotSessions.containsKey(sid)) {
            val robotSession = RobotSession(sid,
                    sessionStoppedWithNoSubscribersHandler = { handleSessionStoppedWithNoSubscribers(sid) })
            robotSessions[sid] = robotSession
        }
        printManagingMsg()
        return robotSessions[sid]!!
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

    private fun printManagingMsg() {
        println("Currently managing ${robotSessions.size} robot session${if (robotSessions.size != 1) "s" else ""}")
    }
}