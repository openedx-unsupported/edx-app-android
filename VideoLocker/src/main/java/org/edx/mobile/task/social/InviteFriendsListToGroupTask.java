package org.edx.mobile.task.social;

import android.content.Context;

import org.edx.mobile.http.Api;
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

        Api api = new Api(context);

        try {

            api.inviteFriendsToGroup(primitiveList, groupId, oauthToken);
            return null;


        } catch(Exception ex) {
            logger.error(ex);
        }

        return null;
    }
}
