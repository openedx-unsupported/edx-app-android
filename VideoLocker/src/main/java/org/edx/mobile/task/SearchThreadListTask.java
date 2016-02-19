package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.model.Page;

public abstract class SearchThreadListTask extends
        Task<Page<DiscussionThread>> {

    final String courseId;
    final String text;
    final int page;

    public SearchThreadListTask(Context context, String courseId, String text, int page) {
        super(context);
        this.courseId = courseId;
        this.text = text;
        this.page = page;
    }

    public Page<DiscussionThread> call() throws Exception {
        try {
            if (courseId != null) {
                return environment.getDiscussionAPI().searchThreadList(courseId, text, page);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
