package ca.joelathiessen.kaly2.core.tests.pc.unit.server

import ca.joelathiessen.kaly2.core.server.KalyWebSocket
import ca.joelathiessen.kaly2.core.server.RobotSession
import ca.joelathiessen.kaly2.core.server.RobotSessionManager
import ca.joelathiessen.kaly2.core.server.messages.RTMsg
import ca.joelathiessen.kaly2.core.server.messages.RTPose
import ca.joelathiessen.kaly2.core.server.messages.RTSlamInfoMsg
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.eclipse.jetty.websocket.WebSocket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.atLeastOnce

class KalyWebSocketTest {

    // The page from February 2018 starts a robot without subscribing to one first. Until the
    // 2018-19 page replaces it on the live site, kaly2 deploys first and has to work with it
    @Test
    fun startsARobotForThePageFromFebruary2018() {
        val session = mock<RobotSession>()
        whenever(session.sid).thenReturn(7L)
        whenever(session.applyRobotSessionSettings(any())).thenReturn(true)
        val manager = mock<RobotSessionManager>()
        whenever(manager.getHandler(null)).thenReturn(RobotSessionManager.GetHandlerResult.RobotSessionResult(session))
        val connection = mock<WebSocket.Connection>()

        val socket = KalyWebSocket(manager)
        socket.onOpen(connection)
        socket.onMessage("""{"msgType":"robotSessionSettings","msg":{"shouldRun":true,"shouldReset":false}}""")

        verify(session).subscribeToRTEvents(any())
        verify(session).applyRobotSessionSettings(any())
        val sent = argumentCaptor<String>()
        verify(connection, atLeastOnce()).sendMessage(sent.capture())
        val reply = JsonParser().parse(sent.lastValue).asJsonObject
        assertEquals("robotSessionSettings", reply["msgType"].asString)
        assertEquals(7L, reply["msg"].asJsonObject["sessionID"].asLong)
        assertEquals(true, reply["msg"].asJsonObject["isRunning"].asBoolean)
        // That page only draws the robot once the reply says it hasn't just reset
        assertEquals(false, reply["msg"].asJsonObject["hasJustReset"]?.asBoolean)
    }

    // The February 2018 page reads the simulation's landmarks from every step, and draws nothing
    // if they're missing
    @Test
    fun stepsHaveTheLandmarksTheFebruary2018PageReads() {
        val pose = RTPose(1f, 2f, 0f)
        val step = RTSlamInfoMsg(7L, 1L, 0L, ArrayList(), ArrayList(), pose, pose, pose)

        val sent = JsonParser().parse(GsonBuilder().create().toJson(RTMsg(step))).asJsonObject

        assertTrue(sent["msg"].asJsonObject["trueLandmarks"]?.isJsonArray ?: false)
    }
}
