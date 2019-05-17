package org.edx.mobile.tta.event;

import org.edx.mobile.tta.data.local.db.table.Content;

public class ContentBookmarkChangedEvent {

    private Content content;
    private boolean bookmarked;

    public ContentBookmarkChangedEvent(Content content, boolean bookmarked) {
        this.content = content;
        this.bookmarked = bookmarked;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }
}
