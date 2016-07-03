package ags.utils.dataStructures.trees.thirdGenKD

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * Tests a modified version of Rednaxela's k-d tree, that allows logarithmic copy-on-insert
 */
class TestKDTree {
    val EPSILON = 0.001

    @Test
    fun testAllInOneBucket() {
        val tree = KdTree<Double>(2)
        val tree2 = tree.addPointAsCopy(doubleArrayOf(1.0, 1.0), 7.0)
        val tree3 = tree2.addPointAsCopy(doubleArrayOf(2.0, 2.0), 8.0)
        val tree4 = tree3.addPointAsCopy(doubleArrayOf(-3.0, -3.0), 9.0)

        val it2 = tree2.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it2.next(), 7.0, EPSILON)
        assertEquals(it2.hasNext(), false)

        val it3 = tree3.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it3.next(), 7.0, EPSILON)
        assertEquals(it3.next(), 8.0, EPSILON)
        assertEquals(it3.hasNext(), false)

        val it4 = tree4.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it4.next(), 7.0, EPSILON)
        assertEquals(it4.next(), 8.0, EPSILON)
        assertEquals(it4.next(), 9.0, EPSILON)
        assertEquals(it4.hasNext(), false)
    }

    @Test
    fun testSeveralMinimalBuckets() {
        val tree = KdTree<Double>(2, 1)
        val tree2 = tree.addPointAsCopy(doubleArrayOf(1.0, 1.0), 7.0)
        val tree3 = tree2.addPointAsCopy(doubleArrayOf(2.0, 2.0), 8.0)
        val tree4 = tree3.addPointAsCopy(doubleArrayOf(-3.0, -3.0), 9.0)

        val it2 = tree2.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it2.next(), 7.0, EPSILON)
        assertEquals(it2.hasNext(), false)

        val it3 = tree3.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it3.next(), 7.0, EPSILON)
        assertEquals(it3.next(), 8.0, EPSILON)
        assertEquals(it3.hasNext(), false)

        val it4 = tree4.getNearestNeighborIterator(doubleArrayOf(1.0, 1.0), 100, SquareEuclideanDistanceFunction())
        assertEquals(it4.next(), 7.0, EPSILON)
        assertEquals(it4.next(), 8.0, EPSILON)
        assertEquals(it4.next(), 9.0, EPSILON)
        assertEquals(it4.hasNext(), false)
    }
}