package ca.joelathiessen.kaly2.map

import ca.joelathiessen.kaly2.Measurement
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.util.FloatMath
import ca.joelathiessen.util.GenTree
import ca.joelathiessen.util.distance
import lejos.robotics.geometry.Point
import java.util.*

class GlobalMap(private val stepDist: Float, private val obsSize: Float, private val removeInvalidObsInterval: Int) {
    var obstacles = GenTree<Point>()
        private set

    private var obstacleList = ArrayList<Point>()
    private var shouldRemove = HashSet<Point>()

    private var numCalled = 0

    fun incorporateMeasurements(measurements: List<Measurement>, improvedPose: RobotPose) {
        val mesObstacles = makeObstacles(improvedPose, measurements)

        // The map should have no obstacles between the sensor and the detected obstacle:
        mesObstacles.forEach { shouldRemove.addAll(getObstaclesUpToMes(improvedPose, it, obstacles)) }

        // It's expensive to always recreate the obstacle tree with removed obstacles:
        if (numCalled > 0 && numCalled % removeInvalidObsInterval == 0) {
            val newObstacles = GenTree<Point>()
            val newObstacleList = ArrayList<Point>()
            obstacleList.forEach {
                if (shouldRemove.contains(it) == false) {
                    newObstacles.add(it.x, it.y, it)
                    newObstacleList.add(it)
                }
            }
            obstacles = newObstacles
            obstacleList = newObstacleList
            shouldRemove = HashSet()
        }

        mesObstacles.forEach {
            obstacles.add(it.x, it.y, it)
            obstacleList.add(it)
        }

        numCalled++
    }

    private fun makeObstacles(improvedPose: RobotPose, measurements: List<Measurement>):
            List<Point> {
        return measurements.map {
            val improvedAngle = improvedPose.heading - it.odoPose.heading + it.probAngle
            val improvedMesX = FloatMath.sin(improvedAngle) * it.distance
            val improvedMesY = FloatMath.sin(improvedAngle) * it.distance

            Point(improvedMesX, improvedMesY)
        }
    }

    private fun getObstaclesUpToMes(improvedPose: RobotPose, mesObs: Point, obstacles: GenTree<Point>):
            HashSet<Point> {
        val obstaclesUpToMes = HashSet<Point>()

        val spanDeltaX = mesObs.x - improvedPose.x
        val spanDeltaY = mesObs.y - improvedPose.y
        val spanDist = distance(mesObs.x, improvedPose.x, mesObs.y, improvedPose.y)

        val endT = 1f - Math.min(1f, obsSize / spanDist)
        var t = 0f
        while (t < endT) {
            val curX = improvedPose.x + (t * spanDeltaX)
            val curY = improvedPose.y + (t * spanDeltaY)

            val nextObstacles = obstacles.getNearestNeighbors(curX, curY)
            var maxDist = 0f
            var shouldCont = true
            while (nextObstacles.hasNext() && shouldCont) {
                val nextObs = nextObstacles.next()
                val distToObs = distance(nextObs.x, curX, nextObs.y, curY)
                if (distToObs < obsSize) {
                    obstaclesUpToMes.add(nextObs)
                } else {
                    shouldCont = false
                }
                if (distToObs > maxDist) {
                    maxDist = distToObs
                }
            }

            t += Math.max(stepDist, maxDist / spanDist)
        }
        return obstaclesUpToMes
    }
}