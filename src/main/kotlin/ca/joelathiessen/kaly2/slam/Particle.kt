package ca.joelathiessen.kaly2.slam

import ca.joelathiessen.kaly2.MotionModel
import ca.joelathiessen.kaly2.RobotPose
import ca.joelathiessen.kaly2.slam.landmarks.LandmarksTree
import lejos.robotics.navigation.Pose

class Particle(val pose: RobotPose, var weight: Double = 0.0, val landmarks: LandmarksTree = LandmarksTree()) {

    fun copy(): Particle {
        return Particle(pose, weight, landmarks.copy())
    }

    //render obeisance to encapsulation...
    fun moveRandom(startRobotPose: RobotPose, endRobotPose: RobotPose, motionModel: MotionModel) {
        motionModel.moveRandom(pose, startRobotPose, endRobotPose)
    }
}