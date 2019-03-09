package ca.joelathiessen.kaly2.android.repository

import ca.joelathiessen.kaly2.core.server.messages.PastSlamInfosResponse
import ca.joelathiessen.kaly2.core.server.messages.RTRobotMsg

interface RobotSessionApiService {

    fun subscribeToRobotSession(sessionID: Long?, subscriber: (RTRobotMsg) -> Unit,
                                onResponse: (sessionID: Long) -> Unit,
                                onFailure: (error: ApiServiceFailure) -> Unit)

    fun unsubscribeFromRobotSession(onResponse: () -> Unit, onFailure: (failure: ApiServiceFailure) -> Unit)

    fun getPastIterations(firstItr: Long, lastItr: Long, onResponse: (pastIterations: PastSlamInfosResponse) -> Unit,
                          onFailure: (failure: ApiServiceFailure) -> Unit)

    fun setRobotSessionSettings(shouldRun: Boolean, onResponse: () -> Unit,
                                onFailure: (failure: ApiServiceFailure) -> Unit)

    fun setSlamSettings(numParticles: Int, sensorAngVar: Float, sensorDistVar: Float, onResponse: () -> Unit,
                        onFailure: (failure: ApiServiceFailure) -> Unit)
}
