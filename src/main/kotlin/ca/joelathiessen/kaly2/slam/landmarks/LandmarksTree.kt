package ca.joelathiessen.kaly2.slam.landmarks

import ags.utils.dataStructures.trees.thirdGenKD.KdTree
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction
import lejos.robotics.geometry.Point
import java.util.*

class LandmarksTree() : AssociatableLandmarks {

    private var kdTree = KdTree<Landmark>(2)
    private val landmarksForInsert = ArrayList<Landmark>()
    private val landmarksForDelete = ArrayList<Landmark>()

    constructor(landmarksTree: KdTree<Landmark>): this() {
        this.kdTree = landmarksTree
    }

    fun copy(): LandmarksTree {
        var newTree = kdTree
        landmarksForInsert.forEach { newTree = newTree.addPointAsCopy(doubleArrayOf(it.x, it.y), it) }
        landmarksForDelete.forEach { newTree.removePoint(doubleArrayOf(it.x, it.y), it) }
        return LandmarksTree(newTree)
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