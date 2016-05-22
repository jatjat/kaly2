package ca.joelathiessen.kaly2.slam.landmarks

import ags.utils.dataStructures.trees.thirdGenKD.KdTree
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction
import ca.joelathiessen.kaly2.featuredetector.Feature
import lejos.robotics.geometry.Point

class LandmarksTree() : AssociatableLandmarks {
    private lateinit var baseTree: LandmarksTree

    constructor(baseTree: LandmarksTree): this() {
        this.baseTree = baseTree
    }

    fun copy(): LandmarksTree {
        throw UnsupportedOperationException()
    }

    fun markForUpdatingOnCopy(updatedLandmark: Landmark) {
        throw UnsupportedOperationException()
    }

    fun markForInsertionOnCopy(feat: Feature) {
        throw UnsupportedOperationException()
    }

    override fun addLandmark(landmark: Landmark) {
        landmarksTree.addLeafPoint(doubleArrayOf(landmark.x,landmark.y), landmark)
    }

    private val landmarksTree = KdTree<Landmark>(2)

    override fun getNearestNeighbor(point: Point): Landmark {
        val nearestHeap = landmarksTree.findNearestNeighbors(doubleArrayOf(point.x.toDouble(), point.y.toDouble()), 1,
                SquareEuclideanDistanceFunction())

        return nearestHeap.max
    }
}