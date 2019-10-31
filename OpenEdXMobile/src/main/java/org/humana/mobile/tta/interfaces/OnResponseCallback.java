package org.humana.mobile.tta.interfaces;

public interface OnResponseCallback<T> {

    void onSuccess(T t);

    void onFailure(Exception e);

}
