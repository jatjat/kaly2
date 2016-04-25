package ca.joelathiessen.kaly2.featuredetector

class SplitAndMergeFeature(override val x: Float, override val y: Float) : Feature {
    override var stdDev: Float = 0.toFloat()

    private var discardedPoints: Int = 0// the number of points along the line starting at this point that were discarded during the split and merge

    fun incrDiscardedPointsCount(inc: Int) {
        discardedPoints += Math.max(inc, 0)
    }
}
