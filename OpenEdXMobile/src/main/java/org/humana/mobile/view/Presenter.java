package org.humana.mobile.view;


import android.support.annotation.NonNull;

public interface Presenter<V> {
    void attachView(@NonNull V view);
    void detachView();
    void destroy();
}
