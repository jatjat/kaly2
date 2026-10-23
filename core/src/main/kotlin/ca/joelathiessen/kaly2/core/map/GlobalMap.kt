package ca.joelathiessen.kaly2.core.map

import ca.joelathiessen.kaly2.core.Measurement
import ca.joelathiessen.kaly2.core.odometry.RobotPose
import ca.joelathiessen.util.FloatMath
import ca.joelathiessen.util.distance
import lejos.robotics.geometry.Point
import java.util.ArrayList
import java.util.HashSet

class GlobalMap(private val stepDist: Float, private val obsSize: Float, private val removeInvalidObsInterval: Int) {
    var obstacleTree = MapTree()
        private set
    private var internalObstacleList = ArrayList<Point>()
    var obstacleList = ArrayList(internalObstacleList)
        get() { return ArrayList(internalObstacleList) }
        private set

    private var shouldRemove = HashSet<Point>()
    private var numCalled = 0

    fun incorporateMeasurements(measurements: List<Measurement>, improvedPose: RobotPose) {
        val mesObstacles = makeObstacles(improvedPose, measurements)
        return incorporateObstacles(mesObstacles, improvedPose)
    }

    private fun makeObstacles(improvedPose: RobotPose, measurements: List<Measurement>): List<Point> {
        return measurements.map {
            val improvedAngle = improvedPose.heading - it.probPose.heading + it.probAngle
            val improvedMesX = improvedPose.x + FloatMath.cos(improvedAngle) * it.distance
            val improvedMesY = improvedPose.y + FloatMath.sin(improvedAngle) * it.distance

            Point(improvedMesX, improvedMesY)
        }
    }

    private fun incorporateObstacles(newObstacles: List<Point>, improvedPose: RobotPose) {
        var newObsSize = 0
        // The map should have no obstacles between the sensor and the detected obstacle:
        newObstacles.forEach {
            shouldRemove.addAll(getObstaclesUpToMes(improvedPose, it, obstacleTree))
            newObsSize++
        }

        // It's expensive to recreate the obstacle tree:
        if (numCalled > 0 && numCalled % removeInvalidObsInterval == 0) {
            val nextObstacles = MapTree()
            val nextObstacleList = ArrayList<Point>()
            internalObstacleList.forEach {
                if (shouldRemove.contains(it) == false) {
                    nextObstacles.add(it)
                    nextObstacleList.add(it)
                }
            }
            newObstacles.forEach {
                nextObstacles.add(it)
                nextObstacleList.add(it)
            }

            obstacleTree = nextObstacles
            internalObstacleList = nextObstacleList
            shouldRemove = HashSet()
        } else {
            newObstacles.forEach {
                obstacleTree = obstacleTree.addAsCopy(it)
                internalObstacleList.add(it)
            }
        }
        numCalled++
    }

    private fun getObstaclesUpToMes(improvedPose: RobotPose, mesObs: Point, obstacles: MapTree):
        HashSet<Point> {
        val obstaclesUpToMes = HashSet<Point>()

        val spanDeltaX = mesObs.x - improvedPose.x
        val spanDeltaY = mesObs.y - improvedPose.y
        val spanDist = distance(mesObs.x, improvedPose.x, mesObs.y, improvedPose.y)

        val stepT = stepDist / spanDist
        val endT = 1f - (obsSize / spanDist)
        var t = 0f
        while (t < endT) {
            val curX = improvedPose.x + (t * spanDeltaX)
            val curY = improvedPose.y + (t * spanDeltaY)

            val nextObstacles = obstacles.getNearestObstacles(curX, curY)
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

            t += Math.max(stepT, maxDist / spanDist)
        }
        return obstaclesUpToMes
    }
}