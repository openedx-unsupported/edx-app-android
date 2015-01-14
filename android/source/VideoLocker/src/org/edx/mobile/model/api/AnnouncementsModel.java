package org.edx.mobile.model.api;

import java.io.Serializable;

@SuppressWarnings("serial")
public class AnnouncementsModel implements Serializable {

    public String date;
    public String content;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
