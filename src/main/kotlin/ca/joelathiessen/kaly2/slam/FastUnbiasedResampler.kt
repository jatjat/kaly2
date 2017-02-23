package ca.joelathiessen.kaly2.slam

import ca.joelathiessen.util.FloatMath
import java.util.*
import ca.joelathiessen.util.sumByFloat

class FastUnbiasedResampler : ParticleResampler {

    override fun resample(oldParticles: ArrayList<Particle>): ArrayList<Particle> {
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