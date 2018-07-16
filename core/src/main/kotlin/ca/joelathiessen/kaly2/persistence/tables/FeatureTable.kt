package ca.joelathiessen.kaly2.persistence.tables

import ca.joelathiessen.util.DEC_FOR_FLT_PREC
import ca.joelathiessen.util.DEC_FOR_FLT_SCALE
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable

object FeatureTable : LongIdTable("features") {
    val iteration = reference("iteration", IterationTable)

    val sensorX = decimal("sensor_x", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val sensorY = decimal("sensor_y", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val distance = decimal("distance", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val angle = decimal("angle", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
    val stdDev = decimal("std_dev", DEC_FOR_FLT_PREC, DEC_FOR_FLT_SCALE)
}

class FeaturesEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<FeaturesEntity>(FeatureTable)

    var sensorX by FeatureTable.sensorX
    var sensorY by FeatureTable.sensorY
    var distance by FeatureTable.distance
    var angle by FeatureTable.angle
    var stdDev by FeatureTable.stdDev
}
