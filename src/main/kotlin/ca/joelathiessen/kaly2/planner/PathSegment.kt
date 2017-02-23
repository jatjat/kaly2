package ca.joelathiessen.kaly2.planner

abstract class PathSegment(val x: Float, val y: Float, parentIn: PathSegment?, costIn: Float) : PathSegmentInfo {
    var cost: Float = costIn
        protected set

    var parent: PathSegment? = parentIn
        protected set

    abstract fun makeChild(xChild: Float, yChild: Float): PathSegment?
    abstract fun changeParentIfCheaper(posNewParent: PathSegment)
}
