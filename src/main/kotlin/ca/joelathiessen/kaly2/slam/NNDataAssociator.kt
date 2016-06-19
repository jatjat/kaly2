package ca.joelathiessen.kaly2.slam

import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.slam.landmarks.AssociatableLandmarks
import ca.joelathiessen.kaly2.slam.landmarks.Landmark
import lejos.robotics.geometry.Point
import lejos.robotics.navigation.Pose

import java.util.*

class NNDataAssociator(val threshold: Double = 0.01) : DataAssociator {
    override fun associate(pose: Pose, features: List<Feature>, landmarks: AssociatableLandmarks): Map<Feature, Landmark> {
        val featureLandmarks = HashMap<Feature, Landmark>()

        for(feature in features) {
            val offsetPoint = Point((feature.x + pose.x).toFloat(), (feature.y + pose.y).toFloat())
            val matchedLandmark = landmarks.getNearestNeighbor(offsetPoint)
            if(matchedLandmark != null) {
                if (offsetPoint.distance(Point(matchedLandmark.x.toFloat(), matchedLandmark.y.toFloat())) < threshold) {
                    featureLandmarks.put(feature, matchedLandmark)
                }
            }
        }
        return featureLandmarks
    }
}