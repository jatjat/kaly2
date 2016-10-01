package ca.joelathiessen.kaly2.slam.landmarks

import ags.utils.dataStructures.trees.thirdGenKD.KdTree
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction
import lejos.robotics.geometry.Point
import java.util.*

class LandmarksTree() : AssociatableLandmarks {
    private val DIMENSIONS = 2
    private val BUCKET_CAPACITY = 24

    private var kdTree = KdTree<Landmark>(DIMENSIONS, BUCKET_CAPACITY)
    private val landmarksForInsert = HashSet<Landmark>()
    private val landmarksForDelete = HashSet<Landmark>()
    private var kdTreeCopy: KdTree<Landmark>? = null

    constructor(landmarksTree: KdTree<Landmark>): this() {
        this.kdTree = landmarksTree
    }

    fun copy(): LandmarksTree {
        if(kdTreeCopy == null) {
            var newTree = kdTree

            landmarksForDelete.forEach { newTree = newTree.removePointAsCopy(doubleArrayOf(it.x, it.y), it) }
            landmarksForInsert.forEach { newTree = newTree.addPointAsCopy(doubleArrayOf(it.x, it.y), it) }
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
    }

    override fun getNearestNeighbor(point: Point): Landmark? {
        var nearest: Landmark? = null
        if(kdTree.size() > 0) {
            nearest = kdTree.findNearestNeighbors(doubleArrayOf(point.x.toDouble(), point.y.toDouble()),
                    1, SquareEuclideanDistanceFunction()).max
        }
        return nearest
    }
}