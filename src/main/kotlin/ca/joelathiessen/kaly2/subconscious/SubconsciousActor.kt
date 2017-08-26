package ca.joelathiessen.kaly2.subconscious

import ca.joelathiessen.kaly2.planner.NewGlblPlnMsg
import ca.joelathiessen.util.itractor.ItrActor
import ca.joelathiessen.util.itractor.ItrActorChannel
import ca.joelathiessen.util.itractor.ItrActorMsg
import ca.joelathiessen.util.itractor.StopMsg

class SubconsciousActor(private val subconscious: SubconsciousActed, inputChannel: ItrActorChannel,
                        private val outputChannel: ItrActorChannel) : ItrActor(inputChannel) {

    override fun act() {
        while (true) {
            var msg: ItrActorMsg? = null
            if (inputChannel.isEmpty() == false) {
                msg = inputChannel.takeMsg()
                when (msg) {
                    is StopMsg -> return
                }
            }
            val results = if(msg is NewGlblPlnMsg) subconscious.iterate(msg.plan) else subconscious.iterate()
            outputChannel.addMsg(SubconscRsltsMsg(results))
        }
    }
}