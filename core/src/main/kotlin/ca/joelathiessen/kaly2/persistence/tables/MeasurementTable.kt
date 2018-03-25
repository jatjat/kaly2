package ca.joelathiessen.kaly2.persistence.tables

import ca.joelathiessen.util.DEC_FOR_FLT_PREC
import ca.joelathiessen.util.DEC_FOR_FLT_SCALE
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable

object MeasurementTable : LongIdTable("measurements") {
    val iteration = reference("iteration", IterationTable)

    val mesNum = long("mes_num")
    val mesTime = long("mes_time")
    val distance = decimal("distance", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val angle = decimal("angle", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)

    // to allow using a single measurements insert per iteration, don't store robot poses in a separate table:
    val guessedPoseX = decimal("guessed_pose_x", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val guessedPoseY = decimal("guessed_pose_y", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val guessedPoseHeading = decimal("guessed_pose_heading", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val odoPoseX = decimal("odo_pose_x", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val odoPoseY = decimal("odo_pose_y", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val odoPoseHeading = decimal("odo_pose_heading", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
}

class MeasurementEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<MeasurementEntity>(MeasurementTable)

    var iteration by IterationEntity referencedOn MeasurementTable.iteration

    var mesNum by MeasurementTable.mesNum
    var mesTime by MeasurementTable.mesTime
    var distance by MeasurementTable.distance
    var angle by MeasurementTable.angle

    var guessedPoseX by MeasurementTable.guessedPoseX
    var guessedPoseY by MeasurementTable.guessedPoseY
    var guessedPoseHeading by MeasurementTable.guessedPoseHeading
    var odoPoseX by MeasurementTable.odoPoseX
    var odoPoseY by MeasurementTable.odoPoseY
    var odoPoseHeading by MeasurementTable.odoPoseHeading
}