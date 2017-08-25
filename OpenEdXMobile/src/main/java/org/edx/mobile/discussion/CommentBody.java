package org.edx.mobile.discussion;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class CommentBody {
    private String threadId;
    private String rawBody;
    private String parentId;

    public CommentBody(@NonNull String threadId, @NonNull String rawBody, @Nullable String parentId) {
        this.threadId = threadId;
        this.rawBody = rawBody;
        this.parentId = parentId;
    }

    public void setParentId(@Nullable String parentId) {
        this.parentId = parentId;
    }

    public void setThreadId(@NonNull String threadId) {
        this.threadId = threadId;
    }

    public void setRawBody(@NonNull String rawBody) {
        this.rawBody = rawBody;
    }

    public String getThreadId() {
        return threadId;
    }

    public String getRawBody() {
        return rawBody;
    }

    public String getParentId() {
        return parentId;
    }
}
