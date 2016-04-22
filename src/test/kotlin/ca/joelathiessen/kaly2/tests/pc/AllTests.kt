package ca.joelathiessen.kaly2.tests.pc

import ca.joelathiessen.kaly2.tests.pc.unit.RobotTest
import ca.joelathiessen.kaly2.tests.pc.unit.featuredetector.SplitAndMergeTest
import ca.joelathiessen.kaly2.tests.pc.unit.sensor.SpinnerTest
import ca.joelathiessen.kaly2.tests.pc.unit.subconscious.SubconsciousTest
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

@RunWith(Suite::class)
@SuiteClasses(SubconsciousTest::class, SpinnerTest::class, RobotTest::class, SplitAndMergeTest::class)
class AllTests
