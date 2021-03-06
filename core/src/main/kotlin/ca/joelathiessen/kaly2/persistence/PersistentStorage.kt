package ca.joelathiessen.kaly2.persistence

import ca.joelathiessen.kaly2.persistence.tables.FeatureTable
import ca.joelathiessen.kaly2.persistence.tables.SessionHistoryEntity
import ca.joelathiessen.kaly2.persistence.tables.SessionHistoryTable
import ca.joelathiessen.kaly2.persistence.tables.IterationTable
import ca.joelathiessen.kaly2.persistence.tables.MapEntity
import ca.joelathiessen.kaly2.persistence.tables.MapTable
import ca.joelathiessen.kaly2.persistence.tables.MeasurementTable
import ca.joelathiessen.kaly2.persistence.tables.ParticleTable
import ca.joelathiessen.kaly2.persistence.tables.ObstacleTable
import ca.joelathiessen.kaly2.persistence.tables.RobotEntity
import ca.joelathiessen.kaly2.persistence.tables.RobotTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.UUID

class PersistentStorage(
    private val serverUUID: UUID,
    private val canAssumeRobotUnownedTimeout: Long = 10000L,
    private val dbInit: DbInitTypes = DbInitTypes.FILE_DB,
    private val user: String = "",
    private val password: String = "",
    dropTablesFirst: Boolean = false
) {

    enum class DbInitTypes(val dbUrl: String, val dbDriver: String) {
        IN_MEMORY_DB("jdbc:h2:mem:kaly2db;DB_CLOSE_DELAY=-1", "org.h2.Driver"),
        FILE_DB("jdbc:h2:~/kaly2db2", "org.h2.Driver"),
        ANDROID_FILE_DB("jdbc:h2:/data/data/ca.joelathiessen.android.kaly2android/kaly2db", "org.h2.Driver"),
        MYSQL_DB("jdbc:mysql://localhost/test?rewriteBatchedStatements=true", "com.mysql.jdbc.Driver")
    }

    init {
        Database.connect(dbInit.dbUrl, driver = dbInit.dbDriver, user = user, password = password)

        transaction {
            if (dropTablesFirst) {
                drop(MapTable, RobotTable, ParticleTable, SessionHistoryTable, ObstacleTable, IterationTable, MeasurementTable, FeatureTable)
            }
            create(MapTable, RobotTable, ParticleTable, SessionHistoryTable, ObstacleTable, IterationTable, MeasurementTable, FeatureTable)
        }
    }

    fun getOrMakeRobotStorage(
        sid: Long?,
        robotName: String,
        isReal: Boolean,
        mapName: String,
        sessionHistoryStartDate: DateTime
    ): RobotStorage? {
        var storage: RobotStorage? = null
        transaction {
            if (sid != null) {
                storage = getRobotStorage(sid)
            } else {
                storage = makeRobotStorage(robotName, isReal, mapName, sessionHistoryStartDate)
            }
        }
        return storage
    }

    fun makeRobotStorage(robotName: String, isReal: Boolean, mapName: String, sessionHistoryStartDate: DateTime): RobotStorage {
        var sid = 0L
        transaction {
            val newRobot = RobotEntity.new {
                name = robotName
                physical = isReal
            }

            val newMap = MapEntity.new {
                name = mapName
            }

            val sessionHistory = SessionHistoryEntity.new {
                map = newMap
                robot = newRobot
                startDate = sessionHistoryStartDate
                owned = true
                lastHeartbeat = sessionHistoryStartDate.millis
                ownerServer = serverUUID
            }

            sid = sessionHistory.id.value
        }

        return RobotStorage(sid, serverUUID, user, password, dbInit.dbUrl)
    }

    // get robot storage if it's unowned or has timed out
    fun getRobotStorage(sid: Long): RobotStorage? {
        var robotStorage: RobotStorage? = null
        var shouldCreate = false

        transaction {
            val sessionHistory = SessionHistoryEntity.findById(sid)
            if (sessionHistory != null) {
                val curTime = System.currentTimeMillis()
                val timedOut = curTime - sessionHistory.lastHeartbeat > canAssumeRobotUnownedTimeout
                if (sessionHistory.owned == false || timedOut == true) {
                    sessionHistory.owned = true
                    sessionHistory.lastHeartbeat = curTime
                    sessionHistory.ownerServer = serverUUID
                    shouldCreate = true
                }
            }
        }

        // Create RobotStorage once its sessionHistory is committed:
        if (shouldCreate == true) {
            robotStorage = RobotStorage(sid, serverUUID, user, password, dbInit.dbUrl)
        }
        return robotStorage
    }
}