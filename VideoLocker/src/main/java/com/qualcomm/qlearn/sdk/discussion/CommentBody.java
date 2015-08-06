package com.qualcomm.qlearn.sdk.discussion;

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

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
