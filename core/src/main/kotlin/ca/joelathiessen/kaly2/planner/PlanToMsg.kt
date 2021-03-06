package ca.joelathiessen.kaly2.planner

import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.util.itractor.ItrActorMsg

class PlanToMsg(val endPose: RobotPose) : ItrActorMsg()