package ca.joelathiessen.kaly2.slam

import ca.joelathiessen.kaly2.slam.landmarks.LandmarksTree
import lejos.robotics.navigation.Pose

data class Particle(val pose: Pose, var stdDev: Float = 0f) {
    val landmarks = LandmarksTree()

    fun moveRandom(dist: Float, distVar: Float = 0f, rot: Float, rotVar: Float = 0f) {
    }
}