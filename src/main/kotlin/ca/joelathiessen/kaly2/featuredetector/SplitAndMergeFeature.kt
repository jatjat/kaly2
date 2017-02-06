package ca.joelathiessen.kaly2.featuredetector

class SplitAndMergeFeature(sensorX: Double, sensorY: Double, distance: Double, angle: Double, stdDev: Double = 0.0) :
        Feature(sensorX, sensorY, distance, angle, stdDev) {

    var discardedPoints: Int = 0// the number of points along the line starting at this point that were discarded during the split and merge
        private set

    fun incrDiscardedPointsCount(inc: Int) {
        discardedPoints += Math.max(inc, 0)
    }
}
