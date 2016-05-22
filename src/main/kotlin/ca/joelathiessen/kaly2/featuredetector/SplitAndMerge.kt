package ca.joelathiessen.kaly2.featuredetector

import ca.joelathiessen.kaly2.Measurement
import java.util.*

class SplitAndMerge : FeatureDetector {

    private val THRESHOLD_DIST = 0.1

    override fun getFeatures(measurements: List<Measurement>): List<Feature> {
        //create a list of points from the measurements
        val features = ArrayList<SplitAndMergeFeature>(measurements.size)
        for (mes in measurements) {
            features.add(SplitAndMergeFeature(0.0, 0.0, mes.distance, mes.angle))
        }

        val featuresOfInterest = splitAndMerge(features, THRESHOLD_DIST)

        return featuresOfInterest
    }

    private fun splitAndMerge(inputPoints: List<SplitAndMergeFeature>, epsilon: Double): List<SplitAndMergeFeature> {
        var distMax = 0.0
        var distMaxIndex = 0
        val results = ArrayList<SplitAndMergeFeature>(2)

        for (i in 1..inputPoints.size - 1 - 1) {
            val dist = distanceFromLineToPoint(inputPoints[i], inputPoints[0], inputPoints[inputPoints.size - 1])
            if (dist > distMax) {
                distMaxIndex = i
                distMax = dist
            }
        }

        if (distMax > epsilon) {
            // the point is far enough away from our line so keep it
            val list1 = splitAndMerge(inputPoints.subList(0, distMaxIndex), epsilon)
            val list2 = splitAndMerge(inputPoints.subList(distMaxIndex, inputPoints.size), epsilon)

            val subLength = list1.size - 1
            results.ensureCapacity(subLength + list2.size)
            results.addAll(list1.subList(0, subLength))
            results.addAll(list2)
        } else {
            // discard all the points between the start and end points
            inputPoints[0].incrDiscardedPointsCount(inputPoints.size - 2)
            results.add(0, inputPoints[0])
            results.add(1, inputPoints[inputPoints.size - 1])
        }
        return results
    }

    private fun distanceFromLineToPoint(point: Feature, lineOne: Feature, lineTwo: Feature): Double {
        val x2MinusX1 = lineTwo.x - lineOne.x
        val y2MinusY1 = lineTwo.y - lineOne.y

        val numerator = Math.abs(y2MinusY1 * point.x - x2MinusX1 * point.y + lineTwo.x * lineOne.y - lineTwo.y * lineOne.x)
        val denominator = Math.sqrt((y2MinusY1 * y2MinusY1 + x2MinusX1 * x2MinusX1))

        return numerator / denominator
    }
}