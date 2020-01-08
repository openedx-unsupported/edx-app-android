package org.humana.mobile.tta.task.notification;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.NotificationResponse;
import org.humana.mobile.tta.data.remote.api.TaAPI;

public class GetNotificationsTask extends Task<NotificationResponse> {

    private int take, skip;
    private String course_id;

    @Inject
    private TaAPI taAPI;

    public GetNotificationsTask(Context context, int take, int skip, String course_id) {
        super(context);
        this.take = take;
        this.skip = skip;
        this.course_id = course_id;
    }

    @Override
    public NotificationResponse call() throws Exception {
        return taAPI.getNotifications(take, skip,course_id).execute().body();
    }
}
