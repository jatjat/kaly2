package ca.joelathiessen.kaly2.core.tests.pc.unit.util.itractor

import ca.joelathiessen.util.itractor.ItrActorChannel
import ca.joelathiessen.util.itractor.StopMsg
import org.junit.Assert.assertEquals
import org.junit.Test

class ItrActorChannelTest {

    @Test
    fun channelAddTakeSizeTest() {
        val channel = ItrActorChannel()

        assertEquals(channel.size(), 0)

        channel.addMsg(StopMsg())

        assertEquals(channel.size(), 1)

        channel.addMsg(StopMsg())

        assertEquals(channel.size(), 2)

        channel.takeMsg()

        assertEquals(channel.size(), 1)

        channel.takeMsg()

        assertEquals(channel.size(), 0)
    }

    @Test
    fun channelAddTakeOrderTest() {
        val channel = ItrActorChannel()

        assertEquals(channel.size(), 0)

        val msg1 = StopMsg()
        val msg2 = StopMsg()

        channel.addMsg(msg1)
        channel.addMsg(msg2)

        assertEquals(channel.takeMsg(), msg1)
        assertEquals(channel.takeMsg(), msg2)
    }
}