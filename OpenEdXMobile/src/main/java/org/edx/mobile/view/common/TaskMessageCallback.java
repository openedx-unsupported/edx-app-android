package org.edx.mobile.view.common;

import android.support.annotation.NonNull;

public interface TaskMessageCallback {
    void onMessage(@NonNull MessageType messageType, @NonNull String message);
}
