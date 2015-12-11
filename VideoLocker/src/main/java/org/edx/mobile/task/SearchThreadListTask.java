package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.discussion.TopicThreads;

public abstract class SearchThreadListTask extends
        Task<TopicThreads> {

    private static final int PAGE_SIZE = 20;

    final String courseId;
    final String text;
    final int page;

    public SearchThreadListTask(Context context, String courseId, String text, int page) {
        super(context);
        this.courseId = courseId;
        this.text = text;
        this.page = page;
    }

    public TopicThreads call() throws Exception {
        try {
            if (courseId != null) {
                return environment.getDiscussionAPI().searchThreadList(courseId, text, PAGE_SIZE, page);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
