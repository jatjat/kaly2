package ca.joelathiessen.kaly2.planner.linear

import ca.joelathiessen.kaly2.map.MapTree
import ca.joelathiessen.kaly2.planner.PathSegment
import ca.joelathiessen.util.FloatMath
import ca.joelathiessen.util.distance
import lejos.robotics.geometry.Line
import java.util.ArrayList

class LinearPathSegment(x: Float, y: Float, parent: PathSegment?, cost: Float) : PathSegment(x, y, parent, cost) {
    val MIN_INCR = 0.0001f // handle 0 sized obstacles

    override fun makeChild(xChild: Float, yChild: Float, obstacles: MapTree, obsSize: Float):
        LinearPathSegment? {

        if (collides(obstacles, obsSize, x, y, xChild, yChild)) {
            return null
        } else {
            val childCost = distance(x, xChild, y, yChild)
            return LinearPathSegment(xChild, yChild, this, childCost)
        }
    }

    override fun changeParentIfCheaper(posNewParent: PathSegment, obstacles: MapTree, obsSize: Float) {
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

    private fun collides(
        obstacles: MapTree,
        obsSize: Float,
        xChild: Float,
        yChild: Float,
        xParent: Float,
        yParent: Float
    ): Boolean {
        val deltaX = xChild - xParent
        val deltaY = yChild - yParent
        val mag = FloatMath.sqrt((deltaX * deltaX) + (deltaY * deltaY))
        val deltaXUnit = deltaX / mag
        val deltaYUnit = deltaY / mag

        var collided = false
        var distIncr = 0f
        while (distIncr < mag && !collided) {
            val xIncr = xParent + deltaXUnit * distIncr
            val yIncr = yParent + deltaYUnit * distIncr
            val nearest = obstacles.getNearestObstacles(xIncr, yIncr)
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