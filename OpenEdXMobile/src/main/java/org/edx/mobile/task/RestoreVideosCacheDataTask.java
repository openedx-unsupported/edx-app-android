package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.module.prefs.PrefManager;

import java.util.List;

import static org.edx.mobile.http.util.CallUtil.executeStrict;

public class RestoreVideosCacheDataTask extends Task<Void> {
    @Inject
    private CourseAPI courseApi;

    private RestoreVideosCacheDataTask(@NonNull Context context) {
        super(context);
    }

    public static void executeInstanceIfNeeded(@NonNull Context context) {
        PrefManager.UserPrefManager prefs = new PrefManager.UserPrefManager(MainApplication.application);
        if (!prefs.isVideosCacheRestored()) {
            new RestoreVideosCacheDataTask(context).execute();
        }
    }

    @Override
    public Void call() throws Exception {
        List<String> courseIds = environment.getDatabase().getUniqueCourseIdsForDownloadedVideos(null);
        for (String courseId : courseIds) {
            executeStrict(courseApi.getCourseStructure(courseId));
        }
        return null;
    }

    @Override
    protected void onSuccess(Void aVoid) throws Exception {
        PrefManager.UserPrefManager prefManager = new PrefManager.UserPrefManager(context);
        prefManager.setIsVideosCacheRestored(true);
    }
}
