package org.edx.mobile.tta.data.model.library;

import org.edx.mobile.tta.data.local.db.table.Content;

import java.util.List;

public class CollectionItemsResponse {

    private long id;

    private List<Content> content;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Content> getContent() {
        return content;
    }

    public void setContent(List<Content> content) {
        this.content = content;
    }
}
