package org.edx.mobile.util.observer

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 * The advantage of this approach is that the user needs to specify the intention by using
 * [Event.getContentIfNotConsumed] or [Event.peekContent]. This method models the events as part of
 * the state: they’re now simply a message that has been consumed or not.
 *
 * Inspiration: https://medium.com/androiddevelopers/ac2622673150
 */
open class Event<out T>(private val content: T) {

    private val isConsumed = AtomicBoolean(false)

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotConsumed(): T? =
        if (isConsumed.compareAndSet(false, true)) content
        else null

    /**
     * Returns the content, even if it's already been consumed..
     */
    fun peekContent(): T = content
}
