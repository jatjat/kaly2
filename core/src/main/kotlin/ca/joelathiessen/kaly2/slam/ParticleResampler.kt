package ca.joelathiessen.kaly2.slam

import java.util.ArrayList

interface ParticleResampler {
    fun resample(particles: ArrayList<Particle>): ArrayList<Particle>
}