package ca.joelathiessen.kaly2.core.persistence.tables

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable

object MapTable : LongIdTable("maps") {
    val NAME_LENGTH = 20
    val name = varchar("name", NAME_LENGTH)
}

class MapEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<MapEntity>(MapTable)

    var name by MapTable.name
}