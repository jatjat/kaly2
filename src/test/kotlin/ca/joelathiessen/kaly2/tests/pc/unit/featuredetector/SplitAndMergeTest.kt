package ca.joelathiessen.kaly2.tests.pc.unit.featuredetector

import ca.joelathiessen.kaly2.Measurement
import ca.joelathiessen.kaly2.featuredetector.SplitAndMerge
import lejos.robotics.navigation.Pose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class SplitAndMergeTest {
    private val MAX_ERROR = 0.001

    private fun makeMeasFromXY(x: Double, y: Double): Measurement {
        val dist = Math.sqrt((x * x + y * y))
        val angle = Math.atan2(y, x)
        return Measurement(dist, angle, Pose(0.0f, 0.0f, 0.0f), 0)
    }

    private fun makeMeasFromXYAndRobotPos(signalX: Double, signalY: Double,
                                          robotX: Double, robotY: Double, robotTheta: Double): Measurement {
        val dist = Math.sqrt((signalX * signalX + signalY * signalY))
        val angle = Math.atan2(signalY, signalX)
        return Measurement(dist, angle, Pose(robotX.toFloat(), robotY.toFloat(), robotTheta.toFloat()), 0)
    }

    @Test
    fun testFourPointSquare() {
        val merge = SplitAndMerge()

        val measurements = ArrayList<Measurement>()
        measurements.add(makeMeasFromXY(0.0, 0.0))
        measurements.add(makeMeasFromXY(0.0, 1.0))
        measurements.add(makeMeasFromXY(1.0, 1.0))
        measurements.add(makeMeasFromXY(1.0, 0.0))

        val features = merge.getFeatures(measurements)

        assertTrue(features.size == measurements.size)

        assertEquals(features[0].x, 0.0, MAX_ERROR)
        assertEquals(features[0].y, 0.0, MAX_ERROR)

        assertEquals(features[1].x, 0.0, MAX_ERROR)
        assertEquals(features[1].y, 1.0, MAX_ERROR)

        assertEquals(features[2].x, 1.0, MAX_ERROR)
        assertEquals(features[2].y, 1.0, MAX_ERROR)

        assertEquals(features[3].x, 1.0, MAX_ERROR)
        assertEquals(features[3].y, 0.0, MAX_ERROR)
    }

    @Test
    fun testFourPointLine() {
        val merge = SplitAndMerge()

        val measurements = ArrayList<Measurement>()
        measurements.add(makeMeasFromXY(0.0, 0.0))
        measurements.add(makeMeasFromXY(0.0, 1.0))
        measurements.add(makeMeasFromXY(0.0, 2.0))
        measurements.add(makeMeasFromXY(0.0, 3.0))

        val features = merge.getFeatures(measurements)

        assertEquals(features[0].y, 0.0, MAX_ERROR)
        assertEquals(features[1].y, 3.0, MAX_ERROR)
        assertEquals(features.size, 2)
    }

    @Test
    fun testInnerCorner() {
        val merge = SplitAndMerge()

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
        val merge = SplitAndMerge()

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

    /*
    //I expect this test will work once the full split-and-merge is implemented
    @Test
    fun testDenseSquare() {
        val merge = SplitAndMerge()

        val measurements = ArrayList<Measurement>()
        run {
            var i = -1.0
            while (i <= 1) {
                measurements.add(makeMeasFromXY(-1.0, i))
                i += 0.1.0
            }
        }
        run {
            var i = -1.0
            while (i <= 1) {
                measurements.add(makeMeasFromXY(i, 1.0))
                i += 0.1.0
            }
        }
        run {
            var i = 1.0
            while (i >= -1) {
                measurements.add(makeMeasFromXY(1.0, i))
                i -= 0.1.0
            }
        }
        var i = 1.0
        while (i >= -1) {
            measurements.add(makeMeasFromXY(i, -1.0))
            i -= 0.1.0
        }

        val features = merge.getFeatures(measurements)

        assertEquals(features.size, 4)
    }*/
}
