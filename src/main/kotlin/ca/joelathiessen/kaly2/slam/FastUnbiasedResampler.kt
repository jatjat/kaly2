package ca.joelathiessen.kaly2.slam

import java.util.*

class FastUnbiasedResampler : ParticleResampler {

    override fun resample(oldParticles: ArrayList<Particle>): ArrayList<Particle> {
        val numOldParticles = oldParticles.size
        val newParticlesList = ArrayList<Particle>(numOldParticles)

        val weightSum = oldParticles.sumByDouble { it.weight }
        oldParticles.forEach { it.weight = it.weight / weightSum }

        var currWeight = oldParticles[0].weight

        val randomNum = Math.random() / numOldParticles
        var count = 1
        for (m in 1..numOldParticles) {
            val maxWeight = randomNum + (m - 1.0) * Math.pow(1.0 * numOldParticles, -1.0)

            while (currWeight < maxWeight) {
                count++
                currWeight += oldParticles[count - 1].weight
            }

            newParticlesList.add(oldParticles[count - 1].copy())
        }

        newParticlesList.forEach { it.weight = 1.0 / newParticlesList.size }

        return newParticlesList
    }
}