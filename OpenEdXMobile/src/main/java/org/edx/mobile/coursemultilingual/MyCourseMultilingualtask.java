package org.edx.mobile.coursemultilingual;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.task.Task;

import java.util.List;

public class MyCourseMultilingualtask extends Task<List<CourseMultilingualModel>> {
    @Inject
    private LoginAPI loginAPI;

    @NonNull
    private final String course_key;


    public MyCourseMultilingualtask(Context context, String course_key) {
        super(context);
        this.course_key = course_key;
    }

    @Override
    public List<CourseMultilingualModel> call() throws Exception {
        return loginAPI.getMyCoursesMultilingualTranslation(course_key);
    }
}
