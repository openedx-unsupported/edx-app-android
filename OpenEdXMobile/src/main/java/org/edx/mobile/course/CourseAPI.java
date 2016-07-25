package org.edx.mobile.course;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.util.Config;
import org.edx.mobile.model.Page;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.prefs.UserPrefs;

import retrofit2.Call;

@Singleton
public class CourseAPI {

    @Inject
    protected Config config;

    @NonNull
    private final CourseService courseService;
    @NonNull
    private final UserPrefs userPrefs;


    @Inject
    public CourseAPI(@NonNull CourseService courseService, @NonNull UserPrefs userPrefs) {
        this.courseService = courseService;
        this.userPrefs = userPrefs;
    }

    public
    @NonNull
    Call<Page<CourseDetail>> getCourseList(int page) {
        return courseService.getCourseList(getUsername(), true, config.getOrganizationCode(), page);
    }

    public
    @NonNull
    Call<CourseDetail> getCourseDetail(@NonNull String courseId) {
        // Empty courseId will return a 200 for a list of course details, instead of a single course
        if (TextUtils.isEmpty(courseId)) throw new IllegalArgumentException();
        return courseService.getCourseDetail(courseId, getUsername());
    }

    @Nullable
    private String getUsername() {
        final ProfileModel profile = userPrefs.getProfile();
        return null == profile ? null : profile.username;
    }
}
