package org.edx.mobile.tta.data.model.library;

import org.edx.mobile.tta.data.local.db.table.Category;
import org.edx.mobile.tta.data.local.db.table.ContentList;

public class ConfigModifiedDateResponse {

    /**
     * Only contains 'modified_at' field
     */
    private Category category;

    /**
     * Only contains 'modified_at' field
     */
    private ContentList list;

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public ContentList getList() {
        return list;
    }

    public void setList(ContentList list) {
        this.list = list;
    }
}
