package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.edx.mobile.event.EnrolledInCourseEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;

public abstract class EnrollForCourseTask extends Task<Void> {

    @NonNull
    private final String courseId;
    private final boolean emailOptIn;
    protected JSONArray courseModes;

    public EnrollForCourseTask(@NonNull Context context, @NonNull String courseId, boolean emailOptIn) {
        super(context);
        if (TextUtils.isEmpty(courseId)) throw new IllegalArgumentException();
        this.courseId = courseId;
        this.emailOptIn = emailOptIn;
    }

    @Override
    public Void call() throws Exception {
        final JSONObject result = environment.getServiceManager().enrollInACourse(courseId, emailOptIn);
        if (result.has("error")) {
            if (result.has("course_details")) {
                this.courseModes = result.getJSONObject("course_details").getJSONArray("course_modes");
            }
            throw new RuntimeException("Enrollment failure, course: " + courseId);
        }
        EventBus.getDefault().post(new EnrolledInCourseEvent());
        return null;
    }
}
