package org.edx.mobile.util.observer;

import androidx.annotation.NonNull;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A basic observable.
 * Keeps a hard reference to all its subscribers.
 * Notifies all subscribed observer of new data or errors.
 * Observers will be notified synchronously on the same thread that calls {@link #sendData(T)} or {@link #sendError(Throwable)}.
 */
public class BasicObservable<T> implements Observable<T> {
    @NonNull
    private final Set<Observer<T>> observers = new LinkedHashSet<>();

    @NonNull
    public Subscription subscribe(@NonNull final Observer<T> observer) {
        observers.add(observer);
        return new Subscription() {
            @Override
            public void unsubscribe() {
                observers.remove(observer);
            }
        };
    }

    public void sendData(@NonNull T data) {
        for (Observer<T> observer : observers) {
            observer.onData(data);
        }
    }

    public void sendError(@NonNull Throwable error) {
        for (Observer<T> observer : observers) {
            observer.onError(error);
        }
    }
}
