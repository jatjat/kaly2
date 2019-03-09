package ca.joelathiessen.kaly2.core.planner

import ca.joelathiessen.kaly2.core.map.MapTree
import ca.joelathiessen.kaly2.core.odometry.RobotPose
import ca.joelathiessen.util.itractor.ItrActorMsg

class PlanFromMsg(val startPose: RobotPose, val obstacles: MapTree) : ItrActorMsg()