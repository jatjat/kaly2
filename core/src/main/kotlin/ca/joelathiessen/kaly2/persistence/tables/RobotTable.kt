package ca.joelathiessen.kaly2.persistence.tables

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable

object RobotTable : LongIdTable("robots") {
    val NAME_LENGTH = 20
    val name = varchar("name", NAME_LENGTH)
    val physical = bool("physical")
}

class RobotEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<RobotEntity>(RobotTable)

    var name by RobotTable.name
    var physical by RobotTable.physical
}