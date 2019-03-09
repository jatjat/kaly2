package ca.joelathiessen.kaly2.core.persistence.tables

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable

object SessionHistoryTable : LongIdTable("session_histories") {
    val map = reference("map", MapTable)
    val robot = reference("robot", RobotTable)

    val startDate = date("start_date")
    val owned = bool("owned")
    val ownerServer = text("owner_server")
    val lastHeartbeat = long("last_heartbeat")
}

class SessionHistoryEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<SessionHistoryEntity>(SessionHistoryTable)

    var map by MapEntity referencedOn SessionHistoryTable.map
    var robot by RobotEntity referencedOn SessionHistoryTable.robot

    var startDate by SessionHistoryTable.startDate
    var owned by SessionHistoryTable.owned
    var ownerServer by SessionHistoryTable.ownerServer
    var lastHeartbeat by SessionHistoryTable.lastHeartbeat
}