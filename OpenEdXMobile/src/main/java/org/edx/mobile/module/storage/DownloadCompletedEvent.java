package org.edx.mobile.module.storage;

import org.edx.mobile.model.db.DownloadEntry;

public class DownloadCompletedEvent {

    private DownloadEntry entry;

    public DownloadCompletedEvent(DownloadEntry entry) {
        this.entry = entry;
    }

    public DownloadEntry getEntry() {
        return entry;
    }
}
