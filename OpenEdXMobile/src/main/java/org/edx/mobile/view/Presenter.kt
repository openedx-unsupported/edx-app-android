package org.edx.mobile.view

interface Presenter<V> {
    fun attachView(view: V)
    fun detachView()
    fun destroy()
}
