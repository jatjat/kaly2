package ca.joelathiessen.kaly2.core.slam

import ca.joelathiessen.kaly2.core.featuredetector.Feature
import ca.joelathiessen.kaly2.core.slam.landmarks.AssociatableLandmarks
import ca.joelathiessen.kaly2.core.slam.landmarks.Landmark
import lejos.robotics.navigation.Pose

interface DataAssociator {
    fun associate(pose: Pose, features: List<Feature>, landmarks: AssociatableLandmarks): Map<Feature, Landmark?>
}