package ca.joelathiessen.kaly2.tests.pc.unit.persistence

import ca.joelathiessen.kaly2.persistence.PersistentStorage
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class RobotStorageTest {

    @Test
    fun testHeartbeat() {
        val timeout = 500L

        val persist = PersistentStorage(UUID(10L, 10L),
            dbInit = PersistentStorage.DbInitTypes.IN_MEMORY_DB, canAssumeRobotUnownedTimeout = timeout)

        val robot = persist.makeRobotStorage("robotname", false, "mapName",
            DateTime())

        val history = robot.getHistory()

        robot.saveHeartbeat()

        val invalidRobot = persist.getRobotStorage(histid = history.id.value)

        Assert.assertEquals(null, invalidRobot)
    }

    @Test
    fun testMissedHeartbeat() {
        val timeout = 1L

        val persist = PersistentStorage(UUID(10L, 10L),
            dbInit = PersistentStorage.DbInitTypes.IN_MEMORY_DB, canAssumeRobotUnownedTimeout = timeout)

        val robot = persist.makeRobotStorage("robotname", false, "mapName",
            DateTime())

        val history = robot.getHistory()

        Thread.sleep(timeout * 2)

        val validRobot = persist.getRobotStorage(histid = history.id.value)

        // the robot's been "abandoned" by its server, so we can get it
        Assert.assertNotNull(validRobot)
    }
}