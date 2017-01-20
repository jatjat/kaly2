package ca.joelathiessen.kaly2.planner

abstract class PathSegment(val x: Double, val y: Double, parentIn: PathSegment?, costIn: Double) : PathSegmentInfo {
    var cost: Double = costIn
        protected set

    var parent: PathSegment? = parentIn
        protected set

    abstract fun makeChild(xChild: Double, yChild: Double): PathSegment?
    abstract fun changeParentIfCheaper(posNewParent: PathSegment)
}
