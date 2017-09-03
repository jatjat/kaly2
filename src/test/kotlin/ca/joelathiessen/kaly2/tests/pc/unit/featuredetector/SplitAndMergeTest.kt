package ca.joelathiessen.kaly2.tests.pc.unit.featuredetector

import ca.joelathiessen.kaly2.Measurement
import ca.joelathiessen.kaly2.featuredetector.SplitAndMerge
import ca.joelathiessen.kaly2.tests.pc.unit.util.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class SplitAndMergeTest {
    private val MAX_ERROR = 0.001f
    private val LINE_THRESHOLD = 0.1f
    private val CHECK_WITHIN_ANGLE = 0.3f
    private val MAX_RATIO = 1.0f
    private val ROBOT_POS = 0.5f

    @Test
    fun testFourPointSquare() {
        val merge = SplitAndMerge(LINE_THRESHOLD, CHECK_WITHIN_ANGLE, MAX_RATIO)

        val measurements = ArrayList<Measurement>()
        measurements.add(makeMeasFromXY(0.0f, 0.0f, ROBOT_POS, ROBOT_POS))
        measurements.add(makeMeasFromXY(0.0f, 1.0f, ROBOT_POS, ROBOT_POS))
        measurements.add(makeMeasFromXY(1.0f, 1.0f, ROBOT_POS, ROBOT_POS))
        measurements.add(makeMeasFromXY(1.0f, 0.0f, ROBOT_POS, ROBOT_POS))

        val features = merge.getFeatures(measurements)

        assertEquals(features.size, measurements.size)

        assertEquals(features[0].x, 0.0f, MAX_ERROR)
        assertEquals(features[0].y, 0.0f, MAX_ERROR)

        assertEquals(features[1].x, 1.0f, MAX_ERROR)
        assertEquals(features[1].y, 0.0f, MAX_ERROR)

        assertEquals(features[2].x, 1.0f, MAX_ERROR)
        assertEquals(features[2].y, 1.0f, MAX_ERROR)

        assertEquals(features[3].x, 0.0f, MAX_ERROR)
        assertEquals(features[3].y, 1.0f, MAX_ERROR)
    }

    @Test
    fun testFourPointLine() {
        val merge = SplitAndMerge(LINE_THRESHOLD, CHECK_WITHIN_ANGLE, MAX_RATIO)

        val measurements = ArrayList<Measurement>()
        measurements.add(makeMeasFromXY(0.0f, 0.0f, ROBOT_POS, ROBOT_POS))
        measurements.add(makeMeasFromXY(0.0f, 1.0f, ROBOT_POS, ROBOT_POS))
        measurements.add(makeMeasFromXY(0.0f, 2.0f, ROBOT_POS, ROBOT_POS))
        measurements.add(makeMeasFromXY(0.0f, 3.0f, ROBOT_POS, ROBOT_POS))

        val features = merge.getFeatures(measurements)

        assertEquals(features.size, 0) // all features within the threshold should are discarded
    }

    @Test
    fun testInnerCorner() {
        val merge = SplitAndMerge(LINE_THRESHOLD, CHECK_WITHIN_ANGLE, MAX_RATIO)

        val measurements = ArrayList<Measurement>()
        run {
            var i = 0.0f
            while (i <= 1f) {
                measurements.add(makeMeasFromXY(1.0f, i, ROBOT_POS, ROBOT_POS))
                i += 0.1f
            }
        }
        var i = 0.0f
        while (i <= 1f) {
            measurements.add(makeMeasFromXY(i, 1.0f, ROBOT_POS, ROBOT_POS))
            i += 0.1f
        }

        val features = merge.getFeatures(measurements)

        assertEquals(features.size, 3)
    }

    @Test
    fun testOuterCorner() {
        val merge = SplitAndMerge(LINE_THRESHOLD, CHECK_WITHIN_ANGLE, MAX_RATIO)

        val measurements = ArrayList<Measurement>()
        run {
            var i = 1.0f
            while (i >= 0f) {
                measurements.add(makeMeasFromXY(0.0f, i, ROBOT_POS, ROBOT_POS))
                i -= 0.1f
            }
        }
        var i = 1.0f
        while (i >= 0f) {
            measurements.add(makeMeasFromXY(i, 0.0f, ROBOT_POS, ROBOT_POS))
            i -= 0.1f
        }

        val features = merge.getFeatures(measurements)

        assertEquals(features.size, 3)
    }

    @Test
    fun testDenseSquare() {
        val merge = SplitAndMerge(LINE_THRESHOLD, CHECK_WITHIN_ANGLE, MAX_RATIO)
        val measurements = ArrayList<Measurement>()

        var left = -1.0f
        while (left < 1.0f) {
            measurements.add(makeMeasFromXY(-1.0f, left, ROBOT_POS, ROBOT_POS))
            left += 0.01f
        }

        var top = -1.0f
        while (top < 1.0f) {
            measurements.add(makeMeasFromXY(top, 1.0f, ROBOT_POS, ROBOT_POS))
            top += 0.01f
        }

        var right = 1.0f
        while (right > -1.0f) {
            measurements.add(makeMeasFromXY(1.0f, right, ROBOT_POS, ROBOT_POS))
            right -= 0.01f
        }

        var bottom = 1.0f
        while (bottom > -1.0f) {
            measurements.add(makeMeasFromXY(bottom, -1.0f, ROBOT_POS, ROBOT_POS))
            bottom -= 0.01f
        }

        val features = merge.getFeatures(measurements)

        assertEquals(features.size, 4)

        assertEquals(features[0].x, -1.0f, MAX_ERROR)
        assertEquals(features[0].y, -1.0f, MAX_ERROR)

        assertEquals(features[1].x, 1.0f, MAX_ERROR)
        assertEquals(features[1].y, -1.0f, MAX_ERROR)

        assertEquals(features[2].x, 1.0f, MAX_ERROR)
        assertEquals(features[2].y, 1.0f, MAX_ERROR)

        assertEquals(features[3].x, -1.0f, MAX_ERROR)
        assertEquals(features[3].y, 1.0f, MAX_ERROR)
    }
}
