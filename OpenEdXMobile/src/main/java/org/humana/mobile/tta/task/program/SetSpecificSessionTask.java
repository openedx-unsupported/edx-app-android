package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.data.remote.api.TaAPI;

public class SetSpecificSessionTask extends Task<SuccessResponse> {

    private String role, username, url,mInstructor_cookie;

    @Inject
    private TaAPI taAPI;

    public SetSpecificSessionTask(Context context, String role, String username, String url,String instructor_cookie) {
        super(context);
        this.username = username;
        this.role = role;
        this.url = url;
        this.mInstructor_cookie=instructor_cookie;

    }

    @Override
    public SuccessResponse call() throws Exception {
        return taAPI.setSpecificSession(role, username, url,mInstructor_cookie).execute().body();
    }
}