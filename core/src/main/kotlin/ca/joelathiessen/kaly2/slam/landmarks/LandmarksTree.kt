package ca.joelathiessen.kaly2.slam.landmarks

import ags.utils.dataStructures.trees.thirdGenKD.KdTree
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction
import lejos.robotics.geometry.Point
import java.util.HashSet

class LandmarksTree() : AssociatableLandmarks {
    private val DIMENSIONS = 2
    private val BUCKET_CAPACITY = 24
    private val sqEucDistFunc = SquareEuclideanDistanceFunction()

    private var kdTree = KdTree<Landmark>(DIMENSIONS, BUCKET_CAPACITY)
    private var list = ArrayList<Landmark>(BUCKET_CAPACITY)

    private val landmarksForInsert = HashSet<Landmark>()
    private val landmarksForDelete = HashSet<Landmark>()
    private var kdTreeCopy: KdTree<Landmark>? = null

    constructor(landmarksTree: KdTree<Landmark>) : this() {
        this.kdTree = landmarksTree
    }

    fun copy(): LandmarksTree {
        if (kdTreeCopy == null) {
            var newTree = kdTree

            landmarksForDelete.forEach {
                newTree = newTree.removePointAsCopy(doubleArrayOf(it.x.toDouble(), it.y.toDouble()), it)
            }
            landmarksForInsert.forEach {
                newTree = newTree.addPointAsCopy(doubleArrayOf(it.x.toDouble(), it.y.toDouble()), it)
            }
            landmarksForInsert.clear()
            landmarksForDelete.clear()
            kdTreeCopy = newTree
        }
        return LandmarksTree(kdTreeCopy!!)
    }

    fun markForUpdateOnCopy(newLandmark: Landmark, oldLandmark: Landmark) {
        landmarksForInsert += newLandmark
        landmarksForDelete += oldLandmark
    }

    fun markForInsertOnCopy(landmark: Landmark) {
        landmarksForInsert += landmark
        list.add(landmark)
    }

    override fun getNearestNeighbor(point: Point): Landmark? {
        var nearest: Landmark? = null
        if (kdTree.size() > 0) {
            nearest = kdTree.findNearestNeighbors(doubleArrayOf(point.x.toDouble(), point.y.toDouble()),
                1, sqEucDistFunc).max
        }
        return nearest
    }

    fun getList(): List<Landmark> {
        return list
    }
}