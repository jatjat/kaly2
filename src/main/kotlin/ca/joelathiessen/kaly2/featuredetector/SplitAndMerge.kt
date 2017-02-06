package ca.joelathiessen.kaly2.featuredetector

import ca.joelathiessen.kaly2.Measurement
import java.util.*

class SplitAndMerge(val threshold: Double, val checkWithinAngle: Double, val maxRatio: Double) : FeatureDetector {
    private val TWO_PI = 2 * Math.PI

    override fun getFeatures(measurements: List<Measurement>): List<Feature> {
        val features = measurements.map {
            SplitAndMergeFeature(it.pose.x.toDouble(), it.pose.y.toDouble(), it.distance, it.angle)
        }

        var smFeats = splitAndMerge(features, threshold)
        val sepFeats = ArrayList<SplitAndMergeFeature>()

        if (smFeats.size > 1) {
            // trim the start and end features if they're within the threshold for lines between their adjacent features:
            if (distanceFromLineToPoint(smFeats[smFeats.size - 1], smFeats[0],
                    smFeats[smFeats.size - 2]) < threshold) {
                smFeats = smFeats.subList(0, smFeats.size - 1)
            }
            if (distanceFromLineToPoint(smFeats[0], smFeats[1],
                    smFeats[smFeats.size - 1]) < threshold) {
                smFeats[smFeats.size - 1].incrDiscardedPointsCount(smFeats[0].discardedPoints)
                smFeats = smFeats.subList(1, smFeats.size)
            }

            val extendedFeats = arrayListOf(smFeats[smFeats.size - 1]) + smFeats + smFeats[0]

            // trim features that are much deeper than adjacent features:
            var k = 1
            while (k < extendedFeats.size - 1) {
                var canAddLeft = true
                var canAddRight = true
                var withinAng = false

                val dAngLeft = Math.abs(extendedFeats[k].angle - extendedFeats[k - 1].angle) % TWO_PI
                if (dAngLeft < checkWithinAngle) {
                    val arcDist = extendedFeats[k - 1].distance * dAngLeft
                    val dDist = extendedFeats[k].distance - extendedFeats[k - 1].distance
                    withinAng = true
                    if (dDist / arcDist > maxRatio) {
                        canAddLeft = false
                    }
                }

                val dAngRight = Math.abs(extendedFeats[k + 1].angle - extendedFeats[k].angle) % TWO_PI
                if (dAngRight < checkWithinAngle) {
                    val arcDist = extendedFeats[k + 1].distance * dAngRight
                    val dDist = extendedFeats[k].distance - extendedFeats[k + 1].distance
                    withinAng = true
                    if (dDist / arcDist > maxRatio) {
                        canAddRight = false
                    }
                }

                if ((canAddLeft && canAddRight) || !withinAng) {
                    sepFeats.add(extendedFeats[k])
                }
                k++
            }
        }
        return sepFeats
    }

    private fun splitAndMerge(inputPoints: List<SplitAndMergeFeature>, epsilon: Double): List<SplitAndMergeFeature> {
        var distMax = 0.0
        var distMaxIndex = 0
        val results = ArrayList<SplitAndMergeFeature>(2)

        for (i in 1 until inputPoints.size - 1) {
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
            if (inputPoints.size > 1) {
                inputPoints[0].incrDiscardedPointsCount(inputPoints.size - 2)
            }
            results.add(0, inputPoints[0])
            results.add(1, inputPoints[inputPoints.size - 1])
        }
        return results
    }

    private fun distanceFromLineToPoint(point: Feature, lineStart: Feature, lineEnd: Feature): Double {
        val dX = lineEnd.x - lineStart.x
        val dY = lineEnd.y - lineStart.y

        val numerator = Math.abs((dY * point.x) - (dX * point.y) + (lineEnd.x * lineStart.y) - (lineEnd.y * lineStart.x))
        val denominator = Math.sqrt((dY * dY) + (dX * dX))

        return numerator / denominator
    }
}