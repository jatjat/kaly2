package ca.joelathiessen.kaly2.tests.pc;

import ca.joelathiessen.kaly2.tests.pc.unit.sensor.SpinnerTest;
import ca.joelathiessen.kaly2.tests.pc.unit.subconscious.SubconsciousTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({SubconsciousTest.class, SpinnerTest.class, RobotTest.class})
public class AllTests {

}
