package org.edx.mobile.task.social;

import android.content.Context;

import org.edx.mobile.module.facebook.FacebookSessionUtil;
import org.edx.mobile.services.ServiceManager;
import org.edx.mobile.task.Task;

public abstract class CreateGroupTask extends Task<Long> {

    String name;
    String description;
    long adminID;
    Boolean privacy;

    public CreateGroupTask(Context context, String name, String description, String adminID, Boolean privacy) {
        super(context);
        this.name = name;
        this.description = description;
        this.adminID = Long.parseLong(adminID);
        this.privacy = privacy;
    }

    @Override
    public Long call( ) {

        String oauthToken = FacebookSessionUtil.getAccessToken();
        //
        ServiceManager api = environment.getServiceManager();
        try {

            final long groupID = api.createGroup(name, description, privacy, adminID, oauthToken);
            return groupID;

        } catch(Exception ex) {
            logger.error(ex);
        }

        return null;
    }
}
