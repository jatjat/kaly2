package ca.joelathiessen.kaly2

import ca.joelathiessen.kaly2.planner.ReqPlannerManeuvers
import ca.joelathiessen.kaly2.planner.PlannerRsltsMsg
import ca.joelathiessen.kaly2.subconscious.SubconscRsltsMsg
import ca.joelathiessen.util.itractor.ItrActor
import ca.joelathiessen.util.itractor.ItrActorChannel
import ca.joelathiessen.util.itractor.StopMsg

class RobotCoreActor(private val robotCore: RobotCoreActed, inputChannel: ItrActorChannel,
                     private val outputChannel: ItrActorChannel, private val plannerInputChannel: ItrActorChannel)
    : ItrActor(inputChannel) {

    init {
        robotCore.reqPlannerManeuvers = { plannerInputChannel.addMsg(ReqPlannerManeuvers()) }
    }

    override fun act() {
        while (true) {
            val msg = inputChannel.takeMsg()
            when (msg) {
                is StopMsg -> return
                is PlannerRsltsMsg -> robotCore.maneuvers = msg.maneuvers
                is SubconscRsltsMsg -> outputChannel.addMsg(RobotCoreRsltsMsg(robotCore.iterate(msg.results)))
            }
        }
    }
}