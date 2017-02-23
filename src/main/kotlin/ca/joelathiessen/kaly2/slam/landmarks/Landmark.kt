package ca.joelathiessen.kaly2.slam.landmarks

import Jama.Matrix
import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.util.FloatMath
import lejos.robotics.navigation.Pose

class Landmark {
    val x: Float
    val y: Float
    val covariance: Matrix

    constructor(x: Float, y: Float, covariance: Matrix) {
        this.x = x
        this.y = y
        this.covariance = covariance
    }

    constructor(pose: Pose, feature: Feature, covariance: Matrix) {
        this.x = pose.x + (FloatMath.cos(feature.angle) * feature.distance)
        this.y = pose.y + (FloatMath.sin(feature.angle) * feature.distance)
        this.covariance = covariance
    }
}