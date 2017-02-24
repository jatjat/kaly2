package ca.joelathiessen.kaly2.planner.linear

import ca.joelathiessen.kaly2.planner.PathSegment
import ca.joelathiessen.util.FloatMath
import ca.joelathiessen.util.GenTree
import ca.joelathiessen.util.distance
import lejos.robotics.geometry.Line
import lejos.robotics.geometry.Point
import java.util.*

class LinearPathSegment(x: Float, y: Float, parent: PathSegment?, cost: Float) : PathSegment(x, y, parent, cost) {
    val MIN_INCR = 0.0001f // handle 0 sized obstacles

    override fun makeChild(xChild: Float, yChild: Float, obstacles: GenTree<Point>, obsSize: Float):
            LinearPathSegment? {

        if (collides(obstacles, obsSize, x, y, xChild, yChild)) {
            return null
        } else {
            val childCost = distance(x, xChild, y, yChild)
            return LinearPathSegment(xChild, yChild, this, childCost)
        }
    }

    override fun changeParentIfCheaper(posNewParent: PathSegment, obstacles: GenTree<Point>, obsSize: Float) {
        var curTotalCost = 0f
        var curSeg: PathSegment? = this
        while (curSeg != null) {
            curTotalCost += curSeg.cost
            curSeg = curSeg.parent
        }
        var baseCost = 0f
        var baseSeg: PathSegment? = posNewParent
        while (baseSeg != null) {
            baseCost += baseSeg.cost
            baseSeg = baseSeg.parent
        }
        val posNewParentCost = distance(x, posNewParent.x, y, posNewParent.y)
        val posNewTotalCost = baseCost + posNewParentCost

        if (posNewTotalCost < curTotalCost) {
            if (!collides(obstacles, obsSize, posNewParent.x, posNewParent.y, x, y)) {
                parent = posNewParent
                cost = posNewParentCost
            }
        }
    }

    override fun getLines(): List<Line> {
        val lines = ArrayList<Line>()
        val oldX = parent?.x ?: x
        val oldY = parent?.y ?: y
        lines.add(Line(oldX, oldY, x, y))
        return lines
    }

    private fun collides(obstacles: GenTree<Point>, obsSize: Float, xChild: Float, yChild: Float,
                         xParent: Float, yParent: Float): Boolean {
        val dX = xChild - xParent
        val dY = yChild - yParent
        val mag = FloatMath.sqrt((dX * dX) + (dY * dY))
        val dXUnit = dX / mag
        val dYUnit = dY / mag

        var collided = false
        var distIncr = 0f
        while (distIncr < mag && !collided) {
            val xIncr = xParent + dXUnit * distIncr
            val yIncr = yParent + dYUnit * distIncr
            val nearest = obstacles.getNearestNeighbors(xIncr, yIncr)
            if (nearest.hasNext()) {
                val next = nearest.next()
                val dist = distance(next.x, xIncr, next.y, yIncr)
                if (dist < obsSize) {
                    collided = true
                } else {
                    distIncr += Math.max(dist, MIN_INCR)
                }
            } else {
                break
            }
        }
        return collided
    }
}