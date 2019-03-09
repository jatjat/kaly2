package ca.joelathiessen.kaly2.core.persistence

import ca.joelathiessen.kaly2.core.Measurement
import ca.joelathiessen.kaly2.core.RobotCoreActedResults
import ca.joelathiessen.kaly2.core.featuredetector.Feature
import ca.joelathiessen.kaly2.core.odometry.RobotPose
import ca.joelathiessen.kaly2.core.persistence.tables.FeatureTable
import ca.joelathiessen.kaly2.core.persistence.tables.SessionHistoryEntity
import ca.joelathiessen.kaly2.core.persistence.tables.SessionHistoryTable
import ca.joelathiessen.kaly2.core.persistence.tables.IterationTable
import ca.joelathiessen.kaly2.core.persistence.tables.MeasurementTable
import ca.joelathiessen.kaly2.core.persistence.tables.ParticleTable
import ca.joelathiessen.kaly2.core.slam.Particle
import ca.joelathiessen.kaly2.core.subconscious.SimPilotPoses
import lejos.robotics.navigation.Pose
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.util.Properties
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.coroutines.experimental.buildSequence

class RobotStorage(
    val sid: Long,
    private val serverName: String,
    dbUser: String,
    dbPassword: String,
    dbUrl: String
) {
    private val MAX_INSERTS = 2000
    private val dbPool = Executors.newSingleThreadExecutor()!!
    private val jdbcConnection: Connection

    var released = false
        private set

    fun makeInsertSeq(tableName: String, columnNames: List<String>): Sequence<String> {
        val seq = buildSequence {
            val insertStr = "insert into $tableName(${columnNames.joinToString()}) values"
            val questionMarks = (0 until columnNames.size).map { "?" }
            val valStr = "(${questionMarks.joinToString()})"
            var insertQuery = insertStr + valStr
            yield(insertQuery + ";")
            while (true) {
                insertQuery = insertQuery + ", " + valStr
                yield(insertQuery + ";")
            }
        }
        return seq
    }

    open class BatchPSInsertableValue
    class LongInsertable(val value: Long) : BatchPSInsertableValue()
    class BigDecimalInsertable(valIn: Float) : BatchPSInsertableValue() {
        val value = BigDecimal(valIn.toDouble())
    }

    class BatchPreparedStatement(val size: Int, val preparedStatement: PreparedStatement, var used: Boolean = false) {
        fun addBatch(valuesToInsert: List<BatchPSInsertableValue>) {
            valuesToInsert.forEachIndexed { index, ele ->
                when (ele) {
                    is LongInsertable -> {
                        preparedStatement.setLong(index + 1, ele.value)
                    }
                    is BigDecimalInsertable -> {
                        preparedStatement.setBigDecimal(index + 1, ele.value)
                    }
                }
            }
            preparedStatement.addBatch()
            used = true
        }

        fun executeBatch() {
            preparedStatement.executeBatch()
        }
    }

    // expects batchPreparedStatement to be sorted by size descending:
    fun insertIntoTable(valuesToInsert: List<BatchPSInsertableValue>, batchPreparedStatement: List<BatchPreparedStatement>) {
        var remaining = valuesToInsert.size
        while (remaining > 0) {
            for (stmt in batchPreparedStatement) {
                while (remaining >= stmt.size) {
                    val prevRemaining = remaining
                    remaining -= stmt.size
                    val start = valuesToInsert.size - prevRemaining
                    val end = valuesToInsert.size - remaining
                    val valuesToInsertWithBatch = valuesToInsert.subList(start, end)
                    stmt.addBatch(valuesToInsertWithBatch)
                }
            }
        }

        batchPreparedStatement.forEach {
            if (it.used) {
                it.executeBatch()
            }
        }
    }

    private val itrStr = "insert into iterations(sessionHistory, itr_num, itr_time, " +
            "slam_pose_x, slam_pose_y, slam_pose_heading, " +
            "real_pose_x, real_pose_y, real_pose_heading, " +
            "odo_pose_x, odo_pose_y, odo_pose_heading) " +
            "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
    private val itrStmt: PreparedStatement

    private val measurementStatements: List<BatchPreparedStatement>
    private val particleStatements: List<BatchPreparedStatement>
    private val featureStatements: List<BatchPreparedStatement>

    init {
        transaction {
            SessionHistoryEntity.findById(sid)
                    ?: throw IllegalArgumentException("RobotStorage requires a sessionHistory row to already exist")
        }

        val props = Properties()
        props.put("user", dbUser)
        props.put("password", dbPassword)
        jdbcConnection = DriverManager.getConnection(dbUrl, props)
        jdbcConnection.autoCommit = false

        measurementStatements = addPreparedStatements(jdbcConnection, "measurements",
                arrayListOf("iteration", "mes_num", "mes_time", "distance", "angle",
                        "guessed_pose_x", "guessed_pose_y", "guessed_pose_heading",
                        "odo_pose_x", "odo_pose_y", "odo_pose_heading"
                ))

        particleStatements = addPreparedStatements(jdbcConnection, "particles",
                arrayListOf("iteration", "x", "y", "heading"))

        featureStatements = addPreparedStatements(jdbcConnection, "features",
                arrayListOf("iteration", "sensor_x", "sensor_y", "distance", "angle", "std_dev"))

        itrStmt = jdbcConnection.prepareStatement(itrStr, PreparedStatement.RETURN_GENERATED_KEYS)
    }

    private fun addPreparedStatements(connection: Connection, tableName: String, columnNames: List<String>): List<BatchPreparedStatement> {
        val statementSql = makeInsertSeq(tableName, columnNames)
        val statements = ArrayList<BatchPreparedStatement>()
        var numInserts = 1
        while (numInserts < MAX_INSERTS) {
            val stmt = statementSql.take(numInserts).last()

            val prepStmt = connection.prepareStatement(stmt)
            statements.add(BatchPreparedStatement(numInserts * columnNames.size, prepStmt))

            numInserts *= 2
        }
        return statements.reversed()
    }

    private fun <T> doIfNotReleased(toDo: () -> T): Future<T> {
        return dbPool.submit(Callable {
            if (released) {
                throw IllegalStateException("RobotStorage can only save to a sessionHistory that it has not released")
            }
            toDo()
        }) as Future<T>
    }

    fun saveTimeStep(results: RobotCoreActedResults): Future<*> {
        val measurements = results.subconcResults.measurements
        val odoPose = results.subconcResults.pilotPoses.odoPose
        val realPose = (results.subconcResults.pilotPoses as? SimPilotPoses)?.realPose

        return doIfNotReleased {
            transaction {
                itrStmt.setLong(1, sid)
                itrStmt.setLong(2, results.numItrs)
                itrStmt.setLong(3, results.timestamp)

                itrStmt.setBigDecimal(4, BigDecimal(results.slamPose.x.toString()))
                itrStmt.setBigDecimal(5, BigDecimal(results.slamPose.y.toString()))
                itrStmt.setBigDecimal(6, BigDecimal(results.slamPose.heading.toString()))

                if (realPose != null) {
                    itrStmt.setBigDecimal(7, BigDecimal(realPose.x.toString()))
                    itrStmt.setBigDecimal(8, BigDecimal(realPose.y.toString()))
                    itrStmt.setBigDecimal(9, BigDecimal(realPose.heading.toString()))
                }

                itrStmt.setBigDecimal(10, BigDecimal(odoPose.x.toString()))
                itrStmt.setBigDecimal(11, BigDecimal(odoPose.y.toString()))
                itrStmt.setBigDecimal(12, BigDecimal(odoPose.heading.toString()))

                itrStmt.executeUpdate()
                val rs = itrStmt.generatedKeys
                rs.next()
                val iteration = LongInsertable(rs.getLong(1))
                connection.commit()

                var idx = 0L
                val mesValsToInsert = measurements.flatMap { mes ->
                    val mesNum = LongInsertable(idx)
                    val mesTime = LongInsertable(mes.time)
                    val distance = BigDecimalInsertable(mes.distance)
                    val angle = BigDecimalInsertable(mes.probAngle)
                    val guessedPoseX = BigDecimalInsertable(mes.probPose.x)
                    val guessedPoseY = BigDecimalInsertable(mes.probPose.y)
                    val guessedPoseHeading = BigDecimalInsertable(mes.probPose.heading)
                    val odoPoseX = BigDecimalInsertable(mes.odoPose.x)
                    val odoPoseY = BigDecimalInsertable(mes.odoPose.y)
                    val odoPoseHeading = BigDecimalInsertable(mes.odoPose.heading)

                    idx++

                    arrayListOf(iteration, mesNum, mesTime, distance, angle,
                            guessedPoseX, guessedPoseY, guessedPoseHeading, odoPoseX, odoPoseY, odoPoseHeading)
                }
                insertIntoTable(mesValsToInsert, measurementStatements)

                val particleValsToInsert = results.particlePoses.flatMap { partPose ->
                    val particlePoseX = BigDecimalInsertable(partPose.x)
                    val particlePoseY = BigDecimalInsertable(partPose.y)
                    val particlePoseHeading = BigDecimalInsertable(partPose.heading)

                    arrayListOf(iteration, particlePoseX, particlePoseY, particlePoseHeading)
                }
                insertIntoTable(particleValsToInsert, particleStatements)

                val featureValsToInsert = results.features.flatMap { feat ->
                    val featureSensorX = BigDecimalInsertable(feat.sensorX)
                    val featureSensorY = BigDecimalInsertable(feat.sensorY)
                    val featureSensorDist = BigDecimalInsertable(feat.distance)
                    val featureSensorAng = BigDecimalInsertable(feat.angle)
                    val featureSensorStdDev = BigDecimalInsertable(feat.stdDev)

                    arrayListOf(iteration, featureSensorX, featureSensorY, featureSensorDist, featureSensorAng, featureSensorStdDev)
                }
                insertIntoTable(featureValsToInsert, featureStatements)

                jdbcConnection.commit()
            }
        }
    }

    fun getSessionHistory(): SessionHistoryEntity {
        val ans = doIfNotReleased {
            transaction { SessionHistoryEntity.findById(sid)!! }
        }
        return ans.get()
    }

    data class MesSlamPair(val slamPose: RobotPose, val measurements: List<Measurement>, val itrNum: Long)

    fun getMeasurements(): List<MesSlamPair> {
        val pairs = ArrayList<MesSlamPair>()

        transaction {
            val sessionHistory = SessionHistoryEntity.findById(sid)!!

            val allItrs = IterationTable
                .innerJoin(SessionHistoryTable)
                .select { IterationTable.sessionHistory eq sessionHistory.id }
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

    fun saveHeartbeat(): Boolean {
        var success = false
        doIfNotReleased {
            transaction {
                val sessionHistory = SessionHistoryEntity.findById(sid)!!
                if (sessionHistory.ownerServer == serverName) {
                    sessionHistory.lastHeartbeat = System.currentTimeMillis()
                    success = true
                }
            }
        }.get()
        return success
    }

    fun releaseSessionHistory() {
        doIfNotReleased {
            transaction {
                val sessionHistory = SessionHistoryEntity.findById(sid)!!
                if (sessionHistory.ownerServer != serverName) {
                    throw IllegalStateException(
                        "RobotStorage can only release a sessionHistory that its server already owns"
                    )
                }
                sessionHistory.lastHeartbeat = System.currentTimeMillis()
                sessionHistory.owned = false
            }
            released = true
        }.get()
    }

    class Iteration(
        val itrNum: Long,
        val timestamp: Long,
        val slamPose: RobotPose,
        val odoPose: RobotPose,
        val measurements: List<Measurement>,
        val features: List<Feature>,
        val realPose: RobotPose?,
        val particles: List<Particle>
    )
    fun getIterations(firstItr: Long?, lastItr: Long?): List<Iteration> {
        val itrs = ArrayList<Iteration>()

        transaction {
            val sessionHistory = SessionHistoryEntity.findById(sid)!!
            val robot = sessionHistory.robot

            val allItrs = IterationTable
                    .innerJoin(SessionHistoryTable)
                    .select {
                        (IterationTable.sessionHistory eq sessionHistory.id)
                            .and (IterationTable.itrNum greaterEq firstItr)
                            .and (IterationTable.itrTime lessEq lastItr)
                    }
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

                var realPose: RobotPose? = null
                if (robot.physical == false) {
                    val realPoseX = it[IterationTable.realPoseX]
                    val realPoseY = it[IterationTable.realPoseY]
                    val realPoseHeading = it[IterationTable.realPoseHeading]

                    if (realPoseX != null && realPoseY != null && realPoseHeading != null) {
                        realPose = RobotPose(it[IterationTable.itrTime], 0f, realPoseX.toFloat(),
                                realPoseY.toFloat(), realPoseHeading.toFloat())
                    }
                }

                val odoPose = RobotPose(it[IterationTable.itrTime], 0f, it[IterationTable.odoPoseX].toFloat(),
                        it[IterationTable.odoPoseY].toFloat(), it[IterationTable.odoPoseHeading].toFloat())

                val timestamp = it[IterationTable.itrTime]

                val foundParticles = ParticleTable
                        .innerJoin(IterationTable)
                        .select { ParticleTable.iteration eq it[IterationTable.id] }
                        .toList()
                val particles = foundParticles.map {
                    val x = it[ParticleTable.x].toFloat()
                    val y = it[ParticleTable.y].toFloat()
                    val heading = it[ParticleTable.heading].toFloat()

                    Particle(Pose(x, y, heading))
                }

                val foundFeatures = FeatureTable
                        .innerJoin(IterationTable)
                        .select { FeatureTable.iteration eq it[IterationTable.id] }
                        .toList()
                val features = foundFeatures.map {
                    val sensorX = it[FeatureTable.sensorX].toFloat()
                    val sensorY = it[FeatureTable.sensorY].toFloat()
                    val distance = it[FeatureTable.distance].toFloat()
                    val angle = it[FeatureTable.angle].toFloat()
                    val stdDev = it[FeatureTable.stdDev].toFloat()

                    Feature(sensorX, sensorY, distance, angle, stdDev = stdDev)
                }

                itrs.add(Iteration(it[IterationTable.itrNum], timestamp, slamPose, odoPose, measurements, features, realPose, particles))
            }
        }

        return itrs
    }
}
