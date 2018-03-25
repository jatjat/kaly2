package ca.joelathiessen.kaly2.slam

import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.odometry.RobotPose
import lejos.robotics.navigation.Pose

interface Slam {
    val avgPose: RobotPose
    val particlePoses: List<Pose>

    fun resetTimeSteps()
    fun addTimeStep(features: List<Feature>, robotPose: RobotPose)
}