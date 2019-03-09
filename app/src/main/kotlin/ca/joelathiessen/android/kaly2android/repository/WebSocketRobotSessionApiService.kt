package ca.joelathiessen.android.kaly2android.repository

import ca.joelathiessen.kaly2.server.messages.PastSlamInfosResponse
import ca.joelathiessen.kaly2.server.messages.RTRobotMsg

class WebSocketRobotSessionApiService: RobotSessionApiService {
    override fun subscribeToRobotSession(sessionID: Long?, subscriber: (RTRobotMsg) -> Unit,
                                         onResponse: (sessionID: Long) -> Unit,
                                         onFailure: (error: ApiServiceFailure) -> Unit) {
        TODO("not implemented")
    }

    override fun unsubscribeFromRobotSession(onResponse: () -> Unit, onFailure: (failure: ApiServiceFailure) -> Unit) {
        TODO("not implemented")
    }

    override fun getPastIterations(firstItr: Long, lastItr: Long,
                                   onResponse: (pastIterations: PastSlamInfosResponse) -> Unit,
                                   onFailure: (failure: ApiServiceFailure) -> Unit) {
        TODO("not implemented")
    }

    override fun setRobotSessionSettings(shouldRun: Boolean, onResponse: () -> Unit,
                                         onFailure: (failure: ApiServiceFailure) -> Unit) {
        TODO("not implemented")
    }

    override fun setSlamSettings(numParticles: Int, sensorAngVar: Float, sensorDistVar: Float, onResponse: () -> Unit,
                                 onFailure: (failure: ApiServiceFailure) -> Unit) {
        TODO("not implemented")
    }
}