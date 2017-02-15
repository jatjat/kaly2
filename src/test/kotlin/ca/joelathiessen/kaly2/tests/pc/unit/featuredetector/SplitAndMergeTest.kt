package ca.joelathiessen.kaly2.tests.pc.unit.featuredetector

import ca.joelathiessen.kaly2.Measurement
import ca.joelathiessen.kaly2.featuredetector.SplitAndMerge
import lejos.robotics.navigation.Pose
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class SplitAndMergeTest {
    private val MAX_ERROR = 0.001
    private val LINE_THRESHOLD = 0.1
    private val CHECK_WITHIN_ANGLE = 0.3
    private val MAX_RATIO = 1.0
    private val ROBOT_POS = 0.5

    private fun makeMeasFromXY(x: Double, y: Double): Measurement {
        val xOffset = x - ROBOT_POS
        val yOffset = y - ROBOT_POS
        val dist = Math.sqrt((xOffset * xOffset) + (yOffset * yOffset))
        val angle = Math.atan2(yOffset, xOffset)
        return Measurement(dist, angle, Pose(0.5f, ROBOT_POS.toFloat(), ROBOT_POS.toFloat()), 0)
    }

    @Test
    fun testFourPointSquare() {
        val merge = SplitAndMerge(LINE_THRESHOLD, CHECK_WITHIN_ANGLE, MAX_RATIO)

        val measurements = ArrayList<Measurement>()
        measurements.add(makeMeasFromXY(0.0, 0.0))
        measurements.add(makeMeasFromXY(0.0, 1.0))
        measurements.add(makeMeasFromXY(1.0, 1.0))
        measurements.add(makeMeasFromXY(1.0, 0.0))

        val features = merge.getFeatures(measurements)

        assertEquals(features.size, measurements.size)

        assertEquals(features[0].x, 0.0, MAX_ERROR)
        assertEquals(features[0].y, 0.0, MAX_ERROR)

        assertEquals(features[1].x, 1.0, MAX_ERROR)
        assertEquals(features[1].y, 0.0, MAX_ERROR)

        assertEquals(features[2].x, 1.0, MAX_ERROR)
        assertEquals(features[2].y, 1.0, MAX_ERROR)

        assertEquals(features[3].x, 0.0, MAX_ERROR)
        assertEquals(features[3].y, 1.0, MAX_ERROR)
    }

    @Test
    fun testFourPointLine() {
        val merge = SplitAndMerge(LINE_THRESHOLD, CHECK_WITHIN_ANGLE, MAX_RATIO)

        val measurements = ArrayList<Measurement>()
        measurements.add(makeMeasFromXY(0.0, 0.0))
        measurements.add(makeMeasFromXY(0.0, 1.0))
        measurements.add(makeMeasFromXY(0.0, 2.0))
        measurements.add(makeMeasFromXY(0.0, 3.0))

        val features = merge.getFeatures(measurements)

        assertEquals(features.size, 0) // all features within the threshold should are discarded
    }

    @Test
    fun testInnerCorner() {
        val merge = SplitAndMerge(LINE_THRESHOLD, CHECK_WITHIN_ANGLE, MAX_RATIO)

        val measurements = ArrayList<Measurement>()
        run {
            var i = 0.0
            while (i <= 1) {
                measurements.add(makeMeasFromXY(1.0, i))
                i += 0.1
            }
        }
        var i = 0.0
        while (i <= 1) {
            measurements.add(makeMeasFromXY(i, 1.0))
            i += 0.1
        }

        val features = merge.getFeatures(measurements)

        assertEquals(features.size, 3)
    }

    @Test
    fun testOuterCorner() {
        val merge = SplitAndMerge(LINE_THRESHOLD, CHECK_WITHIN_ANGLE, MAX_RATIO)

        val measurements = ArrayList<Measurement>()
        run {
            var i = 1.0
            while (i >= 0) {
                measurements.add(makeMeasFromXY(0.0, i))
                i -= 0.1
            }
        }
        var i = 1.0
        while (i >= 0) {
            measurements.add(makeMeasFromXY(i, 0.0))
            i -= 0.1
        }

        val features = merge.getFeatures(measurements)

        assertEquals(features.size, 3)
    }

    @Test
    fun testDenseSquare() {
        val merge = SplitAndMerge(LINE_THRESHOLD, CHECK_WITHIN_ANGLE, MAX_RATIO)
        val measurements = ArrayList<Measurement>()

        var left = -1.0
        while (left < 1.0) {
            measurements.add(makeMeasFromXY(-1.0, left))
            left += 0.01
        }

        var top = -1.0
        while (top < 1.0) {
            measurements.add(makeMeasFromXY(top, 1.0))
            top += 0.01
        }

        var right = 1.0
        while (right > -1.0) {
            measurements.add(makeMeasFromXY(1.0, right))
            right -= 0.01
        }

        var bottom = 1.0
        while (bottom > -1.0) {
            measurements.add(makeMeasFromXY(bottom, -1.0))
            bottom -= 0.01
        }

        val features = merge.getFeatures(measurements)

        assertEquals(features.size, 4)

        assertEquals(features[0].x, -1.0, MAX_ERROR)
        assertEquals(features[0].y, -1.0, MAX_ERROR)

        assertEquals(features[1].x, 1.0, MAX_ERROR)
        assertEquals(features[1].y, -1.0, MAX_ERROR)

        assertEquals(features[2].x, 1.0, MAX_ERROR)
        assertEquals(features[2].y, 1.0, MAX_ERROR)

        assertEquals(features[3].x, -1.0, MAX_ERROR)
        assertEquals(features[3].y, 1.0, MAX_ERROR)
    }
}
