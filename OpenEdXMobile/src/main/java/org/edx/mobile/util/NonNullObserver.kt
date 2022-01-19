package org.edx.mobile.util

import androidx.lifecycle.Observer

class NonNullObserver<T>(private val consumer: (content: T) -> Unit) : Observer<T?> {
    override fun onChanged(t: T?) {
        consumer(t ?: return)
    }
}
