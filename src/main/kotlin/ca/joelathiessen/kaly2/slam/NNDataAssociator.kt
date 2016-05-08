package ca.joelathiessen.kaly2.slam

import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.slam.landmarks.AssociatableLandmarks
import ca.joelathiessen.kaly2.slam.landmarks.Landmark
import lejos.robotics.geometry.Point
import lejos.robotics.navigation.Pose

import java.util.*

class NNDataAssociator : DataAssociator{
    override fun associate(pose: Pose, features: List<Feature>, landmarks: AssociatableLandmarks): Map<Feature, Landmark> {
        val featureLandmarks = HashMap<Feature, Landmark>()

        for(feature in features) {
            val offsetPoint = Point(feature.x + pose.x, feature.y + pose.y)
            val matchedLandmark = landmarks.getNearestNeighbor(offsetPoint)
            featureLandmarks.putIfAbsent(feature, matchedLandmark)
        }
        return featureLandmarks
    }
}