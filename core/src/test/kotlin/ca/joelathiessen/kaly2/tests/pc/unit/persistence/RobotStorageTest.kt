package ca.joelathiessen.kaly2.tests.pc.unit.persistence

import ca.joelathiessen.kaly2.Measurement
import ca.joelathiessen.kaly2.RobotCoreActedResults
import ca.joelathiessen.kaly2.featuredetector.Feature
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.persistence.PersistentStorage
import ca.joelathiessen.kaly2.planner.PathSegmentInfo
import ca.joelathiessen.kaly2.planner.linear.LinearPathSegment
import ca.joelathiessen.kaly2.subconscious.LocalPlan
import ca.joelathiessen.kaly2.subconscious.SimPilotPoses
import ca.joelathiessen.kaly2.subconscious.SubconsciousActedResults
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import lejos.robotics.geometry.Point
import lejos.robotics.navigation.Pose
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class RobotStorageTest {

    val DELTA = 0.00001f

    @Test
    fun testHeartbeat() {
        val timeout = 500L

        val persist = PersistentStorage(UUID(10L, 10L),
            dbInit = PersistentStorage.DbInitTypes.IN_MEMORY_DB, canAssumeRobotUnownedTimeout = timeout)

        val robot = persist.makeRobotStorage("robotname", false, "mapName",
            DateTime())

        val sessionHistory = robot.getSessionHistory()

        robot.saveHeartbeat()

        val invalidRobot = persist.getRobotStorage(histid = sessionHistory.id.value)

        Assert.assertEquals(null, invalidRobot)
    }

    @Test
    fun testMissedHeartbeat() {
        val timeout = 1L

        val persist = PersistentStorage(UUID(10L, 10L),
            dbInit = PersistentStorage.DbInitTypes.IN_MEMORY_DB, canAssumeRobotUnownedTimeout = timeout)

        val robot = persist.makeRobotStorage("robotname", false, "mapName",
            DateTime())

        val sessionHistory = robot.getSessionHistory()

        Thread.sleep(timeout * 2)

        val validRobot = persist.getRobotStorage(histid = sessionHistory.id.value)

        // the robot's been "abandoned" by its server, so we can get it
        Assert.assertNotNull(validRobot)
    }

    @Test
    fun testSaveResultsGetIterations() {
        val persist = PersistentStorage(UUID(10L, 10L),
                dbInit = PersistentStorage.DbInitTypes.IN_MEMORY_DB)

        val robot = persist.makeRobotStorage("robotname", false, "mapName",
                DateTime())

        val features = ArrayList<Feature>()
        for (i in 0 until 10) {
            features.add(Feature(i * 0.1f, i * 0.2f, i * 0.3f, i * 0.4f, stdDev = i * 0.5f))
        }

        val obstacles = ArrayList<Point>()
        for (i in 0 until 15) {
            obstacles.add(Point(i * 0.1f, i * 0.2f))
        }

        val slamPose = RobotPose(0L, 0f, 1f, 2f, 3f)

        val maneuvers = ArrayList<RobotPose>()

        val globalPlannerPaths = ArrayList<PathSegmentInfo>()
        for (i in 0 until 20) {
            globalPlannerPaths.add(LinearPathSegment(i * 0.1f, i * 0.2f, null, i * 0.3f))
        }

        val measurements = ArrayList<Measurement>()
        for (i in 0 until 25) {
            val mesSlamPose = RobotPose(i.toLong(), i * 0.15f, i * 0.25f, i * 0.35f, i * 0.45f)
            val mesOdoPose = RobotPose(i.toLong(), i * 0.12f, i * 0.22f, i * 0.32f, i * 0.42f)
            measurements.add(Measurement(i * 0.1f, i * 0.2f, mesSlamPose, mesOdoPose, i.toLong()))
        }

        val realPos = RobotPose(0L, 0f, 1f, 2f, 3f)
        val odoPose = RobotPose(0L, 0.5f, 1.5f, 2.5f, 3.5f)

        val pilotPoses = SimPilotPoses(realPos, odoPose)

        val plan = LocalPlan(0f, 1f, 2f, 3f, 4f, 5f, 6f)

        val subconcResults = SubconsciousActedResults(measurements, pilotPoses, plan)

        val particles = ArrayList<Pose>()
        for (i in 0 until 35) {
            particles.add(Pose(i * 0.1f, i * 0.2f, i * 0.3f))
        }

        val results = RobotCoreActedResults(0L, features, obstacles, slamPose, maneuvers,
                globalPlannerPaths, subconcResults, particles, 1L)

        val timeStepFuture = robot.saveTimeStep(results)
        timeStepFuture.get()

        val itrs = robot.getIterations(0, 1)

        assertEquals(itrs.size, 1)

        val itr = itrs.first()

        assertNotNull(itr)
        assertEquals(itr.features.size, features.size)
        val featuresSorted = itr.features.sortedBy { it.sensorX }
        featuresSorted.forEachIndexed { idx, feat ->
            assertEquals(feat.sensorX, features[idx].sensorX, DELTA)
            assertEquals(feat.sensorY, features[idx].sensorY, DELTA)
            assertEquals(feat.distance, features[idx].distance, DELTA)
            assertEquals(feat.angle, features[idx].angle, DELTA)
            assertEquals(feat.deltaX, features[idx].deltaX, DELTA)
            assertEquals(feat.deltaY, features[idx].deltaY, DELTA)
            assertEquals(feat.stdDev, features[idx].stdDev, DELTA)
        }

        assertEquals(itr.measurements.size, measurements.size)
        val measurementsSorted = itr.measurements.sortedBy { it.x }
        measurementsSorted.forEachIndexed { idx, mes ->
            assertEquals(mes.x, measurements[idx].x, DELTA)
            assertEquals(mes.y, measurements[idx].y, DELTA)
            assertEquals(mes.deltaX, measurements[idx].deltaX, DELTA)
            assertEquals(mes.deltaY, measurements[idx].deltaY, DELTA)
            assertEquals(mes.distance, measurements[idx].distance, DELTA)
            assertEquals(mes.probAngle, measurements[idx].probAngle, DELTA)
            assertEquals(mes.time, measurements[idx].time)

            assertEquals(mes.probPose.x, measurements[idx].probPose.x, DELTA)
            assertEquals(mes.probPose.y, measurements[idx].probPose.y, DELTA)
            assertEquals(mes.probPose.heading, measurements[idx].probPose.heading, DELTA)

            assertEquals(mes.odoPose.x, measurements[idx].odoPose.x, DELTA)
            assertEquals(mes.odoPose.y, measurements[idx].odoPose.y, DELTA)
            assertEquals(mes.odoPose.heading, measurements[idx].odoPose.heading, DELTA)
        }

        assertEquals(itr.particles.size, particles.size)
        val particlesSorted = itr.particles.sortedBy { it.pose.x }
        particlesSorted.forEachIndexed { idx, part ->
            assertEquals(part.pose.x, particles[idx].x, DELTA)
            assertEquals(part.pose.y, particles[idx].y, DELTA)
            assertEquals(part.pose.heading, particles[idx].heading, DELTA)
        }
    }
}