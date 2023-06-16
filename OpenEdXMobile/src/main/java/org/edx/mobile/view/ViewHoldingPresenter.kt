package org.edx.mobile.view;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.edx.mobile.util.observer.MainThreadObservable;
import org.edx.mobile.util.observer.Observable;
import org.edx.mobile.util.observer.SubscriptionManager;

public abstract class ViewHoldingPresenter<V> implements Presenter<V> {

    @Nullable
    private V view;

    @NonNull
    private final SubscriptionManager viewSubscriptionManager = new SubscriptionManager();

    @Override
    @CallSuper
    public void attachView(@NonNull V view) {
        this.view = view;
    }

    @Override
    @CallSuper
    public void detachView() {
        this.view = null;
        viewSubscriptionManager.unsubscribeAll();
    }

    @Override
    @CallSuper
    public void destroy() {
        viewSubscriptionManager.unsubscribeAll();
    }

    @NonNull
    public <T> Observable<T> observeOnView(@NonNull Observable<T> observable) {
        return viewSubscriptionManager.wrap(new MainThreadObservable<>(observable));
    }

    @Nullable
    public V getView() {
        return view;
    }

}
