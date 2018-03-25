package ca.joelathiessen.kaly2.tests.pc.unit.persistence

import ca.joelathiessen.kaly2.persistence.PersistentStorage
import org.joda.time.DateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class PersistentStorageTest {

    @Test
    fun testCreateRobotStorage() {
        val persist = PersistentStorage(UUID(10L, 10L),
            dbInit = PersistentStorage.DbInitTypes.IN_MEMORY_DB)

        val robot = persist.makeRobotStorage("robotname", false, "mapName",
            DateTime())

        assertEquals(robot.released, false)
    }

    @Test
    fun testGetReleasedRobotStorage() {
        val persist = PersistentStorage(UUID(10L, 10L),
            dbInit = PersistentStorage.DbInitTypes.IN_MEMORY_DB)

        val robot = persist.makeRobotStorage("robotname", false, "mapName",
            DateTime())

        assertEquals(robot.released, false)

        val history = robot.getHistory()

        robot.releaseHistory()

        val secondRobot = persist.getRobotStorage(history.id.value)

        assertNotNull(secondRobot)
        assertEquals(history.id, secondRobot!!.getHistory().id)
    }
}