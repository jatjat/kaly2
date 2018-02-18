package ca.joelathiessen.kaly2.persistence

import ca.joelathiessen.kaly2.Measurement
import ca.joelathiessen.kaly2.RobotCoreActedResults
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.persistence.tables.HistoryEntity
import ca.joelathiessen.kaly2.persistence.tables.HistoryTable
import ca.joelathiessen.kaly2.persistence.tables.IterationTable
import ca.joelathiessen.kaly2.persistence.tables.MeasurementTable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.util.UUID
import java.util.Properties
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.coroutines.experimental.buildSequence

class RobotStorage(val histid: Long, private val serverUUID: UUID,
                   dbUser: String, dbPassword: String, dbUrl: String) {
    private val dbPool = Executors.newSingleThreadExecutor()!!
    private val jdbcConnection: Connection

    var released = false
        private set

    private val mesInsertSeq = buildSequence {
        val insertStr = "insert into measurements(iteration, mes_num, mes_time, distance, angle," +
                "guessed_pose_x, guessed_pose_y, guessed_pose_heading, odo_pose_x, odo_pose_y, odo_pose_heading)" +
                "values"
        val valStr = "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        var insertQuery = insertStr + valStr
        yield(insertQuery + ";")
        while (true) {
            insertQuery = insertQuery + ", " + valStr
            yield(insertQuery + ";")
        }
    }

    private val singleAmt = 1
    private val singleStr = makeMesInsertQueryStr(mesInsertSeq, singleAmt)
    private val singleStmt: PreparedStatement

    private val smallAmt = 10
    private val smallStr = makeMesInsertQueryStr(mesInsertSeq, smallAmt)
    private val smallStmt: PreparedStatement

    private val medAmt = 50
    private val medStr = makeMesInsertQueryStr(mesInsertSeq, medAmt)
    private val medStmt: PreparedStatement

    private val lrgAmt = 360
    private val largeStr = makeMesInsertQueryStr(mesInsertSeq, lrgAmt)
    private val largeStmt: PreparedStatement

    private val itrStr = "insert into iterations(history, itr_num, itr_time, slam_pose_x, slam_pose_y, slam_pose_heading) " +
            "values(?, ?, ?, ?, ?, ?)"
    private val itrStmt: PreparedStatement

    init {
        transaction {
            HistoryEntity.findById(histid)
                    ?: throw IllegalArgumentException("RobotStorage requires a history row to already exist")
        }

        val props = Properties()
        props.put("user", dbUser)
        props.put("password", dbPassword)
        jdbcConnection = DriverManager.getConnection(dbUrl, props)
        jdbcConnection.autoCommit = false

        singleStmt = jdbcConnection.prepareStatement(singleStr)
        smallStmt = jdbcConnection.prepareStatement(smallStr)
        medStmt = jdbcConnection.prepareStatement(medStr)
        largeStmt = jdbcConnection.prepareStatement(largeStr)
        itrStmt = jdbcConnection.prepareStatement(itrStr, PreparedStatement.RETURN_GENERATED_KEYS)
    }

    private fun makeMesInsertQueryStr(seq: Sequence<String>, amt: Int): String {
        return seq.take(amt).last()
    }

    private fun <T> doIfNotReleased(toDo: () -> T): Future<T> {
        return dbPool.submit(Callable {
            if (released) {
                throw IllegalStateException("RobotStorage can only save to a history that it has not released")
            }
            toDo()
        }) as Future<T>
    }

    fun saveTimeStep(results: RobotCoreActedResults) {
        val measurements = results.subconcResults.measurements
        var singleStmtUsed = false
        var smallStmtUsed = false
        var medStmtUsed = false
        var largeStmtUsed = false

        doIfNotReleased {
            transaction {
                itrStmt.setLong(1, histid)
                itrStmt.setLong(2, results.numItrs)
                itrStmt.setLong(3, results.timestamp)

                itrStmt.setBigDecimal(4, BigDecimal(results.slamPose.x.toString()))
                itrStmt.setBigDecimal(5, BigDecimal(results.slamPose.y.toString()))
                itrStmt.setBigDecimal(6, BigDecimal(results.slamPose.heading.toString()))

                itrStmt.executeUpdate()
                val rs = itrStmt.generatedKeys
                rs.next()
                connection.commit()

                var remaining = measurements.size
                while (remaining > 0) {
                    when {
                        remaining > lrgAmt -> {
                            val prevRemaining = remaining
                            remaining -= lrgAmt
                            setMesInPreparedStatements(measurements, rs.getLong(1), largeStmt, remaining, prevRemaining)
                            largeStmtUsed = true
                        }
                        remaining > medAmt -> {
                            val prevRemaining = remaining
                            remaining -= medAmt
                            setMesInPreparedStatements(measurements, rs.getLong(1), medStmt, remaining, prevRemaining)

                            medStmtUsed = true
                        }
                        remaining > smallAmt -> {
                            val prevRemaining = remaining
                            remaining -= smallAmt
                            setMesInPreparedStatements(measurements, rs.getLong(1), smallStmt, remaining, prevRemaining)
                            smallStmtUsed = true
                        }
                        else -> {
                            val prevRemaining = remaining
                            remaining -= singleAmt
                            setMesInPreparedStatements(measurements, rs.getLong(1), singleStmt, remaining, prevRemaining)
                            singleStmtUsed = true
                        }
                    }
                }

                if (largeStmtUsed) {
                    largeStmt.executeBatch()
                }
                if (medStmtUsed) {
                    medStmt.executeBatch()
                }
                if (smallStmtUsed) {
                    smallStmt.executeBatch()
                }
                if (singleStmtUsed) {
                    singleStmt.executeBatch()
                }
                jdbcConnection.commit()
            }
        }
    }

    private fun setMesInPreparedStatements(measurements: List<Measurement>, itrID: Long,
                                           stmt: PreparedStatement, start: Int, end: Int) {
        var stmtPos = 1
        for (i in start until end) {
            val mes = measurements[i]
            stmt.setLong(stmtPos, itrID)
            stmt.setLong(stmtPos + 1, i.toLong())
            stmt.setLong(stmtPos + 2, mes.time)

            stmt.setBigDecimal(stmtPos + 3, BigDecimal(mes.distance.toDouble()))
            stmt.setBigDecimal(stmtPos + 4, BigDecimal(mes.probAngle.toDouble()))

            stmt.setBigDecimal(stmtPos + 5, BigDecimal(mes.probPose.x.toDouble()))
            stmt.setBigDecimal(stmtPos + 6, BigDecimal(mes.probPose.y.toDouble()))
            stmt.setBigDecimal(stmtPos + 7, BigDecimal(mes.probPose.heading.toDouble()))

            stmt.setBigDecimal(stmtPos + 8, BigDecimal(mes.odoPose.x.toDouble()))
            stmt.setBigDecimal(stmtPos + 9, BigDecimal(mes.odoPose.y.toDouble()))
            stmt.setBigDecimal(stmtPos + 10, BigDecimal(mes.odoPose.heading.toDouble()))

            stmtPos += 11
        }
        stmt.addBatch()
    }

    fun getHistory(): HistoryEntity {
        val ans = doIfNotReleased {
            transaction { HistoryEntity.findById(histid)!! }
        }
        return ans.get()
    }

    data class MesSlamPair(val slamPose: RobotPose, val measurements: List<Measurement>, val itrNum: Long)

    fun getMeasurements(): List<MesSlamPair> {
        val pairs = ArrayList<MesSlamPair>()

        transaction {
            val allItrs = IterationTable
                .innerJoin(HistoryTable)
                .select { IterationTable.history eq HistoryTable.id }
                .orderBy(IterationTable.itrNum, false)
                .toList()

            allItrs.forEach {
                val foundNMeasurements = MeasurementTable
                    .innerJoin(IterationTable)
                    .select { MeasurementTable.iteration eq it[IterationTable.id] }
                    .orderBy(MeasurementTable.mesNum)
                    .toList()

                val measurements = foundNMeasurements.map {
                    val probPose = RobotPose(it[MeasurementTable.mesTime], 0f,
                        it[MeasurementTable.guessedPoseX].toFloat(), it[MeasurementTable.guessedPoseY].toFloat(),
                        it[MeasurementTable.guessedPoseHeading].toFloat())
                    val odoPose = RobotPose(it[MeasurementTable.mesTime], 0f,
                        it[MeasurementTable.odoPoseX].toFloat(), it[MeasurementTable.odoPoseY].toFloat(),
                        it[MeasurementTable.odoPoseHeading].toFloat())

                    Measurement(it[MeasurementTable.distance].toFloat(), it[MeasurementTable.angle].toFloat(),
                        probPose, odoPose, it[MeasurementTable.mesTime])
                }
                val slamPose = RobotPose(it[IterationTable.itrTime], 0f, it[IterationTable.slamPoseX].toFloat(),
                    it[IterationTable.slamPoseY].toFloat(), it[IterationTable.slamPoseHeading].toFloat())

                pairs.add(MesSlamPair(slamPose, measurements, it[IterationTable.itrNum]))
            }
        }

        return pairs
    }

    fun saveHeartbeat() {
        doIfNotReleased {
            transaction {
                val history = HistoryEntity.findById(histid)!!
                if (history.ownerServer != serverUUID) {
                    throw IllegalArgumentException(
                        "RobotStorage can only save a heartbeat to a history that its server owns"
                    )
                }
                history.lastHeartbeat = System.currentTimeMillis()
            }
        }.get()
    }

    fun releaseHistory() {
        doIfNotReleased {
            transaction {
                val history = HistoryEntity.findById(histid)!!
                if (history.ownerServer != serverUUID) {
                    throw IllegalStateException(
                        "RobotStorage can only release a history that its server already owns"
                    )
                }
                history.lastHeartbeat = System.currentTimeMillis()
                history.owned = false
            }
            released = true
        }.get()
    }
}
