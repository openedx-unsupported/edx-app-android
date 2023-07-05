package org.edx.mobile.view

import androidx.annotation.CallSuper
import org.edx.mobile.util.observer.MainThreadObservable
import org.edx.mobile.util.observer.Observable
import org.edx.mobile.util.observer.SubscriptionManager

abstract class ViewHoldingPresenter<V> : Presenter<V> {

    var view: V? = null
    private val viewSubscriptionManager = SubscriptionManager()

    @CallSuper
    override fun attachView(view: V) {
        this.view = view
    }

    @CallSuper
    override fun detachView() {
        view = null
        viewSubscriptionManager.unsubscribeAll()
    }

    @CallSuper
    override fun destroy() {
        viewSubscriptionManager.unsubscribeAll()
    }

    fun <T> observeOnView(observable: Observable<T>): Observable<T> {
        return viewSubscriptionManager.wrap(MainThreadObservable(observable))
    }
}
