package ca.joelathiessen.kaly2.subconscious.sensor

interface Spinnable {
    val spinning: Boolean
    val turningClockwise: Boolean

    fun spin()
}