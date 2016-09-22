package ca.joelathiessen.kaly2.server

import java.util.*

class RobotsManager() {
    private val robotHandlers = HashMap<Long, RobotHandler>()
    var nextRid = 1L

    /**
     * Gets a handler for a robot, or creates it if it does not already exist
     */
    @Synchronized
    fun getHandler(rid: Long): RobotHandler {
        if (!robotHandlers.containsKey(rid)) {
            val robotHandler = RobotHandler(rid)
            robotHandler.startRobot()
            robotHandlers[rid] = robotHandler
        }
        return robotHandlers[rid]!!
    }

    @Synchronized
    fun removeHandler(rid: Long) {
        robotHandlers[rid]?.stopRobot()
        robotHandlers.remove(rid)
    }

    @Synchronized
    fun getUnspecifiedRid(): Long {
        val rid = nextRid
        nextRid++
        return rid
    }
}