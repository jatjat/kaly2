package ca.joelathiessen.kaly2.tests.pc.acceptance.kdtree

import ags.utils.dataStructures.trees.thirdGenKD.KdTree
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * Tests a modified version of Rednaxela's k-d tree, that allows
 * logarithmic copy-on-insert, and deletion
 */
class TestKdTree {
    val EPSILON = 0.001

    @Test
    fun testInsertCopyAllInOneBucket() {
        val tree = KdTree<Double>(2)
        val tree2 = tree.addPointAsCopy(doubleArrayOf(1.0, 1.0), 7.0)
        val tree3 = tree2.addPointAsCopy(doubleArrayOf(2.0, 2.0), 8.0)
        val tree4 = tree3.addPointAsCopy(doubleArrayOf(-3.0, -3.0), 9.0)

        val it1 = tree2.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it1.next(), 7.0, EPSILON)
        assertEquals(it1.hasNext(), false)

        val it2 = tree3.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it2.next(), 7.0, EPSILON)
        assertEquals(it2.next(), 8.0, EPSILON)
        assertEquals(it2.hasNext(), false)

        val it3 = tree4.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it3.next(), 7.0, EPSILON)
        assertEquals(it3.next(), 8.0, EPSILON)
        assertEquals(it3.next(), 9.0, EPSILON)
        assertEquals(it3.hasNext(), false)
    }

    @Test
    fun testInsertCopyInMinBuckets() {
        val tree = KdTree<Double>(2, 1)
        val tree2 = tree.addPointAsCopy(doubleArrayOf(1.0, 1.0), 7.0)
        val tree3 = tree2.addPointAsCopy(doubleArrayOf(2.0, 2.0), 8.0)
        val tree4 = tree3.addPointAsCopy(doubleArrayOf(-3.0, -3.0), 9.0)

        val it1 = tree2.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it1.next(), 7.0, EPSILON)
        assertEquals(it1.hasNext(), false)

        val it2 = tree3.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it2.next(), 7.0, EPSILON)
        assertEquals(it2.next(), 8.0, EPSILON)
        assertEquals(it2.hasNext(), false)

        val it3 = tree4.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it3.next(), 7.0, EPSILON)
        assertEquals(it3.next(), 8.0, EPSILON)
        assertEquals(it3.next(), 9.0, EPSILON)
        assertEquals(it3.hasNext(), false)
    }

    @Test
    fun TesttestDeleteAllInOneBucket() {
        val obj1 = Any()
        val obj2 = Any()
        val obj3 = Any()

        val tree = KdTree<Any>(2)
        val tree2 = tree.addPointAsCopy(doubleArrayOf(1.0, 1.0), obj1)
        val tree3 = tree2.addPointAsCopy(doubleArrayOf(2.0, 2.0), obj2)
        val tree4 = tree3.addPointAsCopy(doubleArrayOf(-3.0, -3.0), obj3)

        tree4.removePoint(doubleArrayOf(2.0, 2.0), obj2)

        // check if deleted
        val it = tree4.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it.next(), obj1)
        assertEquals(it.next(), obj3)
        assertEquals(it.hasNext(), false)

        // check if not deleted in older tree (since the Any was deleted from the new tree's copy of the bucket)
        val it2 = tree3.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it2.next(), obj1)
        assertEquals(it2.next(), obj2)
        assertEquals(it2.hasNext(), false)
    }

    @Test
    fun testDeleteFirstAddedInMinBuckets() {
        val obj1 = Any()
        val obj2 = Any()
        val obj3 = Any()

        val tree = KdTree<Any>(2,1)
        val tree2 = tree.addPointAsCopy(doubleArrayOf(1.0, 1.0), obj1)
        val tree3 = tree2.addPointAsCopy(doubleArrayOf(2.0, 2.0), obj2)
        val tree4 = tree3.addPointAsCopy(doubleArrayOf(-3.0, -3.0), obj3)

        tree4.removePoint(doubleArrayOf(1.0, 1.0), obj1)

        // make sure deleted
        val it = tree4.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it.next(), obj2)
        assertEquals(it.next(), obj3)
        assertEquals(it.hasNext(), false)

        // make sure not deleted in older trees
        val it2 = tree3.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it2.next(), obj1)
        assertEquals(it2.next(), obj2)
        assertEquals(it2.hasNext(), false)

        val it3 = tree2.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it3.next(), obj1)
        assertEquals(it3.hasNext(), false)
    }

    @Test
    fun testDeleteMidAddedInMinBuckets() {
        val obj1 = Any()
        val obj2 = Any()
        val obj3 = Any()

        val tree = KdTree<Any>(2,1)
        val tree2 = tree.addPointAsCopy(doubleArrayOf(1.0, 1.0), obj1)
        val tree3 = tree2.addPointAsCopy(doubleArrayOf(2.0, 2.0), obj2)
        val tree4 = tree3.addPointAsCopy(doubleArrayOf(-3.0, -3.0), obj3)

        tree4.removePoint(doubleArrayOf(2.0, 2.0), obj2)

        // make sure deleted
        val it = tree4.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it.next(), obj1)
        assertEquals(it.next(), obj3)
        assertEquals(it.hasNext(), false)

        // make sure not deleted in older trees
        val it2 = tree3.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it2.next(), obj1)
        assertEquals(it2.next(), obj2)
        assertEquals(it2.hasNext(), false)

        val it3 = tree2.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it3.next(), obj1)
        assertEquals(it3.hasNext(), false)
    }

    @Test
    fun testDeleteLastAddedInMinBuckets() {
        val obj1 = Any()
        val obj2 = Any()
        val obj3 = Any()

        val tree = KdTree<Any>(2,1)
        val tree2 = tree.addPointAsCopy(doubleArrayOf(1.0, 1.0), obj1)
        val tree3 = tree2.addPointAsCopy(doubleArrayOf(2.0, 2.0), obj2)
        val tree4 = tree3.addPointAsCopy(doubleArrayOf(-3.0, -3.0), obj3)

        tree4.removePoint(doubleArrayOf(-3.0, -3.0), obj3)

        // make sure deleted
        val it = tree4.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it.next(), obj1)
        assertEquals(it.next(), obj2)
        assertEquals(it.hasNext(), false)

        // make sure not deleted in older trees
        val it2 = tree3.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it2.next(), obj1)
        assertEquals(it2.next(), obj2)
        assertEquals(it2.hasNext(), false)

        val it3 = tree2.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it3.next(), obj1)
        assertEquals(it3.hasNext(), false)
    }
}