package org.edx.mobile.task;

import android.content.Context;

import com.qualcomm.qlearn.sdk.discussion.DiscussionComment;
import com.qualcomm.qlearn.sdk.discussion.DiscussionThread;

public abstract class FlagCommentTask extends
Task<DiscussionComment> {

    DiscussionComment comment;
    Boolean flagged;

    public FlagCommentTask(Context context, DiscussionComment comment, Boolean flagged) {
        super(context);
        this.comment = comment;
        this.flagged = flagged;
    }



    public DiscussionComment call( ) throws Exception{
        try {

            if(comment!=null){

                return environment.getDiscussionAPI().flagComment(comment, flagged);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
