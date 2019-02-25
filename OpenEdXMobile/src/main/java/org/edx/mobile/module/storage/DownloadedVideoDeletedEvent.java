package org.edx.mobile.module.storage;

public class DownloadedVideoDeletedEvent {

    private String type;

    public DownloadedVideoDeletedEvent(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
