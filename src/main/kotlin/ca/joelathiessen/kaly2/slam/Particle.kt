package ca.joelathiessen.kaly2.slam

import ca.joelathiessen.kaly2.MotionModel
import ca.joelathiessen.kaly2.RobotPose
import ca.joelathiessen.kaly2.slam.landmarks.LandmarksTree
import lejos.robotics.navigation.Pose

data class Particle(var pose: Pose, var variance: Float = 0f) {
    val landmarks = LandmarksTree()

    //render obeisance to encapsulation...
    fun moveRandom(startRobotPose: RobotPose, endRobotPose: RobotPose, motionModel: MotionModel) {
        motionModel.moveRandom(pose, startRobotPose, endRobotPose)
    }
}