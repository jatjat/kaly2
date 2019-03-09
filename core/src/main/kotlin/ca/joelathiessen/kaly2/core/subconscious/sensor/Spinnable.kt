package ca.joelathiessen.kaly2.core.subconscious.sensor

interface Spinnable {
    val spinning: Boolean
    val turningClockwise: Boolean

    fun spin()
}