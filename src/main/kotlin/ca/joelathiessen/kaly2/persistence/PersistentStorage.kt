package ca.joelathiessen.kaly2.persistence

import ca.joelathiessen.kaly2.persistence.tables.HistoryEntity
import ca.joelathiessen.kaly2.persistence.tables.HistoryTable
import ca.joelathiessen.kaly2.persistence.tables.IterationTable
import ca.joelathiessen.kaly2.persistence.tables.MapEntity
import ca.joelathiessen.kaly2.persistence.tables.MapTable
import ca.joelathiessen.kaly2.persistence.tables.MeasurementTable
import ca.joelathiessen.kaly2.persistence.tables.NRobotPoses
import ca.joelathiessen.kaly2.persistence.tables.ObstacleTable
import ca.joelathiessen.kaly2.persistence.tables.RobotEntity
import ca.joelathiessen.kaly2.persistence.tables.RobotTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.UUID

class PersistentStorage(private val serverUUID: UUID, private val canAssumeRobotUnownedTimeout: Long = 10000L,
    dbInit: DbInitTypes = DbInitTypes.IN_MEMORY_DB, user: String = "", password: String = "",
    dropTablesFirst: Boolean = false) {

    enum class DbInitTypes(val dbInit: String, val dbDriver: String) {
        IN_MEMORY_DB("jdbc:h2:mem:kaly2db;DB_CLOSE_DELAY=-1", "org.h2.Driver"),
        FILE_DB("jdbc:h2:~/kaly2db2", "org.h2.Driver"),
        MYSQL_DB("jdbc:mysql://localhost/test?rewriteBatchedStatements=true", "com.mysql.jdbc.Driver")
    }

    init {
        Database.connect(dbInit.dbInit, driver = dbInit.dbDriver, user = user, password = password)

        transaction {
            if (dropTablesFirst) {
                drop(MapTable, RobotTable, NRobotPoses, HistoryTable, ObstacleTable, IterationTable, MeasurementTable)
            }
            create(MapTable, RobotTable, NRobotPoses, HistoryTable, ObstacleTable, IterationTable, MeasurementTable)
        }
    }

    fun makeRobotStorage(robotName: String, isReal: Boolean, mapName: String, historyStartDate: DateTime): RobotStorage {
        var histid = 0L
        transaction {
            val newRobot = RobotEntity.new {
                name = robotName
                physical = isReal
            }

            val newMap = MapEntity.new {
                name = mapName
            }

            val history = HistoryEntity.new {
                map = newMap
                robot = newRobot
                startDate = historyStartDate
                owned = true
                lastHeartbeat = historyStartDate.millis
                ownerServer = serverUUID
            }

            histid = history.id.value
        }

        return RobotStorage(histid, serverUUID)
    }

    // get robot storage if it's unowned or has timed out
    fun getRobotStorage(histid: Long): RobotStorage? {
        var robotStorage: RobotStorage? = null
        var shouldCreate = false

        transaction {
            val history = HistoryEntity.findById(histid)
            if (history != null) {
                val curTime = System.currentTimeMillis()
                val timedOut = curTime - history.lastHeartbeat > canAssumeRobotUnownedTimeout
                if (history.owned == false || timedOut == true) {
                    history.owned = true
                    history.lastHeartbeat = curTime
                    history.ownerServer = serverUUID
                    shouldCreate = true
                }
            }
        }

        // Create RobotStorage once its history is committed:
        if (shouldCreate == true) {
            robotStorage = RobotStorage(histid, serverUUID)
        }
        return robotStorage
    }
}