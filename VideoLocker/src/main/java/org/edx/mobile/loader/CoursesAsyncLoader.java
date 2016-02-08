package org.edx.mobile.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.http.RetroHttpException;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.user.UserAPI;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class CoursesAsyncLoader extends AsyncTaskLoader<AsyncTaskResult<List<EnrolledCoursesResponse>>> {

    private static final String TAG = CoursesAsyncLoader.class.getCanonicalName();
    public static final String TAG_COURSE_OAUTH = TAG + ".oauthToken";

    private String oauthToken;
    private AsyncTaskResult<List<EnrolledCoursesResponse>> mData;
    private Context context;

    private Observer mObserver;

    IEdxEnvironment environment;
    UserAPI api;

    public CoursesAsyncLoader(Context context, Bundle args, IEdxEnvironment environment, UserAPI api) {
        super(context);
        this.context = context;
        this.environment = environment;
        this.api = api;
        if (args.containsKey(TAG_COURSE_OAUTH)) {
            this.oauthToken = args.getString(TAG_COURSE_OAUTH);
        }

    }

    @Override
    public AsyncTaskResult<List<EnrolledCoursesResponse>> loadInBackground() {

        PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
        ProfileModel profile = pref.getCurrentUserProfile();

        AsyncTaskResult<List<EnrolledCoursesResponse>> result = new AsyncTaskResult<>();
        try {
            List<EnrolledCoursesResponse> enrolledCoursesResponse = api.getUserEnrolledCourses(profile.username);
            environment.getNotificationDelegate().syncWithServerForFailure();
            environment.getNotificationDelegate().checkCourseEnrollment(enrolledCoursesResponse);
            result.setResult(enrolledCoursesResponse);
        } catch (RetroHttpException exception) {
            result.setEx(exception);
        }
        return result;
    }

    @Override
    protected void onReset() {
        onStopLoading();

        if (mData != null) {
            releaseResources(mData);
            mData = null;
        }

    }

    @Override
    protected void onStartLoading() {

        if (mData != null) {
            deliverResult(mData);
        }

        if (mObserver == null) {
            mObserver = new Observer() {
                @Override
                public void update(Observable observable, Object o) {

                }
            };
        }

        if (takeContentChanged() || mData == null) {
            forceLoad();
        }

    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(AsyncTaskResult<List<EnrolledCoursesResponse>> data) {
        super.onCanceled(data);
        releaseResources(data);
    }

    @Override
    public void deliverResult(AsyncTaskResult<List<EnrolledCoursesResponse>> data) {

        if (isReset()) {
            releaseResources(data);
            return;
        }

        AsyncTaskResult<List<EnrolledCoursesResponse>> oldData = mData;
        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldData != null && oldData != data) {
            releaseResources(oldData);
        }
    }

    private void releaseResources(AsyncTaskResult<List<EnrolledCoursesResponse>> data) {

    }

}