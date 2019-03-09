package ca.joelathiessen.kaly2.core.tests.pc.unit.subconscious

import ca.joelathiessen.kaly2.core.planner.linear.LinearPathSegment
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LinearPathSegmentTest {
    val EPSILON = 0.001f

    @Test
    fun testInitialize() {
        val linPathSeg1 = LinearPathSegment(1.0f, 2.0f, null, 3.0f)
        val linPathSeg2 = LinearPathSegment(1.5f, 2.5f, linPathSeg1, 3.5f)

        assertEquals(linPathSeg1.x, 1.0f, EPSILON)
        assertEquals(linPathSeg1.y, 2.0f, EPSILON)
        assertEquals(linPathSeg1.parent, null)
        assertEquals(linPathSeg1.cost, 3.0f, EPSILON)

        assertEquals(linPathSeg2.parent, linPathSeg1)
        assertEquals(linPathSeg2.cost, 3.5f, EPSILON)
    }

    @Test
    fun testCreateChild() {
        val linPathSeg1 = LinearPathSegment(1.0f, 4.0f, null, 3.0f)
        val linPathSeg2 = linPathSeg1.makeChild(1.0f, 7.0f)!!

        assertEquals(linPathSeg2.x, 1.0f, EPSILON)
        assertEquals(linPathSeg2.y, 7.0f, EPSILON)
        assertEquals(linPathSeg2.parent, linPathSeg1)
        assertEquals(linPathSeg2.cost, 3f, EPSILON)
    }

    @Test
    fun testKeepParent() {
        val linPathSeg1 = LinearPathSegment(0.0f, 0.0f, null, 3.0f)
        val linPathSeg2 = linPathSeg1.makeChild(10.0f, 0.0f)
        val linPathSeg3 = linPathSeg2!!.makeChild(10.0f, 15.0f)!!

        val linPathSeg4 = linPathSeg1.makeChild(1.0f, 50.0f)!!

        linPathSeg3.changeParentIfCheaper(linPathSeg4)

        assertEquals(linPathSeg3.parent, linPathSeg2)
    }

    @Test
    fun testNewParent() {
        val linPathSeg1 = LinearPathSegment(0.0f, 0.0f, null, 3.0f)
        val linPathSeg2 = linPathSeg1.makeChild(1.5f, 50.0f)!!
        val linPathSeg3 = linPathSeg2.makeChild(10.0f, 15.0f)!!

        val linPathSeg4 = linPathSeg1.makeChild(1.01f, 1.01f)!!

        linPathSeg3.changeParentIfCheaper(linPathSeg4)

        assertEquals(linPathSeg3.parent, linPathSeg4)
    }
}