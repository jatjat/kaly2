package ca.joelathiessen.kaly2.featuredetector

import ca.joelathiessen.kaly2.Measurement
import ca.joelathiessen.util.FloatMath
import java.util.*

class SplitAndMerge(val threshold: Float, val checkWithinAngle: Float, val maxRatio: Float) : FeatureDetector {
    private val TWO_PI = 2 * FloatMath.PI

    override fun getFeatures(measurements: List<Measurement>): List<Feature> {
        val features = measurements.map(::SplitAndMergeFeature)

        val smFeats = splitAndMerge(features.sortedBy { it.angle }, threshold)

        val sepFeats = ArrayList<SplitAndMergeFeature>()

        if (smFeats.size > 2) {
            val extendedFeats = arrayListOf(smFeats[smFeats.size - 1]) + smFeats + smFeats[0]

            // trim features that are much deeper than adjacent features:
            var k = 1
            while (k < extendedFeats.size - 1) {
                var canAddLeft = true
                var canAddRight = true
                var withinAng = false

                val dAngLeft = FloatMath.abs(extendedFeats[k].angle - extendedFeats[k - 1].angle) % TWO_PI
                if (dAngLeft < checkWithinAngle) {
                    val arcDist = extendedFeats[k - 1].distance * dAngLeft
                    val dDist = extendedFeats[k].distance - extendedFeats[k - 1].distance
                    withinAng = true
                    if (dDist / arcDist > maxRatio) {
                        canAddLeft = false
                    }
                }

                val dAngRight = FloatMath.abs(extendedFeats[k + 1].angle - extendedFeats[k].angle) % TWO_PI
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

            // trim features that are still within the threshold:
            val expSepFeats = arrayListOf(sepFeats[sepFeats.size - 1]) + sepFeats + sepFeats[0]
            val innerFeats = ArrayList<Feature>()
            for (i in 1 until expSepFeats.size - 1) {
                if(distanceFromLineToPoint(expSepFeats[i], expSepFeats[i-1], expSepFeats[i+1]) > threshold) {
                    innerFeats.add(expSepFeats[i])
                }
            }

            return innerFeats
        }
        return sepFeats
    }

    private fun splitAndMerge(inputPoints: List<SplitAndMergeFeature>, epsilon: Float): List<SplitAndMergeFeature> {
        var distMax = 0.0f
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

    private fun distanceFromLineToPoint(point: Feature, lineStart: Feature, lineEnd: Feature): Float {
        val deltaX = lineEnd.x - lineStart.x
        val deltaY = lineEnd.y - lineStart.y

        val numerator = FloatMath.abs((deltaY * point.x) - (deltaX * point.y) + (lineEnd.x * lineStart.y) - (lineEnd.y * lineStart.x))
        val denominator = FloatMath.sqrt((deltaY * deltaY) + (deltaX * deltaX))

        return numerator / denominator
    }
}