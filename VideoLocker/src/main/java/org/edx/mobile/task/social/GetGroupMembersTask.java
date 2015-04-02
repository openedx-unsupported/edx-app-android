package org.edx.mobile.task.social;

import android.content.Context;

import org.edx.mobile.module.serverapi.ApiFactory;
import org.edx.mobile.module.serverapi.IApi;
import org.edx.mobile.social.SocialMember;
import org.edx.mobile.task.Task;

import java.util.List;

/**
 * Created by marcashman on 2014-12-18.
 */
public abstract class GetGroupMembersTask extends Task<List<SocialMember>> {

    private long groupId;

    public GetGroupMembersTask(Context context, long groupId) {
        super(context);
        this.groupId = groupId;
    }

    @Override
    protected List<SocialMember> doInBackground(Object... objects) {
        IApi api = ApiFactory.getCacheApiInstance(context);
        try {
            return api.getGroupMembers(false, groupId);
        } catch (Exception e) {
            logger.error(e);
            onException(e);
        }
        return null;
    }
}
