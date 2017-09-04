package ca.joelathiessen.kaly2.tests.pc.unit.subconscious

import ca.joelathiessen.kaly2.Measurement
import ca.joelathiessen.kaly2.map.GlobalMap
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.tests.pc.unit.util.makeMeasFromXY
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import java.util.ArrayList

@RunWith(MockitoJUnitRunner::class)
class GlobalMapTest {
    private val EPSILON = 0.00001f
    private val ROBOT_POSE = 0.2f

    @Test
    fun testOneMes() {
        val improvedPose = RobotPose(0, 0f, ROBOT_POSE, ROBOT_POSE, 0f)

        val map = GlobalMap(0.1f, 0.1f, 2)

        val measurements = ArrayList<Measurement>()
        measurements.add(makeMeasFromXY(0.0f, 0.0f, ROBOT_POSE, ROBOT_POSE))

        map.incorporateMeasurements(measurements, improvedPose)

        val nearest = map.obstacleTree.getNearestObstacles(0f, 0f).asSequence().toList()
        assertEquals(1, nearest.size)

        assertEquals(measurements[0].x, nearest[0].x, EPSILON)
        assertEquals(measurements[0].y, nearest[0].y, EPSILON)
    }

    @Test
    fun testKeepTwoPointsOfFourPointLine() {
        val improvedPose = RobotPose(0, 0f, ROBOT_POSE, ROBOT_POSE, 0f)

        val map = GlobalMap(0.1f, 1f, 1)

        val firstMeasurements = ArrayList<Measurement>()
        firstMeasurements.add(makeMeasFromXY(0.0f, 0.0f, ROBOT_POSE, ROBOT_POSE))
        firstMeasurements.add(makeMeasFromXY(0.0f, 1.0f, ROBOT_POSE, ROBOT_POSE))

        map.incorporateMeasurements(firstMeasurements, improvedPose)

        val secondMeasurements = ArrayList<Measurement>()
        secondMeasurements.add(makeMeasFromXY(0.0f, 2.0f, ROBOT_POSE, ROBOT_POSE))
        secondMeasurements.add(makeMeasFromXY(0.0f, 3.0f, ROBOT_POSE, ROBOT_POSE))

        map.incorporateMeasurements(secondMeasurements, improvedPose)

        val nearest = map.obstacleTree.getNearestObstacles(0f, 0f).asSequence().toList()
        assertEquals(2, nearest.size)

        assertEquals(secondMeasurements[0].x, nearest[0].x, EPSILON)
        assertEquals(secondMeasurements[0].y, nearest[0].y, EPSILON)

        assertEquals(secondMeasurements[1].x, nearest[1].x, EPSILON)
        assertEquals(secondMeasurements[1].y, nearest[1].y, EPSILON)
    }
}