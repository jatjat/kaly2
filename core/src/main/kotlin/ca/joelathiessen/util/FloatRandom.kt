package ca.joelathiessen.util

import java.util.Random

// It doesn't seem to be possible to overload a function with an extension function
class FloatRandom(seed: Long) {
    private val random = Random(seed)

    fun nextGaussian(): Float {
        return random.nextGaussian().toFloat()
    }

    fun nextFloat(): Float {
        return random.nextFloat()
    }
}