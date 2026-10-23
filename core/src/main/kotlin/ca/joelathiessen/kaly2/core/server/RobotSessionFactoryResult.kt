package ca.joelathiessen.kaly2.core.server

sealed class RobotSessionFactoryResult {
    class LocalRobotSession(val session: RobotSession) : RobotSessionFactoryResult()
    class RemoteRobotSessionAddress(val address: String) : RobotSessionFactoryResult()
    class RobotSessionCreationError(val description: String? = null) : RobotSessionFactoryResult()
}