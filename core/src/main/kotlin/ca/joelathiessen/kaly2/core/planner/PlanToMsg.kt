package ca.joelathiessen.kaly2.core.planner

import ca.joelathiessen.kaly2.core.odometry.RobotPose
import ca.joelathiessen.util.itractor.ItrActorMsg

class PlanToMsg(val endPose: RobotPose) : ItrActorMsg()