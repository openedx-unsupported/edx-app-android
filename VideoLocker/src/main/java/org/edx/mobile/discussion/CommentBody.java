package org.edx.mobile.discussion;

public class CommentBody {
    String threadId;
    String rawBody;
    String parentId;

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public void setRawBody(String rawBody) {
        this.rawBody = rawBody;
    }

    public String getThreadId() {
        return threadId;
    }

    public String getRawBody() {
        return rawBody;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getParentId() {
        return parentId;
    }
}
