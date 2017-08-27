package ca.joelathiessen.kaly2.planner

import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.subconscious.SubconsciousActedResults
import ca.joelathiessen.util.itractor.ItrActorMsg

class PlannerRsltsMsg(val maneuvers: List<RobotPose>): ItrActorMsg()