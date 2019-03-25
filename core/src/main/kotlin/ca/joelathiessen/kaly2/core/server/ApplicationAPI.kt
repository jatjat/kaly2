package ca.joelathiessen.kaly2.core.server

class ApplicationAPI(private val robotSessionManager: RobotSessionManager) {

    // expose robot sessions directly
    fun getRobotSession(sessionID: Long? = null, isReal: Boolean = false): RobotSession? {
        val handlerResult = robotSessionManager.getHandler(sessionID, isReal)
        when (handlerResult) {
            is RobotSessionManager.GetHandlerResult.RobotSessionResult -> {
                return handlerResult.session
            }
        }
        return null
    }
}