package ca.joelathiessen.kaly2.core.odometry

import lejos.robotics.navigation.Pose

interface MotionModel {

    /**
     * Obtain, randomly perturb, and then apply, a transformation that
     * describes the difference between two poses, to an input pose
     * @param inputPose
     * @param startReadPose
     * @param endReadPose
     * @return the transformed position
     */
    fun moveRandom(inputPose: Pose, startReadPose: RobotPose, endReadPose: RobotPose): Pose
}