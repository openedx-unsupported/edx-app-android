package org.edx.mobile.module.db;

import androidx.annotation.Nullable;

public class ObservableDataCallback<T> extends DataCallback<T> {

    @Nullable
    IDbCallback<T> observer;

    public ObservableDataCallback() {
        super(true);
    }

    @Override
    public void onResult(T result) {
        if (null != observer) {
            observer.sendResult(result);
        }
    }

    @Override
    public void onFail(Exception ex) {
        if (null != observer) {
            observer.sendException(ex);
        }
    }

    public void setObserver(@Nullable IDbCallback<T> observer) {
        this.observer = observer;
    }
}
