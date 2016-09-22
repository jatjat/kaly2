package ca.joelathiessen.kaly2.slam.landmarks

import ags.utils.dataStructures.trees.thirdGenKD.KdTree
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction
import lejos.robotics.geometry.Point
import java.util.*

class LandmarksTree() : AssociatableLandmarks {

    private var kdTree = KdTree<Landmark>(2)
    private val landmarksForInsert = ArrayList<Landmark>()
    private val landmarksForDelete = ArrayList<Landmark>()
    private var newLandmarksTree: LandmarksTree? = null

    constructor(landmarksTree: KdTree<Landmark>): this() {
        this.kdTree = landmarksTree
    }

    fun copy(): LandmarksTree {
        if(landmarksForInsert.size > 0 || landmarksForDelete.size > 0 || newLandmarksTree == null) {
            var newTree = kdTree
            landmarksForInsert.forEach { newTree = newTree.addPointAsCopy(doubleArrayOf(it.x, it.y), it) }
            landmarksForDelete.forEach { newTree.removePoint(doubleArrayOf(it.x, it.y), it) }
            landmarksForInsert.clear()
            landmarksForDelete.clear()
            newLandmarksTree = LandmarksTree(newTree)
        }
        return newLandmarksTree!!
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