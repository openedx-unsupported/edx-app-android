package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.ResponseBody;

public abstract class CreateResponseTask extends
Task<DiscussionComment> {

    ResponseBody thread;

    public CreateResponseTask(Context context, ResponseBody thread) {
        super(context);
        this.thread = thread;
    }



    public DiscussionComment call( ) throws Exception{
        try {

            if(thread!=null){

                return environment.getDiscussionAPI().createResponse(thread);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
