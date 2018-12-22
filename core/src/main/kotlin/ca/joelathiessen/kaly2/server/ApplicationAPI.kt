package ca.joelathiessen.kaly2.server

class ApplicationAPI(private val robotSessionManager: RobotSessionManager) {

    // expose robot sessions directly
    fun getRobotSession(sessionID: Long? = null): RobotSession? {
        val handlerResult = robotSessionManager.getHandler(sessionID)
        when (handlerResult) {
            is RobotSessionManager.GetHandlerResult.RobotSessionResult -> {
                return handlerResult.session
            }
        }
        return null
    }
}