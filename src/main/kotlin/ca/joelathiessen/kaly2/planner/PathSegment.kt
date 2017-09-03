package ca.joelathiessen.kaly2.planner

import ca.joelathiessen.util.GenTree
import lejos.robotics.geometry.Point

abstract class PathSegment(val x: Float, val y: Float, parentIn: PathSegment?, costIn: Float) : PathSegmentInfo {
    var cost: Float = costIn
        protected set

    var parent: PathSegment? = parentIn
        protected set

    abstract fun makeChild(xChild: Float, yChild: Float, obstacles: GenTree<Point> = GenTree(),
        obsSize: Float = 0f): PathSegment?

    abstract fun changeParentIfCheaper(posNewParent: PathSegment, obstacles: GenTree<Point> = GenTree(),
        obsSize: Float = 0f)
}
