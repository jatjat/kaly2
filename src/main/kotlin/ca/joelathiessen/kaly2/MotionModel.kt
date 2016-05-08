package ca.joelathiessen.kaly2

import lejos.robotics.navigation.Pose

interface MotionModel {
    fun moveRandom(particlePose: Pose, startRobotPose: RobotPose, endRobotPose: RobotPose)
}