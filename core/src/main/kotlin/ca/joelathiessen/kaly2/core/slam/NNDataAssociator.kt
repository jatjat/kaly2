package ca.joelathiessen.kaly2.core.slam

import ca.joelathiessen.kaly2.core.featuredetector.Feature
import ca.joelathiessen.kaly2.core.slam.landmarks.AssociatableLandmarks
import ca.joelathiessen.kaly2.core.slam.landmarks.Landmark
import ca.joelathiessen.util.FloatMath
import lejos.robotics.geometry.Point
import lejos.robotics.navigation.Pose
import java.util.HashMap

class NNDataAssociator(val threshold: Float = 15.0f) : DataAssociator {
    override fun associate(pose: Pose, features: List<Feature>, landmarks: AssociatableLandmarks): Map<Feature, Landmark?> {
        val featureLandmarks = HashMap<Feature, Landmark?>()

        for (feature in features) {
            val xHyp = pose.x + (FloatMath.cos(feature.angle) * feature.distance)
            val yHyp = pose.y + (FloatMath.sin(feature.angle) * feature.distance)

            val offsetPoint = Point(xHyp, yHyp)
            val matchedLandmark = landmarks.getNearestNeighbor(offsetPoint)
            if (matchedLandmark != null) {
                if (offsetPoint.distance(Point(matchedLandmark.x, matchedLandmark.y)) < threshold) {
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