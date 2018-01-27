package ca.joelathiessen.kaly2.persistence.tables

import ca.joelathiessen.util.DEC_FOR_FLT_PREC
import ca.joelathiessen.util.DEC_FOR_FLT_SCALE
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable

object NRobotPoses : LongIdTable("robot_poses") {
    val x = decimal("x", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val y = decimal("y", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val heading = decimal("heading", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val poseTime = long("pose_time")
}

class NRobotPose(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<NRobotPose>(NRobotPoses)

    var x by NRobotPoses.x
    var y by NRobotPoses.y
    var heading by NRobotPoses.heading
    var poseTime by NRobotPoses.poseTime
}