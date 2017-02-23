package ca.joelathiessen.util

import ags.utils.dataStructures.trees.thirdGenKD.KdTree
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction
import lejos.robotics.geometry.Point

// A general 2D tree
class GenTree<T> {
    private val DIMENSIONS = 2
    private val BUCKET_CAPACITY = 24
    private val kdTree = KdTree<T>(DIMENSIONS, BUCKET_CAPACITY)
    private val sqEucDistFunc = SquareEuclideanDistanceFunction()

    fun add(point: Point, value: T) {
        add(point.x, point.y, value)
    }

    fun add(x: Float, y: Float, value: T) {
        kdTree.addPoint(doubleArrayOf(x.toDouble(), y.toDouble()), value)
    }

    fun getNearestNeighbors(point: Point): Iterator<T> {
        return getNearestNeighbors(point.x, point.y)
    }

    fun getNearestNeighbors(x: Float, y: Float): Iterator<T> {
        return kdTree.getNearestNeighborIterator(doubleArrayOf(x.toDouble(), y.toDouble()),
                Int.MAX_VALUE, sqEucDistFunc).iterator<T>()
    }
}