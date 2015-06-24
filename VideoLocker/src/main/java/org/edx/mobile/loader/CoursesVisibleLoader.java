package org.edx.mobile.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.logger.Logger;

/**
 * Created by yervant on 1/19/15.
 */
public class CoursesVisibleLoader extends AsyncTaskLoader<AsyncTaskResult<Boolean>>{

    AsyncTaskResult<Boolean> mData;
    private Boolean setToValue = null;
    private boolean fetchValue = false;

    public static final String KEY_SET_TO_VALUE = "key_set_to_value";
    public static final String KEY_GET_VALUE = "key_get_value";
    private static final Logger logger = new Logger(CoursesVisibleLoader.class);
    IEdxEnvironment environment;

    public CoursesVisibleLoader(Context context, Bundle bundle, IEdxEnvironment environment){
        super(context);
        this.environment = environment;
        if(bundle != null){
            if(bundle.containsKey(KEY_SET_TO_VALUE))
                setToValue = bundle.getBoolean(KEY_SET_TO_VALUE);
            else if(bundle.containsKey(KEY_GET_VALUE))
                fetchValue = true;
        }
    }

    @Override
    public AsyncTaskResult<Boolean> loadInBackground() {
        AsyncTaskResult<Boolean> result = new AsyncTaskResult<>();

        try {
            if(fetchValue && setToValue == null){
                Boolean isSet = environment.getServiceManager().getUserCourseShareConsent();
                result.setResult(isSet);
            }
            else if(setToValue != null){
                Boolean wasSet = environment.getServiceManager().setUserCourseShareConsent(setToValue);
                result.setResult(wasSet);
            }
            else{
                result.setEx(new IllegalArgumentException("Course sharing: either KEY_GET_VALUE or KEY_SET_TO_VALUE must be set."));
            }
        } catch (Exception e) {
            logger.error(e);
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

        if (takeContentChanged() || mData == null) {
            forceLoad();
        }

    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(AsyncTaskResult<Boolean> data) {
        super.onCanceled(data);
        releaseResources(data);
    }

    @Override
    public void deliverResult(AsyncTaskResult<Boolean> data) {

        if (isReset()) {
            releaseResources(data);
            return;
        }

        AsyncTaskResult<Boolean> oldData = mData;
        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldData != null && oldData != data) {
            releaseResources(oldData);
        }
    }

    private void releaseResources(AsyncTaskResult<Boolean> data) {



    }
}
