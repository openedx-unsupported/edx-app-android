package org.humana.mobile.tta.task.content.course.discussion;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.discussion.DiscussionComment;
import org.humana.mobile.model.Page;
import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.remote.api.DiscussionApi;

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
