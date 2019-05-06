package org.edx.mobile.tta.event;

import org.edx.mobile.tta.data.local.db.table.ContentStatus;

public class ContentStatusReceivedEvent {

    private ContentStatus contentStatus;

    public ContentStatusReceivedEvent(ContentStatus contentStatus) {
        this.contentStatus = contentStatus;
    }

    public ContentStatus getContentStatus() {
        return contentStatus;
    }

    public void setContentStatus(ContentStatus contentStatus) {
        this.contentStatus = contentStatus;
    }
}
