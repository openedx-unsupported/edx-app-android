package org.edx.mobile.myCourse;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.task.Task;

import java.util.List;

public class MyCourseTask extends Task<List<EnrolledCoursesResponse>> {
    @Inject
    private LoginAPI loginAPI;

    @NonNull
    private final String username;
    @NonNull
    private final String token;

    public MyCourseTask(Context context, String username,String token) {
        super(context);
        this.username = username;
        this.token = token;
    }

    @Override
    public List<EnrolledCoursesResponse> call() throws Exception {
        return loginAPI.getMyCourses(token,username);
    }
}
