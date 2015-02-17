package org.edx.mobile.task.social;

import android.content.Context;

import org.edx.mobile.http.Api;
import org.edx.mobile.task.Task;
import org.edx.mobile.module.facebook.FacebookSessionUtil;

public abstract class CreateGroupTask extends Task<Long> {

    public CreateGroupTask(Context context) {

        super(context);

    }

    @Override
    protected Long doInBackground(Object... params) {

        String name = (String) params[0];
        String description = (String) params[1];
        long adminID = Long.parseLong((String)params[2]);
        Boolean privacy = (Boolean) params[3];
        String oauthToken = FacebookSessionUtil.getAccessToken();
        //
        Api api = new Api(context);
        try {

            final long groupID = api.createGroup(name, description, privacy, adminID, oauthToken);
            return groupID;

        } catch(Exception ex) {
            logger.error(ex);
        }

        return null;
    }
}
