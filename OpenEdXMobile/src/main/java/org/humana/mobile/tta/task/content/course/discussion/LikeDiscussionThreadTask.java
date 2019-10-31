package org.humana.mobile.tta.task.content.course.discussion;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.discussion.DiscussionThread;
import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.remote.api.DiscussionApi;

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
