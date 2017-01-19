package ca.joelathiessen.kaly2.planner.linear

import ca.joelathiessen.kaly2.planner.PathSegment
import ca.joelathiessen.util.distance

class LinearPathSegment(x: Double, y: Double, parent: PathSegment?, cost: Double) : PathSegment(x, y, parent, cost) {
    override fun makeChild(xChild: Double, yChild: Double): LinearPathSegment {
        val childCost = cost + distance(x, xChild, y, yChild)
        return LinearPathSegment(xChild, yChild, this, childCost)
    }

    override fun changeParentIfCheaper(posNewParent: PathSegment) {
        val tryCost = posNewParent.cost + distance(x, posNewParent.x, y, posNewParent.y)
        if (tryCost < cost) {
            parent = posNewParent
            cost = tryCost
        }
    }
}