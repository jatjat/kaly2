package ca.joelathiessen.kaly2.tests.pc.unit.subconscious

import ca.joelathiessen.kaly2.Measurement
import ca.joelathiessen.kaly2.subconscious.Subconscious
import ca.joelathiessen.kaly2.subconscious.sensor.Kaly2PulsedLightLidarLiteV2
import ca.joelathiessen.kaly2.subconscious.sensor.Spinner
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import lejos.robotics.localization.OdometryPoseProvider
import lejos.robotics.navigation.MovePilot
import lejos.robotics.navigation.Pose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.ArrayList
import java.util.concurrent.ConcurrentLinkedQueue

class SubconsciousTest {

    private lateinit var sensor: Kaly2PulsedLightLidarLiteV2
    private lateinit var spinner: Spinner
    private lateinit var pilot: MovePilot
    private lateinit var odometry: OdometryPoseProvider
    private lateinit var sweeps: ConcurrentLinkedQueue<ArrayList<Measurement>>

    @Before
    fun setUp() {
        sensor = Mockito.mock(Kaly2PulsedLightLidarLiteV2::class.java)
        pilot = Mockito.mock(MovePilot::class.java)
        odometry = Mockito.mock(OdometryPoseProvider::class.java)

        spinner = Mockito.`mock`(Spinner::class.java)
        sweeps = ConcurrentLinkedQueue<ArrayList<Measurement>>()
    }

    @Test
    fun testConstructSubconscious_PassedValuesUnmodified() {
        val sensorCopy = sensor.toString()
        val spinCopy = spinner.toString()
        val pilotCopy = pilot.toString()
        val odoCopy = odometry.toString()
        val sweepsCopy = sweeps.toString()

        val sub = Subconscious(sensor, pilot, odometry, spinner, sweeps)
        sub.toString()

        assertEquals(sensor.toString(), sensorCopy)
        assertEquals(pilot.toString(), pilotCopy)
        assertEquals(odometry.toString(), odoCopy)
        assertEquals(spinner.toString(), spinCopy)
        assertEquals(sweeps.toString(), sweepsCopy)
    }

    @Test
    fun testRun_atLeast1SweepOf3Measurements() {
        val timeout: Long
        val sub = Subconscious(sensor, pilot, odometry, spinner, sweeps)
        val thread = Thread(sub)

        `when`(odometry.pose).thenReturn(Pose())
        // spin thrice:
        `when`(spinner.spinning()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false)
        `when`(spinner.angle).thenReturn(2.0f)
        doAnswer { invocation ->
            val sample = invocation.arguments[0] as FloatArray
            sample[0] = 10f
            null
        }.`when`<Kaly2PulsedLightLidarLiteV2>(sensor).fetchSample(any<FloatArray>(), any<Int>())

        timeout = System.currentTimeMillis() + 1000
        thread.start()
        // I think polling is fine, at least for now:
        while (sweeps.isEmpty() == true && System.currentTimeMillis() < timeout) {
        }
        thread.interrupt()

        assertFalse("At least one sensor sweep should have been created", sweeps.isEmpty())

        for (sweep in sweeps) {
            for (mes in sweep) {
                assertEquals("Measurement's distance should be correct", mes.distance, 10.0f, 0.0f)
                assertEquals("Measurement's probable angle should be correct", mes.probAngle, 2.0f, 0.0f)
                assertNotNull("Measurement's probable pose should not be null", mes.probPose)

                assertTrue("Measurement's time should be correct", mes.time <= timeout)
                // well it works on my computer!
            }
        }
    }

    @Test
    fun testRun_interruptionHaltsExcecution() {
        val sub = Subconscious(sensor, pilot, odometry, spinner, sweeps)
        val thread = Thread(sub)
        thread.start()
        thread.interrupt()
        try {
            thread.join(150)
        } catch (e: InterruptedException) {
            fail("Subconscious thread was interrupted but didn't stop in a timely fashion")
        }

    }
}
