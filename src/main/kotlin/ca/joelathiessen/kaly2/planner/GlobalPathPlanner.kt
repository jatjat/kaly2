package ca.joelathiessen.kaly2.planner

import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.util.FloatMath
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
                        private val obsSize: Float, private val searchDist: Float, private val stepDist: Float,
                        private val startPose: RobotPose, private val endPose: RobotPose) {
    val paths: ArrayList<PathSegmentInfo>
        get() {
            return ArrayList(pathList)
        }

    private val PROB_REWIRE_ROOT = 0.01
    private val rand = Random(0)
    private val pathTree = GenTree<PathSegment>()
    private val rootNode = pathFactory.makePathSegmentRoot(startPose)
    private val pathList = ArrayList<PathSegment>()
    private val doubleSearchDist = 2 * searchDist
    private val searchArea = doubleSearchDist * doubleSearchDist
    private val searchXBase = startPose.x - searchDist
    private val searchYBase = startPose.y - searchDist
    private val gamma = 6 * searchArea // assume obstacles reduce the search area negligibly
    private var nodeCount = 1f

    init {
        pathTree.add(rootNode.x, rootNode.y, rootNode)
    }

    fun iterate(numItrs: Int) {
        for (i in 0 until numItrs) {
            val xSearch = searchXBase + (rand.nextFloat() * doubleSearchDist)
            val ySearch = searchYBase + (rand.nextFloat() * doubleSearchDist)
            val nearestNeighbors = pathTree.getNearestNeighbors(xSearch, ySearch)

            if (nearestNeighbors.hasNext()) {
                val nearest = nearestNeighbors.next()
                val ang = FloatMath.atan2(ySearch - nearest.y, xSearch - nearest.x)
                val xInter = nearest.x + (FloatMath.cos(ang) * stepDist)
                val yInter = nearest.y + (FloatMath.sin(ang) * stepDist)

                val newNode = nearest.makeChild(xInter, yInter, obstacles, obsSize)
                if (newNode != null) {
                    pathTree.add(xInter, yInter, newNode)
                    pathList.add(newNode)
                    nodeCount++
                    val searchRadius = gamma * FloatMath.pow(FloatMath.log(nodeCount) / nodeCount, 0.5f)
                    rewire(newNode, pathTree, searchRadius)

                    // Make sure the root of the tree is optimal:
                    if (rand.nextFloat() < PROB_REWIRE_ROOT) {
                        rewire(rootNode, pathTree, searchRadius)
                    }

                }
            }
        }
    }

    private fun rewire(newNode: PathSegment, pathTree: GenTree<PathSegment>, searchRadius: Float) {
        val nearby = pathTree.getNearestNeighbors(newNode.x, newNode.y)
        var cont = true
        while (cont && nearby.hasNext()) {
            val nextNearest = nearby.next()
            if (newNode != nextNearest && nextNearest != rootNode) {
                val dist = distance(newNode.x, nextNearest.x, newNode.y, nextNearest.y)
                if (dist < searchRadius) {
                    nextNearest.changeParentIfCheaper(newNode, obstacles, obsSize)
                } else {
                    cont = false
                }
            }
        }
    }

    fun getManeuvers(): List<RobotPose> {
        val poses = ArrayList<RobotPose>()
        var angle = startPose.heading
        val nearestToLast = pathTree.getNearestNeighbors(endPose.x, endPose.y)
        if (nearestToLast.hasNext()) {
            val nearest = nearestToLast.next()
            var cur: PathSegment? = nearest
            while (cur != null) {
                val parent = cur.parent
                if (parent != null) {
                    angle = FloatMath.atan2(cur.y - parent.y, cur.x - parent.x)
                }
                val newPose = RobotPose(0, 0f, cur.x, cur.y, angle)
                poses.add(0, newPose)
                cur = cur.parent
            }
        }
        return poses
    }
}
