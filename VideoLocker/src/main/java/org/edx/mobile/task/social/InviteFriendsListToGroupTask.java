package org.edx.mobile.task.social;

import android.content.Context;

import org.edx.mobile.module.serverapi.ApiFactory;
import org.edx.mobile.module.serverapi.IApi;
import org.edx.mobile.task.Task;
import org.edx.mobile.util.JavaUtil;


public abstract class InviteFriendsListToGroupTask extends Task<Void> {

    public InviteFriendsListToGroupTask(Context context) {

        super(context);

    }

    @Override
    protected Void doInBackground(Object... params) {

        Long[] friendList = (Long[]) params[0];
        long[] primitiveList = JavaUtil.toPrimitive(friendList);
        Long groupId = (Long) params[1];
        String oauthToken = (String) params[2];

        IApi api = ApiFactory.getCacheApiInstance(context);

        try {

            api.doInviteFriendsToGroup(primitiveList, groupId, oauthToken);
            return null;


        } catch(Exception ex) {
            logger.error(ex);
        }

        return null;
    }
}
