package ca.joelathiessen.kaly2.planner

import ca.joelathiessen.kaly2.map.MapTree
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.util.itractor.ItrActorMsg

class PlanFromMsg(val startPose: RobotPose, val obstacles: MapTree) : ItrActorMsg()