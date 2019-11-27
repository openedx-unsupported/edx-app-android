package org.edx.mobile.loader;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.loader.content.AsyncTaskLoader;

import com.google.inject.Inject;

import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.services.CourseManager;

import roboguice.RoboGuice;

public class CourseOutlineAsyncLoader extends AsyncTaskLoader<AsyncTaskResult<CourseComponent>> {
    private final String blocksApiVersion;
    private final String courseId;
    private AsyncTaskResult<CourseComponent> data;
    @Inject
    private CourseManager courseManager;

    public CourseOutlineAsyncLoader(@NonNull Context context, @NonNull String blocksApiVersion, @NonNull String courseId) {
        super(context);
        RoboGuice.injectMembers(context, this);
        this.blocksApiVersion = blocksApiVersion;
        this.courseId = courseId;
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        if (data != null) {
            // If we currently have a result available, deliver it immediately.
            deliverResult(data);
        }
        if (takeContentChanged() || data == null) {
            // Something has changed or we have no data, so kick off loading it.
            forceLoad();
        }
    }

    /**
     * Called on a worker thread to perform the actual load and to return the result of the
     * load operation.
     */
    @Override
    public AsyncTaskResult<CourseComponent> loadInBackground() {
        final AsyncTaskResult<CourseComponent> result = new AsyncTaskResult<>();
        // Obtain course data from persistable cache
        result.setResult(courseManager.getCourseDataFromPersistableCache(blocksApiVersion, courseId));
        return result;
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped.
        onStopLoading();
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Called when there is new data to deliver to the client.
     */
    @Override
    public void deliverResult(AsyncTaskResult<CourseComponent> data) {
        // We’ll save the data for later retrieval
        this.data = data;
        if (isStarted()) {
            // If the Loader is currently started, we can immediately deliver its results.
            super.deliverResult(data);
        }
    }
}
