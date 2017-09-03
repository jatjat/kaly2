package ca.joelathiessen.kaly2.tests.pc.unit.sensor

import ca.joelathiessen.kaly2.subconscious.sensor.Spinner
import ca.joelathiessen.util.FloatMath
import lejos.robotics.RegulatedMotor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SpinnerTest {

    @Mock
    private lateinit var motor: RegulatedMotor

    @Test
    fun testSpin_DefaultLimits() {
        val spinner = Spinner(motor)

        `when`(motor.isMoving).thenReturn(false).thenReturn(true).thenReturn(false)

        assertFalse(spinner.spinning())
        assertTrue(spinner.turningClockwise())

        spinner.spin()

        assertTrue(spinner.spinning())
        assertFalse(spinner.turningClockwise())
        verify<RegulatedMotor>(motor).rotateTo(Spinner.DEFAULT_MAX_DETECTOR_ANGLE_DEG, true)

        spinner.spin()

        assertFalse(spinner.spinning())
        assertTrue(spinner.turningClockwise())
        verify<RegulatedMotor>(motor).rotateTo(Spinner.DEFAULT_MAX_DETECTOR_ANGLE_DEG, true)
    }

    @Test
    fun testSpin_LargeLimits() {
        val spinner = Spinner(motor, -360, 720)

        `when`(motor.isMoving).thenReturn(false).thenReturn(true).thenReturn(false)

        assertFalse(spinner.spinning())
        assertTrue(spinner.turningClockwise())

        spinner.spin()

        assertTrue(spinner.spinning())
        assertFalse(spinner.turningClockwise())
        verify<RegulatedMotor>(motor).rotateTo(-360, true)

        spinner.spin()

        assertFalse(spinner.spinning())
        assertTrue(spinner.turningClockwise())
        verify<RegulatedMotor>(motor).rotateTo(720, true)
    }

    @Test
    fun testGetAngle() {
        val spinner = Spinner(motor)

        `when`(motor.tachoCount).thenReturn(77)

        assertEquals(spinner.angle, FloatMath.toRadians(77.0f), 0.0001f)
    }

}
