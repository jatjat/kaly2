package ca.joelathiessen.kaly2.core.slam

import ca.joelathiessen.kaly2.core.featuredetector.Feature
import ca.joelathiessen.kaly2.core.odometry.RobotPose
import lejos.robotics.navigation.Pose

interface Slam {
    val avgPose: RobotPose
    val particlePoses: List<Pose>

    fun resetTimeSteps()
    fun addTimeStep(features: List<Feature>, robotPose: RobotPose)
}