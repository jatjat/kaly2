package ca.joelathiessen.kaly2.tests.pc.unit.sensor;

import static org.junit.Assert.*;
import lejos.robotics.RegulatedMotor;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import ca.joelathiessen.kaly2.main.subconscious.sensor.Spinner;

@RunWith(MockitoJUnitRunner.class)
public class SpinnerTest {

  @Mock
  private RegulatedMotor motor;

  @Before
  public void setUpBeforeClass() {
    motor = Mockito.mock(RegulatedMotor.class);
  }

  @Test
  public void testSpin_DefaultLimits() {
    Spinner spinner = new Spinner(motor);

    when(motor.isMoving()).thenReturn(false).thenReturn(true).thenReturn(false);

    assertFalse(spinner.spinning());
    assertTrue(spinner.turningClockwise());

    spinner.spin();

    assertTrue(spinner.spinning());
    assertFalse(spinner.turningClockwise());
    verify(motor).rotateTo(Spinner.DEFAULT_MAX_DETECTOR_ANGLE_DEG, true);

    spinner.spin();

    assertFalse(spinner.spinning());
    assertTrue(spinner.turningClockwise());
    verify(motor).rotateTo(Spinner.DEFAULT_MIN_DETECTOR_ANGLE_DEG, true);
  }

  @Test
  public void testSpin_LargeLimits() {
    Spinner spinner = new Spinner(motor, -360, 720);

    when(motor.isMoving()).thenReturn(false).thenReturn(true).thenReturn(false);

    assertFalse(spinner.spinning());
    assertTrue(spinner.turningClockwise());

    spinner.spin();

    assertTrue(spinner.spinning());
    assertFalse(spinner.turningClockwise());
    verify(motor).rotateTo(-360, true);

    spinner.spin();

    assertFalse(spinner.spinning());
    assertTrue(spinner.turningClockwise());
    verify(motor).rotateTo(720, true);
  }


  @Test
  public void testGetAngle() {
    Spinner spinner = new Spinner(motor);

    when(motor.getTachoCount()).thenReturn(77);

    assertEquals(spinner.getAngle(), Math.toRadians(77), 0.0001);
  }

}
