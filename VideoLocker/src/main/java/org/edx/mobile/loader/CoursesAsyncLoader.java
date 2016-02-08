package org.edx.mobile.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.services.ServiceManager;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class CoursesAsyncLoader extends AsyncTaskLoader<AsyncTaskResult<List<EnrolledCoursesResponse>>> {

    private static final String TAG = CoursesAsyncLoader.class.getCanonicalName();
    public static final String TAG_COURSE_OAUTH = TAG + ".oauthToken";

    private String oauthToken;
    private AsyncTaskResult<List<EnrolledCoursesResponse>> mData;

    private Observer mObserver;

    IEdxEnvironment environment;
    ServiceManager api;

    public CoursesAsyncLoader(Context context, Bundle args, IEdxEnvironment environment,  ServiceManager api){
        super(context);
        this.environment = environment;
        this.api = api;
        if (args.containsKey(TAG_COURSE_OAUTH)){
            this.oauthToken = args.getString(TAG_COURSE_OAUTH);
        }

    }

    @Override
    public AsyncTaskResult<List<EnrolledCoursesResponse>> loadInBackground() {

        // FIXME: (PR#120) Should this Loader class really be called when social feature is disabled?
        if (environment.getConfig().getSocialSharingConfig().isEnabled() && this.oauthToken == null) {
            return null;
        }

        AsyncTaskResult<List<EnrolledCoursesResponse>> result = new AsyncTaskResult<List<EnrolledCoursesResponse>>();
        try {
            List<EnrolledCoursesResponse> response = api.getEnrolledCourses();
            environment.getNotificationDelegate().syncWithServerForFailure();
            environment.getNotificationDelegate().checkCourseEnrollment(response);
            result.setResult(response);

        } catch (Exception e) {
            result.setEx(e);
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
