package ca.joelathiessen.kaly2.slam.landmarks

import Jama.Matrix
import ca.joelathiessen.kaly2.featuredetector.Feature
import lejos.robotics.navigation.Pose

class Landmark {
    val x: Double
    val y: Double
    val covariance: Matrix

    constructor(x: Double, y: Double, covariance: Matrix) {
        this.x = x
        this.y = y
        this.covariance = covariance
    }

    constructor(pose: Pose, feature: Feature, covariance: Matrix) {
        this.x = pose.x + Math.cos(feature.angle) * feature.distance
        this.y = pose.y + Math.sin(feature.angle) * feature.distance
        this.covariance = covariance
    }
}