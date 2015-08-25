package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.discussion.CommentBody;
import org.edx.mobile.discussion.DiscussionComment;

public abstract class CreateCommentTask extends
Task<DiscussionComment> {

    CommentBody thread;

    public CreateCommentTask(Context context, CommentBody thread) {
        super(context);
        this.thread = thread;
    }



    public DiscussionComment call( ) throws Exception{
        try {

            if(thread!=null){

                return environment.getDiscussionAPI().createComment(thread);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
