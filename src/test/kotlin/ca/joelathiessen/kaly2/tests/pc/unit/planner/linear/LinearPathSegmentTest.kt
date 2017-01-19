package ca.joelathiessen.kaly2.tests.pc.unit.subconscious

import ca.joelathiessen.kaly2.planner.linear.LinearPathSegment
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LinearPathSegmentTest {
    val EPSILON = 0.001

    @Test
    fun testInitialize() {
        val linPathSeg1 = LinearPathSegment(1.0, 2.0, null, 3.0)
        val linPathSeg2 = LinearPathSegment(1.5, 2.5, linPathSeg1, 3.5)

        assertEquals(linPathSeg1.x, 1.0, EPSILON)
        assertEquals(linPathSeg1.y, 2.0, EPSILON)
        assertEquals(linPathSeg1.parent, null)
        assertEquals(linPathSeg1.cost, 3.0, EPSILON)

        assertEquals(linPathSeg2.parent, linPathSeg1)
        assertEquals(linPathSeg2.cost, 3.5, EPSILON)
    }

    @Test
    fun testCreateChild() {
        val linPathSeg1 = LinearPathSegment(1.0, 4.0, null, 3.0)
        val linPathSeg2 = linPathSeg1.makeChild(1.0, 7.0)

        assertEquals(linPathSeg2.x, 1.0, EPSILON)
        assertEquals(linPathSeg2.y, 7.0, EPSILON)
        assertEquals(linPathSeg2.parent, linPathSeg1)
        assertEquals(linPathSeg2.cost, 6.0, EPSILON)
    }

    @Test
    fun testKeepParent() {
        val linPathSeg1 = LinearPathSegment(0.0, 0.0, null, 3.0)
        val linPathSeg2 = linPathSeg1.makeChild(10.0, 0.0)
        val linPathSeg3 = linPathSeg2.makeChild(10.0, 15.0)

        val linPathSeg4 = linPathSeg1.makeChild(1.0, 50.0)

        linPathSeg3.changeParentIfCheaper(linPathSeg4)

        assertEquals(linPathSeg3.parent, linPathSeg2)
        assertEquals(linPathSeg3.cost, 28.0, EPSILON)
    }

    @Test
    fun testNewParent() {
        val linPathSeg1 = LinearPathSegment(0.0, 0.0, null, 3.0)
        val linPathSeg2 = linPathSeg1.makeChild(1.5, 50.0)
        val linPathSeg3 = linPathSeg2.makeChild(10.0, 15.0)
        val oldCost = linPathSeg3.cost

        val linPathSeg4 = linPathSeg1.makeChild(1.01, 1.01)

        linPathSeg3.changeParentIfCheaper(linPathSeg4)

        assertEquals(linPathSeg3.parent, linPathSeg4)
        assert(linPathSeg3.cost < oldCost)
    }
}