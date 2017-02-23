package ca.joelathiessen.util

import java.util.*

// It doesn't seem to be possible to overload a function with an extension function
class FloatRandom(seed: Long) {
    private val random = Random(seed)

    fun nextGaussian(): Float {
        return random.nextGaussian().toFloat()
    }
}