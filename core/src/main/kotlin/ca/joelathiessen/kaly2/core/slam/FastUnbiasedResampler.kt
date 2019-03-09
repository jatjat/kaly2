package ca.joelathiessen.kaly2.core.slam

import ca.joelathiessen.util.FloatMath
import ca.joelathiessen.util.sumByFloat
import java.util.ArrayList

class FastUnbiasedResampler : ParticleResampler {

    override fun resample(particles: ArrayList<Particle>): ArrayList<Particle> {
        val oldParticles = particles
        val numOldParticles = oldParticles.size
        val newParticlesList = ArrayList<Particle>(numOldParticles)

        val weightSum = oldParticles.sumByFloat { it.weight }
        oldParticles.forEach { it.weight = it.weight / weightSum }

        var currWeight = oldParticles[0].weight

        val randomNum = Math.random() / numOldParticles
        var count = 1
        for (m in 1..numOldParticles) {
            val maxWeight = randomNum + (m - 1.0f) * FloatMath.pow(1.0f * numOldParticles, -1.0f)

            while (currWeight < maxWeight) {
                count++
                currWeight += oldParticles[count - 1].weight
            }

            newParticlesList.add(oldParticles[count - 1].copy())
        }

        newParticlesList.forEach { it.weight = 1.0f / newParticlesList.size }

        return newParticlesList
    }
}