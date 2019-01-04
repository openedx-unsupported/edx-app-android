package org.edx.mobile.tta.interfaces;

public interface OnResponseListener<T> {

    void onSuccess(T data);

    void onFailure(Exception e);

}
