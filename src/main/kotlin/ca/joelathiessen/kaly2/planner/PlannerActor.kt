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
                    is ReqPlannerManeuvers -> outputChannel.addMsg(PlannerManeuversMsg(planner.getManeuvers()))
                    is PlanFromMsg -> planner.planFrom(msg.startPose)
                }
            } else {
                outputChannel.addMsg(PlannerPathsMsg(planner.iterate()))
            }
        }
    }
}