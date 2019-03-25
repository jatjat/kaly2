package ca.joelathiessen.kaly2.android.repository

import ca.joelathiessen.kaly2.core.server.messages.PastSlamInfosResponse
import ca.joelathiessen.kaly2.core.server.messages.RTRobotMsg
import ca.joelathiessen.kaly2.core.server.messages.RTSlamInfoMsg
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RobotSessionRepository @Inject constructor(private val apiService: RobotSessionApiService) {
    private var currentSessionID: Long? = null // start unsubscribed
    private var lastReceivedItrs = HashMap<Long, Long>()
    private val iterationsCache = HashMap<Long, ArrayList<RTSlamInfoMsg>>()
    private var unsubscribing = false

    fun subscribeToRobotSession(sessionID: Long?, isReal: Boolean, subscriber: (RTRobotMsg) -> Unit,
                                onResponse: (sessionID: Long?) -> Unit, onFailure: (failure: RepositoryFailure) -> Unit) {
        if (unsubscribing == true) {
            onFailure(RepositoryIsUnsubscribingFailure())
        } else if (currentSessionID != null) {
            onFailure(RepositoryAlreadySubscribedFailure())
        } else {
            apiService.subscribeToRobotSession(sessionID, isReal, { rtRobotMsg: RTRobotMsg ->
                updateCaches(rtRobotMsg)
                subscriber(rtRobotMsg)
            }, { apiSessionID: Long? ->
                currentSessionID = apiSessionID
                onResponse(apiSessionID)
            }, { apiFailure: ApiServiceFailure ->
                onFailure(RepositoryApiServiceFailure(apiFailure))
            })
        }
    }

    private fun updateCaches(info: RTRobotMsg) {
        when (info) {
            is RTSlamInfoMsg -> {
                val sesID = info.sessionID
                lastReceivedItrs[sesID] = info.iteration

                val cachedItrs = iterationsCache[sesID] ?: ArrayList()
                cachedItrs.add(info)
                iterationsCache[sesID] = cachedItrs
            }
        }
    }

    fun unsubscribeFromRobotSession(onResponse: () -> Unit, onFailure: (failure: RepositoryFailure) -> Unit) {
        unsubscribing = true
        apiService.unsubscribeFromRobotSession({
            unsubscribing = false
            currentSessionID = null
            onResponse()
        }, { apiFailure: ApiServiceFailure ->
            onFailure(RepositoryApiServiceFailure(apiFailure))
        })
    }

    fun getPastIterations(onResponse: (pastIterations: List<RTSlamInfoMsg>?) -> Unit, onFailure: (failure: ApiServiceFailure) -> Unit) {
        val locSes: Long? = currentSessionID // currentSessionID is mutable so must use local reference
        if (locSes != null) {
            val cachedItrs = iterationsCache.get(locSes) ?: ArrayList()
            val lastReceivedItr = lastReceivedItrs.get(locSes)

            if (lastReceivedItr == null || lastReceivedItr >= cachedItrs.size) {
                // handle missing iterations or gaps:

                // get the starting point based on the first gap:
                val sorted = cachedItrs.map { it.iteration }.sorted()
                var noGapsIdx = 0
                for (i in 1 until sorted.size) {
                    if(sorted[i - 1] + 1 == sorted[i]) {
                        noGapsIdx = i
                    } else {
                        break
                    }
                }
                val firstDesiredItr = if (sorted.size > 0) sorted[noGapsIdx] + 1 else 0

                apiService.getPastIterations(firstDesiredItr, Int.MAX_VALUE.toLong(), { apiPastIterations: PastSlamInfosResponse ->
                    val slamInfos = apiPastIterations.slamInfos
                    cachedItrs.addAll(slamInfos)
                    iterationsCache[locSes] = cachedItrs
                    lastReceivedItrs[locSes] = (lastReceivedItr ?: 0) + slamInfos.size
                    onResponse(cachedItrs)
                }, { apiFailure: ApiServiceFailure ->
                    onFailure(apiFailure)
                })
            } else {
                onResponse(cachedItrs)
            }
        }
    }

    fun setRobotSessionSettings(shouldRun: Boolean, onResponse: () -> Unit, onFailure: (failure: RepositoryFailure) -> Unit) {
        apiService.setRobotSessionSettings(shouldRun, {
            onResponse()
        }, { apiFailure ->
            onFailure(RepositoryApiServiceFailure(apiFailure))
        })
    }

    fun setSlamSettings(numParticles: Int, sensorAngVar: Float, sensorDistVar: Float, onResponse: () -> Unit,
                        onFailure: (failure: RepositoryApiServiceFailure) -> Unit) {
        apiService.setSlamSettings(numParticles, sensorAngVar, sensorDistVar, {
            onResponse()
        }, { apiFailure ->
            onFailure(RepositoryApiServiceFailure(apiFailure))
        })
    }
}

