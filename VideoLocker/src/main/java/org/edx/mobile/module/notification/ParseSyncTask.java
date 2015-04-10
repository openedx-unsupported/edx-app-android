package org.edx.mobile.module.notification;

import android.content.Context;

import com.parse.ParseInstallation;

import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.task.Task;

import java.util.List;

/**
 * Sync with Parse server
 */
public abstract class ParseSyncTask extends Task<Void> {

    List<String> subscribedChannels;
    public ParseSyncTask(Context context) {
        super(context);
    }

    @Override
    protected Void doInBackground(Object... params) {
        try {
            subscribedChannels = ParseInstallation.getCurrentInstallation().getList("channels");
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex);
        }
        return null;
    }

    public void onException(Exception ex){
          //do nothing.?
    }
}