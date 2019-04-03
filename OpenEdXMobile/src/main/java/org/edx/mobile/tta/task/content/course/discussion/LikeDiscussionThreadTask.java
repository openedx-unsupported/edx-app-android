package org.edx.mobile.tta.task.content.course.discussion;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.remote.api.DiscussionApi;

public class LikeDiscussionThreadTask extends Task<DiscussionThread> {

    private String threadId;
    private boolean liked;

    @Inject
    private DiscussionApi discussionApi;

    public LikeDiscussionThreadTask(Context context, String threadId, boolean liked) {
        super(context);
        this.threadId = threadId;
        this.liked = liked;
    }

    @Override
    public DiscussionThread call() throws Exception {
        return discussionApi.likeThread(threadId, liked).execute().body();
    }
}
