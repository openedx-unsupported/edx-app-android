package org.edx.mobile.task.social;

import android.content.Context;

import org.edx.mobile.module.facebook.FacebookSessionUtil;
import org.edx.mobile.module.serverapi.ApiFactory;
import org.edx.mobile.module.serverapi.IApi;
import org.edx.mobile.task.Task;

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
        IApi api = ApiFactory.getCacheApiInstance(context);
        try {

            final long groupID = api.doCreateGroup(name, description, privacy, adminID, oauthToken);
            return groupID;

        } catch(Exception ex) {
            logger.error(ex);
        }

        return null;
    }
}
