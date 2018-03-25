package ca.joelathiessen.util

import java.util.ArrayList

/**
 * For a containing class, allows exposing a public event for subscription while keeping its invocation private
 */
class EventContainer<T>(onHandlerAdded: (() -> Unit)? = null,
                        onHandlerRemoved: (() -> Unit)? = null) {
    private val handlers = arrayListOf<(sender: Any, eventArgs: T) -> Unit>()
    val event: Event<T> = EventImpl(handlers, onHandlerAdded, onHandlerRemoved)

    @Synchronized
    operator fun invoke(sender: Any, eventArgs: T) {
        handlers.forEach { it(sender, eventArgs) }
    }

    private class EventImpl<T>(private val handlers: ArrayList<(sender: Any, eventArgs: T) -> Unit>,
                               private val onHandlerAdded: (() -> Unit)? = null,
                               private val onHandlerRemoved: (() -> Unit)? = null)
        : Event<T> {
        @Synchronized
        override operator fun plusAssign(handler: (sender: Any, eventArgs: T) -> Unit) {
            handlers.add(handler)
            onHandlerAdded?.invoke()
        }

        @Synchronized
        override operator fun minusAssign(handler: (sender: Any, eventArgs: T) -> Unit) {
            handlers.remove(handler)
            onHandlerRemoved?.invoke()
        }

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