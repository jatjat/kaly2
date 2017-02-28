package ca.joelathiessen.kaly2.featuredetector

import ca.joelathiessen.kaly2.Measurement

class SplitAndMergeFeature(sensorX: Float, sensorY: Float, distance: Float, angle: Float, dX: Float, dY: Float,
                           stdDev: Float = 0.0f) : Feature(sensorX, sensorY, distance, angle, dX, dY, stdDev) {

    constructor(measurement: Measurement): this(measurement.probPose.x, measurement.probPose.y, measurement.distance,
            measurement.probAngle, measurement.dX, measurement.dY)

    var discardedPoints: Int = 0// the number of points along the line starting at this point that were discarded during the split and merge
        private set

    fun incrDiscardedPointsCount(inc: Int) {
        discardedPoints += Math.max(inc, 0)
    }
}
