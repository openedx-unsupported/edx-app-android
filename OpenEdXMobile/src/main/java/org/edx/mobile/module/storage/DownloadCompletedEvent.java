package org.edx.mobile.module.storage;

import org.edx.mobile.tta.data.enums.DownloadType;

public class DownloadCompletedEvent {

    private String type;

    public DownloadCompletedEvent(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
