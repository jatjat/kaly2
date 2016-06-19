package ca.joelathiessen.kaly2.slam.landmarks

import ags.utils.dataStructures.BinaryHeap
import ags.utils.dataStructures.MaxHeap
import ags.utils.dataStructures.trees.thirdGenKD.KdTree
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction
import ca.joelathiessen.kaly2.featuredetector.Feature
import lejos.robotics.geometry.Point

class LandmarksTree() : AssociatableLandmarks {

    private var landmarksTree = KdTree<Landmark>(2)

    constructor(landmarksTree: KdTree<Landmark>): this() {
        this.landmarksTree = landmarksTree
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

    override fun getNearestNeighbor(point: Point): Landmark? {
            val nearestHeap = landmarksTree.findNearestNeighbors(doubleArrayOf(point.x.toDouble(), point.y.toDouble()),
                    1, SquareEuclideanDistanceFunction())
        return nearestHeap.max
    }
}