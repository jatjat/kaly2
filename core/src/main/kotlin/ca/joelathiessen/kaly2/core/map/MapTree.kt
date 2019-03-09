package ca.joelathiessen.kaly2.core.map

import ags.utils.dataStructures.trees.thirdGenKD.KdTree
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction
import lejos.robotics.geometry.Point

class MapTree() : ReadableMap {
    private val DIMENSIONS = 2
    private val BUCKET_CAPACITY = 24
    private var kdTree: KdTree<Point> = KdTree(DIMENSIONS, BUCKET_CAPACITY)
    private val sqEucDistFunc = SquareEuclideanDistanceFunction()

    constructor(obstacles: KdTree<Point>) : this() {
        this.kdTree = obstacles
    }

    override fun getNearestObstacles(point: Point): Iterator<Point> {
        return getNearestObstacles(point.x, point.y)
    }

    override fun getNearestObstacles(x: Float, y: Float): Iterator<Point> {
        return kdTree.getNearestNeighborIterator(doubleArrayOf(x.toDouble(), y.toDouble()), Int.MAX_VALUE,
            sqEucDistFunc)
    }

    fun addAsCopy(point: Point): MapTree {
        return MapTree(kdTree.addPointAsCopy(doubleArrayOf(point.x.toDouble(), point.y.toDouble()), point))
    }

    fun add(point: Point) {
        kdTree.addPoint(doubleArrayOf(point.x.toDouble(), point.y.toDouble()), point)
    }
}