package com.qualcomm.qlearn.sdk.discussion;

/**
 * Created by jakelim on 6/16/15.
 */
public interface APICallback<T> {

    void success(T t);

    void failure(Exception e);

}
