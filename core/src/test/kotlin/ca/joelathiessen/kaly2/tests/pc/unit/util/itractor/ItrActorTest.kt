package ca.joelathiessen.kaly2.tests.pc.unit.util.itractor

import ca.joelathiessen.util.itractor.ItrActorChannel
import ca.joelathiessen.util.itractor.StopMsg
import org.junit.Assert.assertEquals
import org.junit.Test

class ItrActorTest {

    @Test
    fun testAct() {
        val channel = ItrActorChannel() // a Mockito & Kotlin bug prevents verifying a mocked channel was called
        channel.addMsg(StopMsg())
        val actor = ItrActorSampleImpl(channel)

        actor.act()

        assertEquals(channel.size(), 0)
    }
}