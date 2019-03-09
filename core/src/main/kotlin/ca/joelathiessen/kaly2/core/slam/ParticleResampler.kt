package ca.joelathiessen.kaly2.core.slam

import java.util.ArrayList

interface ParticleResampler {
    fun resample(particles: ArrayList<Particle>): ArrayList<Particle>
}