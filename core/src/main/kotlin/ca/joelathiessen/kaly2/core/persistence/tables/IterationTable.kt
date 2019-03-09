package ca.joelathiessen.kaly2.core.persistence.tables

import ca.joelathiessen.util.DEC_FOR_FLT_PREC
import ca.joelathiessen.util.DEC_FOR_FLT_SCALE
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable

object IterationTable : LongIdTable("iterations") {
    val sessionHistory = reference("sessionHistory", SessionHistoryTable)

    val itrNum = long("itr_num")
    val itrTime = long("itr_time")

    val slamPoseX = decimal("slam_pose_x", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val slamPoseY = decimal("slam_pose_y", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val slamPoseHeading = decimal("slam_pose_heading", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)

    val realPoseX = decimal("real_pose_x", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE).nullable()
    val realPoseY = decimal("real_pose_y", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE).nullable()
    val realPoseHeading = decimal("real_pose_heading", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE).nullable()

    val odoPoseX = decimal("odo_pose_x", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val odoPoseY = decimal("odo_pose_y", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val odoPoseHeading = decimal("odo_pose_heading", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE) }

class IterationEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<IterationEntity>(IterationTable)

    var sessionHistory by SessionHistoryEntity referencedOn IterationTable.sessionHistory

    var itrNum by IterationTable.itrNum
    var itrTime by IterationTable.itrTime

    var slamPoseX by IterationTable.slamPoseX
    var slamPoseY by IterationTable.slamPoseY
    var slamPoseHeading by IterationTable.slamPoseHeading

    var realPoseX by IterationTable.realPoseX
    var realPoseY by IterationTable.realPoseY
    var realPoseHeading by IterationTable.realPoseHeading

    var odoPoseX by IterationTable.odoPoseX
    var odoPoseY by IterationTable.odoPoseY
    var odoPoseHeading by IterationTable.odoPoseHeading
}