package org.edx.mobile.course;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.discovery.model.EnrollAndUnenrollData;
import org.edx.mobile.discovery.model.EnrollResponse;
import org.edx.mobile.task.Task;

public abstract class EnrollInCourseTask extends Task<EnrollResponse> {

    @Inject
    private LoginAPI loginAPI;
    @NonNull
    private final EnrollAndUnenrollData requestBody;

    public EnrollInCourseTask(EnrollAndUnenrollData body, Context activity) {
        super(activity);
        this.requestBody = body;
    }

    @Override
    @NonNull
    public EnrollResponse call() throws Exception {
        return loginAPI.getAccessEnroll(requestBody);
    }
}