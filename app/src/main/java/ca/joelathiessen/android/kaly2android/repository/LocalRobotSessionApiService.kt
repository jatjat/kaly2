package ca.joelathiessen.android.kaly2android.repository

import ca.joelathiessen.kaly2.server.KalyServer
import ca.joelathiessen.kaly2.server.RobotSession
import ca.joelathiessen.kaly2.server.messages.PastSlamInfosRequest
import ca.joelathiessen.kaly2.server.messages.PastSlamInfosResponse
import ca.joelathiessen.kaly2.server.messages.RTMsg
import ca.joelathiessen.kaly2.server.messages.RTRobotMsg
import ca.joelathiessen.kaly2.server.messages.RobotSessionSettingsRequest
import ca.joelathiessen.kaly2.server.messages.SlamSettingsResponse
import java.util.concurrent.Executor
import javax.inject.Inject


// Presents a single local robot session asynchronously
class LocalRobotSessionApiService @Inject constructor(private val server: KalyServer,
                                                      private val subscriberThreadExecutor: Executor
) : RobotSessionApiService {
    private var session: RobotSession? = null
    private var rtMessageHandler: ((Any, RTMsg) -> Unit)? = null
    private var subscriber: ((RTRobotMsg) -> Unit)? = null

    init {
        server.serve()
    }

    override fun subscribeToRobotSession(sessionID: Long?, subscriber: (RTRobotMsg) -> Unit,
                                         onResponse: (sessionID: Long) -> Unit, onFailure: (failure: ApiServiceFailure) -> Unit) {
        val isNotSubscribedToAnotherSession = session == null || (sessionID != session?.sid && sessionID != null)
        if (isNotSubscribedToAnotherSession) {
            session = server.inprocessAPI.getRobotSession(sessionID)

            val ses = session
            if (ses != null) {
                val rtMsgHand = { sender: Any, eventArgs: RTMsg -> handleRTMessage(sender, eventArgs) }
                this.subscriber = subscriber
                rtMessageHandler = rtMsgHand
                ses.subscribeToRTEvents(rtMsgHand)
                onResponse(ses.sid)
            } else {
                onFailure(NoSessionObtainedFailure())
            }
        } else {
            onFailure(SubscribedToAnotherSessionFailure())
        }
    }

    override fun unsubscribeFromRobotSession(onResponse: () -> Unit, onFailure: (failure: ApiServiceFailure) -> Unit) {
        session?.unsubscribeFromRTEvents(this.rtMessageHandler)
        session = null
        rtMessageHandler = null
        onResponse()
    }

    override fun getPastIterations(firstItr: Long, lastItr: Long,
                                   onResponse: (pastIterations: PastSlamInfosResponse) -> Unit,
                                   onFailure: (failure: ApiServiceFailure) -> Unit) {
        val ses = session
        if (ses != null) {
            val itrs = ses.getPastIterations(PastSlamInfosRequest(ses.sid, firstItr, lastItr))
            onResponse(itrs)
        } else {
            onFailure(NoSessionPresentFailure())
        }
    }

    override fun setRobotSessionSettings(shouldRun: Boolean, onResponse: () -> Unit,
                                         onFailure: (failure: ApiServiceFailure) -> Unit) {
        val ses = session
        if (ses != null) {
            ses.applyRobotSessionSettings(RobotSessionSettingsRequest(ses.sid, shouldRun))
            onResponse()
        } else {
            onFailure(NoSessionPresentFailure())
        }
    }

    override fun setSlamSettings(numParticles: Int, sensorAngVar: Float, sensorDistVar: Float, onResponse: () -> Unit,
                                 onFailure: (failure: ApiServiceFailure) -> Unit) {
        val ses = session
        if (ses != null) {
            ses.applySlamSettings(SlamSettingsResponse(ses.sid, numParticles, sensorDistVar, sensorAngVar))
            onResponse()
        } else {
            onFailure(NoSessionPresentFailure())
        }
    }

    private fun handleRTMessage(@Suppress("UNUSED_PARAMETER") sender:Any, eventArgs: RTMsg) {
        subscriberThreadExecutor.execute {
            subscriber?.invoke(eventArgs.msg)
        }
    }
}
