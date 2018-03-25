package ca.joelathiessen.kaly2.persistence.tables

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable

object HistoryTable : LongIdTable("histories") {
    val map = reference("map", MapTable)
    val robot = reference("robot", RobotTable)

    val startDate = date("start_date")
    val owned = bool("owned")
    val ownerServer = uuid("owner_server")
    val lastHeartbeat = long("last_heartbeat")
}

class HistoryEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<HistoryEntity>(HistoryTable)

    var map by MapEntity referencedOn HistoryTable.map
    var robot by RobotEntity referencedOn HistoryTable.robot

    var startDate by HistoryTable.startDate
    var owned by HistoryTable.owned
    var ownerServer by HistoryTable.ownerServer
    var lastHeartbeat by HistoryTable.lastHeartbeat
}