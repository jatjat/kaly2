package ca.joelathiessen.kaly2.planner

import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.util.GenTree
import ca.joelathiessen.util.distance
import lejos.robotics.geometry.Point
import java.util.*

/**
 * See:
 * Karaman, Sertac, and Emilio Frazzoli.
 * "Incremental sampling-based algorithms for optimal motion planning."
 * Robotics Science and Systems VI 104 (2010).
 **/
class GlobalPathPlanner(private val pathFactory: PathSegmentRootFactory, private val obstacles: GenTree<Point>,
                        private val obsSize: Double, private val searchDist: Double, private val stepDist: Double,
                        private val startPose: RobotPose, private val endPose: RobotPose) {
    val paths: ArrayList<PathSegmentInfo>
        get() {
            return ArrayList(pathList)
        }

    private val PROB_REWIRE_ROOT = 0.001
    private val rand = Random(0)
    private val pathTree = GenTree<PathSegment>()
    private val rootNode = pathFactory.makePathSegmentRoot(startPose)
    private val pathList = ArrayList<PathSegment>()
    private val doubleSearchDist = 2 * searchDist
    private val searchArea = doubleSearchDist * doubleSearchDist
    private val searchXBase = startPose.x - searchDist
    private val searchYBase = startPose.y - searchDist
    private val gamma = 6 * searchArea // assume obstacles reduce the search area negligibly
    private var nodeCount = 1

    init {
        pathTree.add(rootNode.x, rootNode.y, rootNode)
    }

    fun iterate(numItrs: Int) {
        for (i in 0 until numItrs) {
            val xSearch = searchXBase + (rand.nextDouble() * doubleSearchDist)
            val ySearch = searchYBase + (rand.nextDouble() * doubleSearchDist)
            val nearestNeighbors = pathTree.getNearestNeighbors(xSearch, ySearch)

            if (nearestNeighbors.hasNext()) {
                val nearest = nearestNeighbors.next()
                val ang = Math.atan2(ySearch - nearest.y, xSearch - nearest.x)
                val xInter = nearest.x + (Math.cos(ang) * stepDist)
                val yInter = nearest.y + (Math.sin(ang) * stepDist)

                val newNode = nearest.makeChild(xInter, yInter)
                if (newNode != null) {
                    pathTree.add(xInter, yInter, newNode)
                    pathList.add(newNode)
                    nodeCount++
                    val searchRadius = gamma * Math.pow(Math.log(nodeCount.toDouble()) / nodeCount, 0.5)
                    rewire(newNode, pathTree, searchRadius)

                    // Make sure the root of the tree is optimal:
                    if (rand.nextFloat() < PROB_REWIRE_ROOT) {
                        rewire(rootNode, pathTree, searchRadius)
                    }

                }
            }
        }
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

    fun getManeuvers(): List<RobotPose> {
        val poses = ArrayList<RobotPose>()
        var angle = startPose.heading
        val nearestToLast = pathTree.getNearestNeighbors(endPose.x.toDouble(), endPose.y.toDouble())
        if (nearestToLast.hasNext()) {
            val nearest = nearestToLast.next()
            var cur: PathSegment? = nearest
            while (cur != null) {
                val parent: PathSegment? = cur.parent
                if (parent != null) {
                    angle = Math.atan2(cur.y - parent.y, cur.x - parent.x).toFloat()
                }
                val newPose = RobotPose(0, 0f, cur.x.toFloat(), cur.y.toFloat(), angle)
                poses.add(0, newPose)
                cur = cur.parent
            }
        }
        return poses
    }
}