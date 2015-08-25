package org.edx.mobile.view.common;

import retrofit.RetrofitError;

/**
 * Created by hanning on 4/30/15.
 */
public interface TaskProcessCallback {
    void startProcess();
    void finishProcess();
    void onMessage(MessageType messageType, String message);
}
