package ca.joelathiessen.kaly2.core.tests.pc.unit.util.itractor

import ca.joelathiessen.util.itractor.ItrActorChannel
import ca.joelathiessen.util.itractor.ItrActorMsg
import ca.joelathiessen.util.itractor.ItrActorThreadedHost
import org.junit.Assert.assertEquals
import org.junit.Test

class ItrActorThreadedHostTest {

    @Test
    fun startStopTest() {
        val channel = ItrActorChannel()
        val actor = ItrActorSampleImpl(channel)
        val host = ItrActorThreadedHost(actor)
        channel.addMsg(object : ItrActorMsg() {})

        host.start()
        host.stop()

        assertEquals(0, channel.size())
    }

    @Test
    fun stopBlocksTest() {
        val channel = ItrActorChannel()
        val actor = ItrActorSampleImpl(channel)
        val host = ItrActorThreadedHost(actor)
        for (i in 1..1000) {
            channel.addMsg(object : ItrActorMsg() {})
        }

        host.start()
        host.stop()

        assertEquals(0, channel.size())
    }

    @Test
    fun startStopMultipleTest() {
        val channel = ItrActorChannel()
        val actor = ItrActorSampleImpl(channel)
        val host = ItrActorThreadedHost(actor)
        channel.addMsg(object : ItrActorMsg() {})
        channel.addMsg(object : ItrActorMsg() {})

        host.start()
        channel.addMsg(object : ItrActorMsg() {})
        channel.addMsg(object : ItrActorMsg() {})
        host.stop()
        channel.addMsg(object : ItrActorMsg() {})
        channel.addMsg(object : ItrActorMsg() {})
        host.start()
        channel.addMsg(object : ItrActorMsg() {})
        channel.addMsg(object : ItrActorMsg() {})
        host.stop()

        assertEquals(0, channel.size())
    }

    @Test
    fun startMultipleTest() {
        val channel = ItrActorChannel()
        val actor = ItrActorSampleImpl(channel)
        val host = ItrActorThreadedHost(actor)
        channel.addMsg(object : ItrActorMsg() {})

        host.start()
        host.start()
        host.stop()

        assertEquals(0, channel.size())
    }

    @Test
    fun stopMultipleTest() {
        val channel = ItrActorChannel()
        val actor = ItrActorSampleImpl(channel)
        val host = ItrActorThreadedHost(actor)
        channel.addMsg(object : ItrActorMsg() {})

        host.start()
        host.stop()
        host.stop()

        assertEquals(0, channel.size())
    }
}
