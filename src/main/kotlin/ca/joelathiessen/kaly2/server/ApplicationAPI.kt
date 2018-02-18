package ca.joelathiessen.kaly2.server

class ApplicationAPI(private val robotSessionManager: RobotSessionManager) {

    // expose robot sessions directly
    fun getRobotSession(sessionID: Long): RobotSession {
        return robotSessionManager.getHandler(sessionID)
    }
}