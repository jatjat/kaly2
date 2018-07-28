package ca.joelathiessen.kaly2.tests.pc.unit.persistence

import ca.joelathiessen.kaly2.persistence.PersistentStorage
import org.joda.time.DateTime
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import java.util.UUID

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

        val sessionHistory = robot.getSessionHistory()

        robot.releaseSessionHistory()

        val secondRobot = persist.getRobotStorage(sessionHistory.id.value)

        assertNotNull(secondRobot)
        assertEquals(sessionHistory.id, secondRobot!!.getSessionHistory().id)
    }
}