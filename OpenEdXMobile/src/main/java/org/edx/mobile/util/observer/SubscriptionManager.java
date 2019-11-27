package org.edx.mobile.util.observer;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Keeps track of a set of subscriptions so that you can easily unsubscribe from them all at once.
 *
 * For example, when an Activity or Presenter is destroyed, you'll want to unsubscribe from everything it had subscribed to.
 */
public class SubscriptionManager {

    @NonNull
    private Set<Subscription> viewSubscriptions = new HashSet<>();

    public void unsubscribeAll() {
        for (Subscription subscription : viewSubscriptions) {
            subscription.unsubscribe();
        }
        viewSubscriptions.clear();
    }

    public void add(@NonNull Subscription subscription) {
        viewSubscriptions.add(subscription);
    }

    /**
     * Wraps an observable such that anything subscribed to is automatically added to this SubscriptionManager
     */
    @NonNull
    public <T> Observable<T> wrap(@NonNull Observable<T> observable) {
        return new SubscriptionManager.ManagedObservable<>(observable, this);
    }

    private static class ManagedObservable<T> implements Observable<T> {
        @NonNull
        private final Observable<T> observable;

        @NonNull
        private final SubscriptionManager subscriptionManager;

        public ManagedObservable(@NonNull Observable<T> observable, @NonNull SubscriptionManager subscriptionManager) {
            this.observable = observable;
            this.subscriptionManager = subscriptionManager;
        }


        @NonNull
        @Override
        public Subscription subscribe(@NonNull Observer<T> observer) {
            final Subscription subscription = observable.subscribe(observer);
            subscriptionManager.add(subscription);
            return subscription;
        }
    }
}
