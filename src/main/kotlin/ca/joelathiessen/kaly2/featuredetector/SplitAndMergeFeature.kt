package ca.joelathiessen.kaly2.featuredetector

class SplitAndMergeFeature(x: Float, y: Float, stdDev: Float = 0f) : Feature(x, y, stdDev) {

    private var discardedPoints: Int = 0// the number of points along the line starting at this point that were discarded during the split and merge

    fun incrDiscardedPointsCount(inc: Int) {
        discardedPoints += Math.max(inc, 0)
    }
}
