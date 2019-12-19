package org.humana.mobile.tta.task.notification;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.local.db.table.Notification;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class SendNotificationTask extends Task<SuccessResponse> {

    private String title,type, desc, action, action_parent_id, action_id, respondent;

    @Inject
    private TaAPI taAPI;

    public SendNotificationTask(Context context, String title, String type, String desc, String action,
                                String action_id, String action_parent_id, String respondent) {
        super(context);

        this.title = title;
        this.desc = desc;
        this.type = type;
        this.action = action;
        this.action_id = action_id;
        this.action_parent_id = action_parent_id;
        this.respondent = respondent;
    }

    @Override
    public SuccessResponse call() throws Exception {
        return taAPI.sendNotification(title, type, desc, action, action_parent_id, action_id, respondent).execute().body();
    }
}