package ca.joelathiessen.kaly2.core.persistence.tables

import ca.joelathiessen.util.DEC_FOR_FLT_PREC
import ca.joelathiessen.util.DEC_FOR_FLT_SCALE
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable

object ParticleTable : LongIdTable("particles") {
    val iteration = reference("iteration", IterationTable)

    val x = decimal("x", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val y = decimal("y", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val heading = decimal("heading", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
}

class ParticlesEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ParticlesEntity>(ParticleTable)

    var x by ParticleTable.x
    var y by ParticleTable.y
    var heading by ParticleTable.heading
}