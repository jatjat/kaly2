package ca.joelathiessen.kaly2.tests.pc.unit.util.itractor

import ca.joelathiessen.util.itractor.ItrActor
import ca.joelathiessen.util.itractor.ItrActorChannel
import ca.joelathiessen.util.itractor.StopMsg

class ItrActorSampleImpl(channel: ItrActorChannel) : ItrActor(channel) {
    override fun act() {
        while (true) {
            val msg = inputChannel.takeMsg()
            when (msg) {
                is StopMsg -> return
            }
        }
    }
}
