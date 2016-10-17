package ca.joelathiessen.kaly2.slam

import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.slam.landmarks.AssociatableLandmarks
import ca.joelathiessen.kaly2.slam.landmarks.Landmark
import lejos.robotics.geometry.Point
import lejos.robotics.navigation.Pose

import java.util.*

class NNDataAssociator(val threshold: Double = 15.0) : DataAssociator {
    override fun associate(pose: Pose, features: List<Feature>, landmarks: AssociatableLandmarks): Map<Feature, Landmark?> {
        val featureLandmarks = HashMap<Feature, Landmark?>()

        for (feature in features) {
            val xHyp = pose.x + Math.cos(feature.angle) * feature.distance
            val yHyp = pose.y + Math.sin(feature.angle) * feature.distance

            val offsetPoint = Point(xHyp.toFloat(), yHyp.toFloat())
            val matchedLandmark = landmarks.getNearestNeighbor(offsetPoint)
            if (matchedLandmark != null) {
                if (offsetPoint.distance(Point(matchedLandmark.x.toFloat(), matchedLandmark.y.toFloat())) < threshold) {
                    featureLandmarks.put(feature, matchedLandmark)
                } else {
                    featureLandmarks.put(feature, null)
                }
            } else {
                featureLandmarks.put(feature, null)
            }
        }
        return featureLandmarks
    }
}