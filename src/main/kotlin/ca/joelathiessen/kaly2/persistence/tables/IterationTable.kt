package ca.joelathiessen.kaly2.persistence.tables

import ca.joelathiessen.util.DEC_FOR_FLT_PREC
import ca.joelathiessen.util.DEC_FOR_FLT_SCALE
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable

object IterationTable : LongIdTable("iterations") {
    val history = reference("history", HistoryTable)

    val itrNum = long("itr_num")
    val itrTime = long("itr_time")

    val slamPoseX = decimal("slam_pose_x", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val slamPoseY = decimal("slam_pose_y", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val slamPoseHeading = decimal("slam_pose_heading", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
}

class IterationEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<IterationEntity>(IterationTable)

    var history by HistoryEntity referencedOn IterationTable.history

    var itrNum by IterationTable.itrNum
    var itrTime by IterationTable.itrTime

    var slamPoseX by IterationTable.slamPoseX
    var slamPoseY by IterationTable.slamPoseY
    var slamPoseHeading by IterationTable.slamPoseHeading
}