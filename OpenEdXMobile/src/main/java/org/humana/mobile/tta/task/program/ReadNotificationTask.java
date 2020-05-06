package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.data.remote.api.TaAPI;

public class ReadNotificationTask extends Task<SuccessResponse> {

    @Inject
    private TaAPI taAPI;

    private String id;
    private String course_id;

    public ReadNotificationTask(String course_id,Context context, String id) {
        super(context);
        this.id = id;
        this.course_id = course_id;
    }

    @Override
    public SuccessResponse call() throws Exception {
        return taAPI.getReadNotification(id,course_id).execute().body();
    }
}