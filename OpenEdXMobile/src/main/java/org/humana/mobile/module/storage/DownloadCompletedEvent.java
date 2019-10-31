package org.humana.mobile.module.storage;

import org.humana.mobile.model.db.DownloadEntry;

public class DownloadCompletedEvent {

    private DownloadEntry entry;

    public DownloadCompletedEvent(DownloadEntry entry) {
        this.entry = entry;
    }

    public DownloadEntry getEntry() {
        return entry;
    }
}
