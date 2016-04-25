package ca.joelathiessen.kaly2.slam

import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.slam.landmarks.AssociatableLandmarks
import ca.joelathiessen.kaly2.slam.landmarks.Landmark
import lejos.robotics.geometry.Point
import lejos.robotics.navigation.Pose


interface DataAssociator {
    fun associate(pose: Pose, features: List<Feature>, landmarks: AssociatableLandmarks): Map<Feature, Pair<Point, Landmark>>

}