package org.edx.mobile.tta.task.content.course.discussion;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.model.Page;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.remote.api.DiscussionApi;

import java.util.List;

public class GetDiscussionThreadsTask extends Task<Page<DiscussionThread>> {

    private String courseId;
    private List<String> topicIds;
    private String view;
    private String orderBy;
    private int take;
    private int page;
    private List<String> requestedFields;

    @Inject
    private DiscussionApi api;

    public GetDiscussionThreadsTask(Context context, String courseId, List<String> topicIds,
                                    String view, String orderBy, int take, int page,
                                    List<String> requestedFields) {
        super(context);
        this.courseId = courseId;
        this.topicIds = topicIds;
        this.view = view;
        this.orderBy = orderBy;
        this.take = take;
        this.page = page;
        this.requestedFields = requestedFields;
    }

    @Override
    public Page<DiscussionThread> call() throws Exception {
        return api.getThreadList(courseId, topicIds, view, orderBy, take, page, requestedFields)
                .execute().body();
    }
}
