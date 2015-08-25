package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.discussion.ThreadComments;

import org.edx.mobile.view.adapters.IPagination;

public abstract class GetCommentListTask extends
Task<ThreadComments> {

    String threadId;
    IPagination pagination;

    public GetCommentListTask(Context context, String threadId, IPagination pagination) {
        super(context);
        this.threadId = threadId;
        this.pagination = pagination;
    }



    public ThreadComments call( ) throws Exception{
        try {

            if(threadId!=null){
                int pageSize = pagination.pageSize();
                int page = pagination.numOfPagesLoaded() + 1;
                return environment.getDiscussionAPI().getCommentList(threadId, pageSize, page);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
