package org.humana.mobile.tta.task.content.course.discussion;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.discussion.DiscussionComment;
import org.humana.mobile.model.Page;
import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.remote.api.DiscussionApi;

import java.util.List;

public class GetThreadCommentsTask extends Task<Page<DiscussionComment>> {

    private String threadId;
    private int take;
    private int page;
    private List<String> requestedFields;
    private boolean isQuestionType;

    @Inject
    private DiscussionApi api;

    public GetThreadCommentsTask(Context context, String threadId, int take, int page, List<String> requestedFields, boolean isQuestionType) {
        super(context);
        this.threadId = threadId;
        this.take = take;
        this.page = page;
        this.requestedFields = requestedFields;
        this.isQuestionType = isQuestionType;
    }

    @Override
    public Page<DiscussionComment> call() throws Exception {
        return api.getResponsesList(threadId, take, page, requestedFields, isQuestionType).execute().body();
    }
}
