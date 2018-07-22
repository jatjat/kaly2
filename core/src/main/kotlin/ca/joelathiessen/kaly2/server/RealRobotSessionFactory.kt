package ca.joelathiessen.kaly2.server

class RealRobotSessionFactory : RobotSessionFactory {
    override fun makeRobotSession(sid: Long?, sessionStoppedWithNoSubscribersHandler: (stopSid: Long) -> Unit): RobotSession? {
        TODO("not implemented")
    }
}