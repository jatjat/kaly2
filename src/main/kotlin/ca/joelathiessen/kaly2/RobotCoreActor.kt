package ca.joelathiessen.kaly2

import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.planner.PlannerManeuversMsg
import ca.joelathiessen.kaly2.planner.PlannerPathsMsg
import ca.joelathiessen.kaly2.planner.ReqPlannerManeuvers
import ca.joelathiessen.kaly2.subconscious.SubconscRsltsMsg
import ca.joelathiessen.util.itractor.ItrActor
import ca.joelathiessen.util.itractor.ItrActorChannel
import ca.joelathiessen.util.itractor.StopMsg

class RobotCoreActor(private val robotCore: RobotCoreActed, inputChannel: ItrActorChannel,
                     private val outputChannel: ItrActorChannel, private val plannerInputChannel: ItrActorChannel,
                     private val subconscInputChannel: ItrActorChannel)
    : ItrActor(inputChannel) {

    init {
        robotCore.reqPlannerManeuvers = { plannerInputChannel.addMsg(ReqPlannerManeuvers()) }
        robotCore.sendPlannerManeuversToLocalPlanner = {
            maneuvers: List<RobotPose> -> subconscInputChannel.addMsg(PlannerManeuversMsg(maneuvers))
        }
    }

    override fun act() {
        while (true) {
            val msg = inputChannel.takeMsg()
            when (msg) {
                is StopMsg -> return
                is PlannerManeuversMsg -> robotCore.onManeuverResults(msg.maneuvers)
                is PlannerPathsMsg -> robotCore.onPaths(msg.paths)
                is SubconscRsltsMsg -> outputChannel.addMsg(RobotCoreRsltsMsg(robotCore.iterate(msg.results)))
            }
        }
    }
}