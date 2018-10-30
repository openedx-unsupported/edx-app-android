package org.edx.mobile.event;

import android.support.annotation.NonNull;

public class MediaStatusEvent extends BaseEvent {

    @NonNull
    private final String status;

    public MediaStatusEvent(@NonNull String status) {
        this.status = status;
    }

    @NonNull
    public String getStatus() {
        return status;
    }
}
