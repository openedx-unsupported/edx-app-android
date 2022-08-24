package org.edx.mobile.util

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/**
 * A non-nullable observer for [Event] class that returns the value of the event if it is not yet
 * consumed.
 */
class EventObserver<T>(private val consumer: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(event: Event<T>?) {
        consumer(event?.getContentIfNotConsumed() ?: return)
    }
}

fun <T> MutableLiveData<Event<T>>.postEvent(content: T) {
    postValue(Event(content))
}
