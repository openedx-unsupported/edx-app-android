package org.edx.mobile.tta.task.content.course.discussion;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.model.Page;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.remote.api.DiscussionApi;

import java.util.List;

public class GetCommentRepliesTask extends Task<Page<DiscussionComment>> {

    private String responseId;
    private int take;
    private int page;
    private List<String> requestedFields;

    @Inject
    private DiscussionApi api;

    public GetCommentRepliesTask(Context context, String responseId, int take, int page, List<String> requestedFields) {
        super(context);
        this.responseId = responseId;
        this.take = take;
        this.page = page;
        this.requestedFields = requestedFields;
    }

    @Override
    public Page<DiscussionComment> call() throws Exception {
        return api.getCommentsList(responseId, take, page, requestedFields).execute().body();
    }
}
