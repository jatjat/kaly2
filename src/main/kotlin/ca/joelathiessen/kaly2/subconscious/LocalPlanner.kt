package ca.joelathiessen.kaly2.subconscious

import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.util.EPSILON
import ca.joelathiessen.util.array2d
import java.util.*


data class LocalPlan(val angle: Double, val distance: Double)
class LocalPlanner(val robotSize: Double, val rotStep: Double, val distStep: Double, val gridStep: Double,
                   val gridSize: Double, val staticObstacleSize: Double) {
    private val halfGridSize = gridSize / 2
    private val numSteps = (gridSize / gridStep).toInt()

    private data class GridPoint(val x: Double, val y: Double)

    fun makePlan(staticObstacles: ArrayList<Pair<Double, Double>>, startPose: RobotPose, maxRot: Double,
                 maxDist: Double, desiredPath: List<RobotPose>): LocalPlan {
        val searchGrid = array2d<ArrayList<GridPoint>>(numSteps, numSteps, { ArrayList() })
        val checkDist = robotSize + staticObstacleSize

        // fill the search grid with obstacles:
        staticObstacles.forEach {
            val xHalfGrid = it.first + halfGridSize
            val yHalfGrid = it.second + halfGridSize

            val xStartOffset = ((xHalfGrid - checkDist) % gridStep).toInt()
            val xStart = Math.max(xStartOffset, 0)
            val xEndOffset = ((xHalfGrid + checkDist) % gridStep).toInt()
            val xEnd = Math.min(xEndOffset, numSteps)

            val yStartOffset = ((yHalfGrid - checkDist) % gridStep).toInt()
            val yStart = Math.max(yStartOffset, 0)
            val yEndOffset = ((yHalfGrid + checkDist) % gridStep).toInt()
            val yEnd = Math.min(yEndOffset, numSteps)

            for (x in xStart until xEnd) {
                for (y in yStart until yEnd) {
                    searchGrid[x][y].add(GridPoint(it.first, it.second))
                }
            }
        }

        // find the cheapest non-colliding path:
        val heading = startPose.heading.toDouble()
        var curRot = -1 * maxRot
        var minCost = Double.MAX_VALUE
        var bestRot = 0.0
        var bestDist = 0.0
        while (curRot < maxRot) {
            var curDist = 0.0
            var collided = false
            while (curDist < maxDist && collided == false) {

                val result = RotateResult(0.0, 0.0)
                rotate(curRot, curDist, heading, result)

                val x = startPose.x + result.dx
                val y = startPose.y + result.dy
                collided = checkObstacles(searchGrid, x, y)
                if (!collided) {
                    val cost = getCost(x, y, heading + curRot, desiredPath)
                    if (cost < minCost) {
                        bestRot = curRot
                        bestDist = curDist
                        minCost = cost
                    }
                }
                curDist += distStep
            }
            curRot += rotStep
        }

        if (minCost < Double.MAX_VALUE) {
            return LocalPlan(bestRot, bestDist)
        }
        return LocalPlan(0.0, 0.0)
    }

    data class RotateResult(var dx: Double, var dy: Double)

    private fun rotate(turnAngle: Double, distance: Double, heading: Double, result: RotateResult) {
        val radius = distance / turnAngle
        var dy = 0.0
        var dx = 0.0
        if (Math.abs(turnAngle) > EPSILON) {
            dy = radius * (Math.cos(heading) - Math.cos(heading + turnAngle))
            dx = radius * (Math.sin(heading + turnAngle) - Math.sin(heading))
        } else if (Math.abs(distance) > EPSILON) {
            dx = distance * Math.cos(heading)
            dy = distance * Math.sin(heading)
        }
        result.dx = dx
        result.dy = dy
    }

    private fun within(one: Double, two: Double, epsilon: Double): Boolean {
        return Math.abs(two - one) < epsilon
    }

    private fun checkObstacles(searchGrid: Array<Array<ArrayList<GridPoint>>>, x: Double, y: Double): Boolean {
        val gridX = ((x + halfGridSize) % gridStep).toInt()
        val gridY = ((y + halfGridSize) % gridStep).toInt()

        val container = searchGrid[gridX][gridY]
        var found = false
        var count = 0
        while (found == false && count < container.size) {
            val checkDist = robotSize + staticObstacleSize
            if (within(container[count].x, x, checkDist) && within(container[count].y, y, checkDist)) {
                found = true
            }
            count++
        }
        return found
    }

    // TODO: Calculate cost using more of desired path, efficiently
    private fun getCost(x: Double, y: Double, heading: Double, desiredPath: List<RobotPose>): Double {
        val pose = desiredPath.firstOrNull()
        if (pose != null) {
            val dXpos = pose.x - x
            val dYpos = pose.y - y
            return (dXpos * dXpos) + (dYpos * dYpos) + Math.abs(pose.heading - heading)
        }
        return Double.MAX_VALUE
    }
}