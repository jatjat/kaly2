package ca.joelathiessen.util.itractor

import kotlin.concurrent.thread

class ItrActorThreadedHost(private val itrActor: ItrActor) {
    private var eventRunnerThread = makeEventHostThread()

    private fun makeEventHostThread() = thread(start = false) {
        itrActor.act()
    }

    @Synchronized
    fun start() {
        if (eventRunnerThread.isAlive == false) {
            eventRunnerThread = makeEventHostThread()
            eventRunnerThread.start()
        }
    }

    @Synchronized
    fun stop() {
        if (eventRunnerThread.isAlive == true) {
            itrActor.inputChannel.addMsg(StopMsg())
            eventRunnerThread.join()
        }
    }
}
