package ca.joelathiessen.kaly2.core.server

interface RobotSessionFactory {
    fun makeRobotSession(sid: Long?, sessionStoppedWithNoSubscribersHandler: (stopSid: Long) -> Unit = {}): RobotSessionFactoryResult
}