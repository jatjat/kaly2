package ca.joelathiessen.kaly2.planner

import ca.joelathiessen.util.itractor.ItrActor
import ca.joelathiessen.util.itractor.ItrActorChannel
import ca.joelathiessen.util.itractor.StopMsg


class PlannerActor(private val planner: GlobalPathPlanner, inputChannel: ItrActorChannel,
                   private val outputChannel: ItrActorChannel) : ItrActor(inputChannel) {

    override fun act() {
        while (true) {
            if (inputChannel.isEmpty() == false) {
                val msg = inputChannel.takeMsg()
                when (msg) {
                    is StopMsg -> return
                    is ReqPlannerManeuvers -> outputChannel.addMsg(PlannerRsltsMsg(planner.getManeuvers()))
                }
            }
            planner.iterate()
        }
    }
}