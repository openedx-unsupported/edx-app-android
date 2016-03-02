package org.edx.mobile.util.observer;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

/**
 * This class is only temporary, for use as long as our HTTP calls use synchronous methods.
 * Once we transition our HTTP calls to be asynchronous themselves, we won't need this class.
 */
public enum AsyncCallableUtils {
    ;

    // Using AsyncTask.THREAD_POOL_EXECUTOR to automatically support Espresso idling resources
    @VisibleForTesting
    @NonNull
    public static Executor EXECUTOR = AsyncTask.THREAD_POOL_EXECUTOR;

    /**
     * Executes a callable on a background thread, and
     * delivers the result or exception to an observer from the background thread.
     */
    public static <T> void observe(@NonNull final Callable<T> callable, @NonNull final Observer<T> observer) {
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                final T data;
                try {
                    data = callable.call();
                } catch (Throwable e) {
                    observer.onError(e);
                    return;
                }
                observer.onData(data);
            }
        });
    }
}
