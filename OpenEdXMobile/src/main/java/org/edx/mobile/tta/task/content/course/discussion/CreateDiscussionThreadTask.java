package org.edx.mobile.tta.task.content.course.discussion;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.remote.api.DiscussionApi;

public class CreateDiscussionThreadTask extends Task<DiscussionThread> {

    private String courseId;
    private String title;
    private String body;
    private String topicId;
    private DiscussionThread.ThreadType type;

    @Inject
    private DiscussionApi discussionApi;

    public CreateDiscussionThreadTask(Context context, String courseId, String title, String body, String topicId, DiscussionThread.ThreadType type) {
        super(context);
        this.courseId = courseId;
        this.title = title;
        this.body = body;
        this.topicId = topicId;
        this.type = type;
    }

    @Override
    public DiscussionThread call() throws Exception {
        return discussionApi.createThread(courseId, title, body, topicId, type).execute().body();
    }
}
