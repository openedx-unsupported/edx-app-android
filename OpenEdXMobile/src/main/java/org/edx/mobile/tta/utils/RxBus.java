package org.edx.mobile.tta.utils;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public final class RxBus {
    private PublishSubject<Object> mBusSubject;
    private static RxBus sRxbus;

    private RxBus() {
        mBusSubject = PublishSubject.create();
    }

    public static RxBus getInstance() {
        if (sRxbus == null) {
            synchronized (RxBus.class) {
                if (sRxbus == null) {
                    sRxbus = new RxBus();
                }
            }
        }
        return sRxbus;
    }

    public void post(final Object event) {
        mBusSubject.onNext(event);
    }

    public Observable<Object> observable() {
        return mBusSubject;
    }

    /**
     * Observable that only emits events of a specific class.
     * Use this if you only want to subscribe to one type of events.
     */
    public <T> Observable<T> filteredObservable(final Class<T> eventClass) {
        return mBusSubject.ofType(eventClass);
    }
}
