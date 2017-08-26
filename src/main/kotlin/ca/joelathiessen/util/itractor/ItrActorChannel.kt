package ca.joelathiessen.util.itractor

import java.util.concurrent.LinkedBlockingQueue

class ItrActorChannel : ItrActorChannelAdder {
    private val mailbox = LinkedBlockingQueue<ItrActorMsg>()

    override fun addMsg(msg: ItrActorMsg) {
        mailbox.add(msg)
    }

    fun takeMsg(): ItrActorMsg {
        return mailbox.take()
    }

    fun size(): Int {
        return mailbox.size
    }

    fun isEmpty(): Boolean {
        return mailbox.isEmpty()
    }
}
