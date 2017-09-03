package ca.joelathiessen.kaly2.planner

import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.util.itractor.ItrActorMsg

class PlannerManeuversMsg(val maneuvers: List<RobotPose>) : ItrActorMsg()