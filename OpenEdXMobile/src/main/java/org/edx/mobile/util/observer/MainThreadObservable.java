package org.edx.mobile.util.observer;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import java.util.concurrent.Executor;


/**
 * Wraps an observable such that any observers subscribed to it will receive events on the main thread.
 */
public class MainThreadObservable<T> implements Observable<T> {

    @VisibleForTesting
    @NonNull
    public static Executor EXECUTOR = new Executor() {
        @Override
        public void execute(@NonNull Runnable command) {
            new Handler(Looper.getMainLooper()).post(command);
        }
    };

    @NonNull
    private final Observable<T> observable;

    public MainThreadObservable(@NonNull Observable<T> observable) {
        this.observable = observable;
    }

    @Override
    @NonNull
    public Subscription subscribe(@NonNull final Observer<T> observer) {
        return observable.subscribe(new Observer<T>() {
            @Override
            public void onData(@NonNull final T data) {
                EXECUTOR.execute(new Runnable() {
                    @Override
                    public void run() {
                        observer.onData(data);
                    }
                });
            }

            @Override
            public void onError(@NonNull final Throwable error) {
                EXECUTOR.execute(new Runnable() {
                    @Override
                    public void run() {
                        observer.onError(error);
                    }
                });
            }
        });
    }
}
