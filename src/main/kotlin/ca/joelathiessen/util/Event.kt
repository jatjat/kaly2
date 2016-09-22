package ca.joelathiessen.util

import java.util.*

/**
 * For a containing class, allows exposing a public event for subscription while keeping its invocation private
 */
class EventContainer<T>() {
    private val handlers = arrayListOf<(sender: Any, eventArgs: T) -> Unit>()
    val event : Event<T> = EventImpl(handlers)

    @Synchronized
    operator fun invoke(sender: Any, eventArgs: T) {
        handlers.forEach { it(sender, eventArgs) }
    }

    private class EventImpl<T>(private val handlers: ArrayList<(sender: Any, eventArgs: T) -> Unit>) : Event<T> {
        @Synchronized
        override operator fun plusAssign(handler: (sender: Any, eventArgs: T) -> Unit) { handlers.add(handler) }
        @Synchronized
        override operator fun minusAssign(handler: (sender: Any, eventArgs: T) -> Unit) { handlers.remove(handler) }

        override val length: Int
        @Synchronized
        get() = handlers.size
    }
}

interface Event<T> {
    operator fun plusAssign(handler: (sender: Any, eventArgs: T) -> Unit)
    operator fun minusAssign(handler: (sender: Any, eventArgs: T) -> Unit)
    val length: Int
}