package ca.joelathiessen.kaly2.subconscious

import ca.joelathiessen.kaly2.Measurement
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.util.FloatMath
import ca.joelathiessen.util.rotate
import ca.joelathiessen.util.array2d
import java.util.*


data class LocalPlan(val angle: Float, val distance: Float, val startX: Float, val startY: Float, val startAngle: Float,
                     val endX: Float, val endY: Float)
class LocalPlanner(val robotSize: Float, val rotStep: Float, val distStep: Float, val gridStep: Float,
                   val gridSize: Float, val staticObstacleSize: Float) {
    private val halfGridSize = gridSize / 2
    private val numSteps = (gridSize / gridStep).toInt()

    private data class GridPoint(val x: Float, val y: Float)

    fun makePlan(staticObstacles: List<Measurement>, startPose: RobotPose, maxRot: Float,
                 maxDist: Float, desiredPath: List<RobotPose>): LocalPlan {
        val searchGrid = array2d<ArrayList<GridPoint>>(numSteps, numSteps, { ArrayList() })
        val checkDist = robotSize + staticObstacleSize

        // fill the search grid with obstacles:
        staticObstacles.forEach {
            val xHalfGrid = it.x + halfGridSize
            val yHalfGrid = it.y + halfGridSize

            val xStartOffset = ((xHalfGrid - checkDist) / gridStep).toInt()
            val xStart = Math.max(xStartOffset, 0)
            val xEndOffset = ((xHalfGrid + checkDist) / gridStep).toInt()
            val xEnd = Math.min(xEndOffset, numSteps)

            val yStartOffset = ((yHalfGrid - checkDist) / gridStep).toInt()
            val yStart = Math.max(yStartOffset, 0)
            val yEndOffset = ((yHalfGrid + checkDist) / gridStep).toInt()
            val yEnd = Math.min(yEndOffset, numSteps)

            for (x in xStart..xEnd) {
                for (y in yStart..yEnd) {
                    searchGrid[x][y].add(GridPoint(it.x, it.y))
                }
            }
        }

        // find the cheapest non-colliding path:
        val heading = startPose.heading
        var curRot = -1 * maxRot
        var minCost = Float.MAX_VALUE
        var bestRot = 0.0f
        var bestDist = 0.0f
        var bestX = 0.0f
        var bestY = 0.0f
        while (curRot < maxRot) {
            var curDist = 0.0f
            var collided = false
            while (curDist < maxDist && collided == false) {

                val result = rotate(curRot, curDist, heading)

                val x = startPose.x + result.deltaX
                val y = startPose.y + result.deltaY
                collided = checkObstacles(searchGrid, x, y)
                if (!collided) {
                    val cost = getCost(x, y, heading + curRot, desiredPath)
                    if (cost < minCost) {
                        bestRot = curRot
                        bestDist = curDist
                        bestX = x
                        bestY = y
                        minCost = cost
                    }
                }
                curDist += distStep
            }
            curRot += rotStep
        }

        if (minCost < Float.MAX_VALUE) {
            return LocalPlan(bestRot, bestDist, startPose.x, startPose.y, startPose.heading, bestX, bestY)
        }
        return LocalPlan(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    }
    
    private fun withinFast(one: Float, two: Float, epsilon: Float): Boolean {
        return FloatMath.abs(two - one) < epsilon
    }

    private fun checkObstacles(searchGrid: Array<Array<ArrayList<GridPoint>>>, x: Float, y: Float): Boolean {
        val gridX = ((x + halfGridSize) / gridStep).toInt()
        val gridY = ((y + halfGridSize) / gridStep).toInt()

        val container = searchGrid[gridX][gridY]
        var found = false
        var count = 0
        while (found == false && count < container.size) {
            val checkDist = robotSize + staticObstacleSize
            if (withinFast(container[count].x, x, checkDist) && withinFast(container[count].y, y, checkDist)) {
                found = true
            }
            count++
        }
        return found
    }

    // TODO: Calculate cost using more of desired path, efficiently
    private fun getCost(x: Float, y: Float, heading: Float, desiredPath: List<RobotPose>): Float {
        val pose = desiredPath.firstOrNull()
        if (pose != null) {
            val deltaXpos = pose.x - x
            val deltaYpos = pose.y - y
            return (deltaXpos * deltaXpos) + (deltaYpos * deltaYpos) + FloatMath.abs(pose.heading - heading)
        }
        return Float.MAX_VALUE
    }
}