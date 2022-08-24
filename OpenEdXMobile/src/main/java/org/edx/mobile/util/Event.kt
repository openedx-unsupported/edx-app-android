package org.edx.mobile.util

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
open class Event<out T>(private val content: T) {

    private val isConsumed = AtomicBoolean(false)

    /**
     * Returns the content and prevents its use again.
     */
    internal fun getContentIfNotConsumed(): T? =
        if (isConsumed.compareAndSet(false, true)) content
        else null

    /**
     * Returns the content, even if it's already been consumed..
     */
    fun peekContent(): T = content
}
