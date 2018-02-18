package ca.joelathiessen.kaly2.server

interface RobotSessionFactory {
    fun makeRobotSession(sid: Long, sessionStoppedWithNoSubscribersHandler: () -> Unit = {}): RobotSession
}