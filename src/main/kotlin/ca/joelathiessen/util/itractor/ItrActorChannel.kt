package ca.joelathiessen.util.itractor

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class ItrActorChannel(bufferSize: Int = 0) : ItrActorChannelAdder {
    private val mailbox: BlockingQueue<ItrActorMsg>

    init {
        if(bufferSize <= 0) {
            mailbox = LinkedBlockingQueue<ItrActorMsg>()
        } else {
            mailbox = ArrayBlockingQueue<ItrActorMsg>(bufferSize)
        }
    }

    override fun addMsg(msg: ItrActorMsg) {
        mailbox.put(msg)
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
