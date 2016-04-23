package ca.joelathiessen.kaly2.tests.pc.unit.featuredetector

import ca.joelathiessen.kaly2.featuredetector.SplitAndMerge
import ca.joelathiessen.kaly2.subconscious.Measurement
import ca.joelathiessen.kaly2.featuredetector.Kaly2Feature
import lejos.robotics.navigation.Pose
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner

import java.util.ArrayList

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

@RunWith(MockitoJUnitRunner::class)
class SplitAndMergeTest {
    private val MAX_ERROR = 0.001

    private fun makeMeasFromXY(x: Float, y: Float): Measurement {
        val dist = Math.sqrt((x * x + y * y).toDouble()).toFloat()
        val angle = Math.atan2(y.toDouble(),x.toDouble()).toFloat()
        return Measurement(dist, angle, Pose(0f, 0f, 0f), 0)
    }

    @Test
    fun testFourPointSquare() {
        val merge = SplitAndMerge()

        val measurements = ArrayList<Measurement>()
        measurements.add(makeMeasFromXY(0f, 0f))
        measurements.add(makeMeasFromXY(0f, 1f))
        measurements.add(makeMeasFromXY(1f, 1f))
        measurements.add(makeMeasFromXY(1f, 0f))

        val features = merge.getFeatures(measurements)

        assertTrue(features.size == measurements.size)

        assertEquals(features[0].x.toDouble(), 0.0, MAX_ERROR)
        assertEquals(features[0].y.toDouble(), 0.0, MAX_ERROR)

        assertEquals(features[1].x.toDouble(), 0.0, MAX_ERROR)
        assertEquals(features[1].y.toDouble(), 1.0, MAX_ERROR)

        assertEquals(features[2].x.toDouble(), 1.0, MAX_ERROR)
        assertEquals(features[2].y.toDouble(), 1.0, MAX_ERROR)

        assertEquals(features[3].x.toDouble(), 1.0, MAX_ERROR)
        assertEquals(features[3].y.toDouble(), 0.0, MAX_ERROR)
    }

    @Test
    fun testFourPointLine() {
        val merge = SplitAndMerge()

        val measurements = ArrayList<Measurement>()
        measurements.add(makeMeasFromXY(0f, 0f))
        measurements.add(makeMeasFromXY(0f, 1f))
        measurements.add(makeMeasFromXY(0f, 2f))
        measurements.add(makeMeasFromXY(0f, 3f))

        val features = merge.getFeatures(measurements)

        assertEquals(features[0].y.toDouble(), 0.0, MAX_ERROR)
        assertEquals(features[1].y.toDouble(), 3.0, MAX_ERROR)
        assertEquals(features.size, 2)
    }

    @Test
    fun testInnerCorner() {
        val merge = SplitAndMerge()

        val measurements = ArrayList<Measurement>()
        run {
            var i = 0f
            while (i <= 1) {
                measurements.add(makeMeasFromXY(1f, i))
                i += 0.1f
            }
        }
        var i = 0f
        while (i <= 1) {
            measurements.add(makeMeasFromXY(i, 1f))
            i += 0.1f
        }

        val features = merge.getFeatures(measurements)

        assertEquals(features.size, 3)
    }

    @Test
    fun testOuterCorner() {
        val merge = SplitAndMerge()

        val measurements = ArrayList<Measurement>()
        run {
            var i = 1f
            while (i >= 0) {
                measurements.add(makeMeasFromXY(0f, i))
                i -= 0.1f
            }
        }
        var i = 1f
        while (i >= 0) {
            measurements.add(makeMeasFromXY(i, 0f))
            i -= 0.1f
        }

        val features = merge.getFeatures(measurements)

        assertEquals(features.size, 3)
    }

    /*
    //I expect this test will work once the full split-and-merge is implemented
    @Test
    fun testDenseSquare() {
        val merge = SplitAndMerge()

        val measurements = ArrayList<Measurement>()
        run {
            var i = -1f
            while (i <= 1) {
                measurements.add(makeMeasFromXY(-1f, i))
                i += 0.1f
            }
        }
        run {
            var i = -1f
            while (i <= 1) {
                measurements.add(makeMeasFromXY(i, 1f))
                i += 0.1f
            }
        }
        run {
            var i = 1f
            while (i >= -1) {
                measurements.add(makeMeasFromXY(1f, i))
                i -= 0.1f
            }
        }
        var i = 1f
        while (i >= -1) {
            measurements.add(makeMeasFromXY(i, -1f))
            i -= 0.1f
        }

        val features = merge.getFeatures(measurements)

        assertEquals(features.size, 4)
    }*/
}
