package org.edx.mobile.tta.ui.assistant;

import org.edx.mobile.tta.data.local.db.table.Content;

import java.util.List;

public class AssistantModel {
    private String text;
    private boolean isRequest;
    private List<Content> contentList;

    public AssistantModel() {
    }

    public AssistantModel(String text, boolean isRequest) {
        this.text = text;
        this.isRequest = isRequest;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isRequest() {
        return isRequest;
    }

    public void setRequest(boolean request) {
        isRequest = request;
    }

    public List<Content> getContentList() {
        return contentList;
    }

    public void setContentList(List<Content> contentList) {
        this.contentList = contentList;
    }
}
