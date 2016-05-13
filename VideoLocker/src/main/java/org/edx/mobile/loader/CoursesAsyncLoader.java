package org.edx.mobile.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.google.inject.Inject;

import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.http.RetroHttpException;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import roboguice.RoboGuice;

public class CoursesAsyncLoader extends AsyncTaskLoader<AsyncTaskResult<List<EnrolledCoursesResponse>>> {
    private AsyncTaskResult<List<EnrolledCoursesResponse>> mData;

    private Observer mObserver;

    @Inject
    IEdxEnvironment environment;

    private final Logger logger = new Logger(getClass().getName());

    public CoursesAsyncLoader(Context context) {
        super(context);
        RoboGuice.injectMembers(context, this);
    }

    @Override
    public AsyncTaskResult<List<EnrolledCoursesResponse>> loadInBackground() {
        AsyncTaskResult<List<EnrolledCoursesResponse>> result = new AsyncTaskResult<>();
        try {
            List<EnrolledCoursesResponse> enrolledCoursesResponse = getEnrolledCourses();
            environment.getNotificationDelegate().syncWithServerForFailure();
            environment.getNotificationDelegate().checkCourseEnrollment(enrolledCoursesResponse);
            result.setResult(enrolledCoursesResponse);
        } catch (RetroHttpException exception) {
            result.setEx(exception);
        }
        return result;
    }

    private List<EnrolledCoursesResponse> getEnrolledCourses() {
        List<EnrolledCoursesResponse> ret = null;

        try {
            ret = environment.getServiceManager().getEnrolledCourses(false);
        } catch (Exception e) {
            logger.debug(e.toString());
        }

        return ret;
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
