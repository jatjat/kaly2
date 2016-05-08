package ca.joelathiessen.kaly2.slam

import ca.joelathiessen.kaly2.RobotPose
import ca.joelathiessen.kaly2.featuredetector.Feature
import lejos.robotics.navigation.Pose

interface Slam {
    fun resetTimeSteps()
    fun addTimeStep(features: List<Feature>, robotPose: RobotPose)
    fun getCurPos(): Pose
}