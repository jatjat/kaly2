package ca.joelathiessen.kaly2.core.odometry

import lejos.robotics.navigation.Pose

class RobotPose(val time: Long, val rotRate: Float, x: Float, y: Float, heading: Float) : Pose(x, y, heading) {
    constructor(pose: RobotPose): this(pose.time, pose.rotRate, pose.x, pose.y, pose.heading)
}