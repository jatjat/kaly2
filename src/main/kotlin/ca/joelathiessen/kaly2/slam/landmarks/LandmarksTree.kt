package ca.joelathiessen.kaly2.slam.landmarks

import ags.utils.dataStructures.trees.thirdGenKD.KdTree
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction
import lejos.robotics.geometry.Point

class LandmarksTree() : AssociatableLandmarks {
    override fun addLandmark(landmark: Landmark) {
        landmarksTree.addLeafPoint(doubleArrayOf(landmark.x.toDouble(),landmark.y.toDouble()), landmark)
    }

    private val landmarksTree = KdTree<Landmark>(2)

    override fun getNearestNeighbor(point: Point): Landmark {
        val nearestHeap = landmarksTree.findNearestNeighbors(doubleArrayOf(point.x.toDouble(), point.y.toDouble()), 1, SquareEuclideanDistanceFunction())

        return nearestHeap.max
    }
}