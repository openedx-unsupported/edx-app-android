package org.edx.mobile.discussion;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CommentBody {
    private String threadId;
    private String rawBody;
    private String parentId;

    public CommentBody(@NonNull String threadId, @NonNull String rawBody, @Nullable String parentId) {
        this.threadId = threadId;
        this.rawBody = rawBody;
        this.parentId = parentId;
    }
}
