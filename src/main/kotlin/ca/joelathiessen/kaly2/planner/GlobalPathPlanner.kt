package ca.joelathiessen.kaly2.planner

import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.util.GenTree
import ca.joelathiessen.util.distance
import lejos.robotics.geometry.Point
import java.util.*

// See:
// Karaman, Sertac, and Emilio Frazzoli.
// "Incremental sampling-based algorithms for optimal motion planning."
// Robotics Science and Systems VI 104 (2010).
class GlobalPathPlanner(val pathFactory: PathSegmentRootFactory) {
    private val rand = Random()

    fun getPath(obstacles: GenTree<Point>, obsSize: Double, searchDist: Double, stepDist: Double,
                startPose: RobotPose, endPose: RobotPose, numItrs: Int): List<RobotPose> {
        val pathTree = GenTree<PathSegment>()
        val rootNode = pathFactory.makePathSegmentRoot(startPose)
        pathTree.add(rootNode.x, rootNode.y, rootNode)

        val doubleSearchDist = 2 * searchDist
        val searchArea = doubleSearchDist * doubleSearchDist
        val searchXBase = startPose.x - searchDist
        val searchYBase = startPose.y - searchDist

        val gamma = 6 * searchArea // assume obstacles reduce the search area negligibly

        var nodeCount = 1

        for (i in 0 until numItrs) {
            val xSearch = searchXBase + (rand.nextDouble() * doubleSearchDist)
            val ySearch = searchYBase + (rand.nextDouble() * doubleSearchDist)
            val nearestNeighbors = pathTree.getNearestNeighbors(xSearch, ySearch)

            if (nearestNeighbors.hasNext()) {
                val nearest = nearestNeighbors.next()
                val ang = Math.atan2(xSearch - nearest.x, ySearch - nearest.y)
                val xInter = nearest.x + Math.cos(ang) * stepDist
                val yInter = nearest.y + Math.sin(ang) * stepDist

                val newNode = nearest.makeChild(xInter, yInter)
                if (newNode != null) {
                    pathTree.add(xInter, yInter, newNode)
                    nodeCount++
                    val searchRadius = gamma * Math.pow(Math.log(nodeCount.toDouble()) / nodeCount, 0.5)
                    rewire(newNode, pathTree, searchRadius)
                }
            }
        }
        return getManeuvers(startPose, endPose, pathTree, obsSize)
    }

    private fun rewire(newNode: PathSegment, pathTree: GenTree<PathSegment>, searchRadius: Double) {
        val nearby = pathTree.getNearestNeighbors(newNode.x, newNode.y)
        var cont = true
        while (cont && nearby.hasNext()) {
            val nextNearest = nearby.next()
            if (newNode != nextNearest && nextNearest.parent != null) {
                val dist = distance(newNode.x, nextNearest.x, newNode.y, nextNearest.y)
                if (dist < searchRadius) {
                    nextNearest.changeParentIfCheaper(newNode)
                } else {
                    cont = false
                }
            }
        }
    }

    private fun getManeuvers(startPose: RobotPose, endPose: RobotPose, pathTree: GenTree<PathSegment>,
                             obsSize: Double): List<RobotPose> {
        val poses = ArrayList<RobotPose>()
        val endX = endPose.x.toDouble()
        val endY = endPose.y.toDouble()
        var angle = startPose.heading
        val nearestToLast = pathTree.getNearestNeighbors(endX, endY)
        if (nearestToLast.hasNext()) {
            val nearest = nearestToLast.next()
            var cur: PathSegment? = nearest
            while (cur != null) {
                val parent: PathSegment? = cur.parent
                if (parent != null) {
                    angle = Math.atan2(cur.x - parent.x, cur.x - parent.x).toFloat()
                }
                val newPose = RobotPose(0, 0f, cur.x.toFloat(), cur.y.toFloat(), angle)
                poses.add(0, newPose)
                cur = cur.parent
            }
        }
        return poses
    }
}
