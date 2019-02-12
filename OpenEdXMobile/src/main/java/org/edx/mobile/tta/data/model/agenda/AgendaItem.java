package org.edx.mobile.tta.data.model.agenda;

import androidx.annotation.Nullable;

public class AgendaItem {

    private long source_id;

    private long content_count;

    private String source_name;

    private String source_title;

    private String source_icon;

    public long getSource_id() {
        return source_id;
    }

    public void setSource_id(long source_id) {
        this.source_id = source_id;
    }

    public long getContent_count() {
        return content_count;
    }

    public void setContent_count(long content_count) {
        this.content_count = content_count;
    }

    public String getSource_name() {
        return source_name;
    }

    public void setSource_name(String source_name) {
        this.source_name = source_name;
    }

    public String getSource_title() {
        return source_title;
    }

    public void setSource_title(String source_title) {
        this.source_title = source_title;
    }

    public String getSource_icon() {
        return source_icon;
    }

    public void setSource_icon(String source_icon) {
        this.source_icon = source_icon;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return (obj instanceof AgendaItem) && source_name.equalsIgnoreCase(((AgendaItem) obj).source_name);
    }
}
