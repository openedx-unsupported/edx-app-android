package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.discussion.DiscussionThread;

public abstract class FlagThreadTask extends
Task<DiscussionThread> {

    DiscussionThread thread;
    Boolean flagged;

    public FlagThreadTask(Context context, DiscussionThread thread, Boolean flagged) {
        super(context);
        this.thread = thread;
        this.flagged = flagged;
    }



    public DiscussionThread call( ) throws Exception{
        try {

            if(thread!=null){

                return environment.getDiscussionAPI().flagThread(thread, flagged);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}
