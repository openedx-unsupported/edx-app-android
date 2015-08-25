package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.discussion.DiscussionComment;

public abstract class VoteCommentTask extends
Task<DiscussionComment> {

    DiscussionComment comment;
    Boolean voted;

    public VoteCommentTask(Context context, DiscussionComment comment, Boolean voted) {
        super(context);
        this.comment = comment;
        this.voted = voted;
    }



    public DiscussionComment call( ) throws Exception{
        try {

            if(comment!=null){

                return environment.getDiscussionAPI().voteComment(comment, voted);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
