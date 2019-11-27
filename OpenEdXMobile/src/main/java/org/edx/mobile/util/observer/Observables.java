package org.edx.mobile.util.observer;

import androidx.annotation.NonNull;

public class Observables {
    public static<A, B> Observable<B> map(Observable<A> observable, final Func1<A, B> func) {
        final CachingObservable<B> result = new CachingObservable<>();
        observable.subscribe(new Observer<A>() {
            @Override
            public void onData(@NonNull A data) {
                result.onData(func.call(data));
            }

            @Override
            public void onError(@NonNull Throwable error) {
                result.onError(error);
            }
        });

        return result;
    }
}
