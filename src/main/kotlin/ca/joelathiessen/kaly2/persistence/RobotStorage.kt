package ca.joelathiessen.kaly2.persistence

import ca.joelathiessen.kaly2.Measurement
import ca.joelathiessen.kaly2.RobotCoreActedResults
import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.persistence.tables.HistoryEntity
import ca.joelathiessen.kaly2.persistence.tables.HistoryTable
import ca.joelathiessen.kaly2.persistence.tables.IterationEntity
import ca.joelathiessen.kaly2.persistence.tables.IterationTable
import ca.joelathiessen.kaly2.persistence.tables.MeasurementTable
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

class RobotStorage(private val histid: Long, private val serverUUID: UUID) {
    private val dbPool = Executors.newSingleThreadExecutor()!!

    var released = false
        private set

    init {
        transaction {
            HistoryEntity.findById(histid)
                ?: throw IllegalArgumentException("RobotStorage requires a history row to already exist")
        }
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
        doIfNotReleased {
            transaction {
                val newIteration = IterationEntity.new {
                    history = HistoryEntity.findById(histid)!!
                    slamPoseX = BigDecimal(results.slamPose.x.toString())
                    slamPoseY = BigDecimal(results.slamPose.y.toString())
                    slamPoseHeading = BigDecimal(results.slamPose.heading.toString())

                    itrNum = results.numItrs
                    itrTime = results.timestamp
                }

                var mesIdx = 0L
                MeasurementTable.batchInsert(results.subconcResults.measurements) {
                    this[MeasurementTable.iteration] = newIteration.id

                    this[MeasurementTable.mesNum] = mesIdx
                    this[MeasurementTable.mesTime] = it.time
                    this[MeasurementTable.distance] = BigDecimal(it.distance.toString())
                    this[MeasurementTable.angle] = BigDecimal(it.probAngle.toString())

                    this[MeasurementTable.guessedPoseX] = BigDecimal(it.probPose.x.toString())
                    this[MeasurementTable.guessedPoseY] = BigDecimal(it.probPose.y.toString())
                    this[MeasurementTable.guessedPoseHeading] = BigDecimal(it.probPose.heading.toString())

                    this[MeasurementTable.odoPoseX] = BigDecimal(it.odoPose.x.toString())
                    this[MeasurementTable.odoPoseY] = BigDecimal(it.odoPose.y.toString())
                    this[MeasurementTable.odoPoseHeading] = BigDecimal(it.odoPose.heading.toString())

                    mesIdx++
                }
            }
        }
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
