package ca.joelathiessen.kaly2.slam

import ca.joelathiessen.kaly2.odometry.MotionModel
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.slam.landmarks.LandmarksTree
import lejos.robotics.navigation.Pose

class Particle(pose: Pose, var weight: Float = 0.0f, val landmarks: LandmarksTree = LandmarksTree()) {
    var pose: Pose = pose
        private set

    fun copy(): Particle {
        return Particle(Pose(pose.x, pose.y, pose.heading), weight, landmarks.copy())
    }

    //render obeisance to encapsulation...
    fun moveRandom(startRobotPose: RobotPose, endRobotPose: RobotPose, motionModel: MotionModel) {
        pose = motionModel.moveRandom(pose, startRobotPose, endRobotPose)
    }
}