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
        add(point.x.toDouble(), point.y.toDouble(), value)
    }

    fun add(x: Double, y: Double, value: T) {
        kdTree.addPoint(doubleArrayOf(x, y), value)
    }

    fun getNearestNeighbors(point: Point): Iterator<T> {
        return getNearestNeighbors(point.x.toDouble(), point.y.toDouble())
    }

    fun getNearestNeighbors(x: Double, y: Double): Iterator<T> {
        return kdTree.getNearestNeighborIterator(doubleArrayOf(x, y), Int.MAX_VALUE, sqEucDistFunc).iterator<T>()
    }
}