package org.edx.mobile.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;

import org.edx.mobile.http.Api;
import org.edx.mobile.social.SocialMember;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class FriendsInCourseLoader extends AsyncTaskLoader<AsyncTaskResult<List<SocialMember>>> {

    private static final String TAG = FriendsInCourseLoader.class.getCanonicalName();
    public static final String TAG_COURSE_ID = TAG + ".courseID";
    public static final String TAG_COURSE_OAUTH = TAG + ".oauthToken";

    private String oauthToken;
    private String courseID;
    private AsyncTaskResult<List<SocialMember>> mData;

    private Observer mObserver;

    public FriendsInCourseLoader(Context context, Bundle args){

        super(context);
        if(args.containsKey(TAG_COURSE_ID)){
           this.courseID = args.getString(TAG_COURSE_ID);
        }
        if (args.containsKey(TAG_COURSE_OAUTH)){
            this.oauthToken = args.getString(TAG_COURSE_OAUTH);
        }

    }

    @Override
    public AsyncTaskResult<List<SocialMember>> loadInBackground() {

        Api api = new Api(getContext());
        if (TextUtils.isEmpty(courseID) || TextUtils.isEmpty(this.oauthToken)){
            return null;
        }

        AsyncTaskResult<List<SocialMember>> result = new AsyncTaskResult<List<SocialMember>>();
        try {

            List<SocialMember> list = api.getFriendsInCourse(false, this.courseID, this.oauthToken);

            result.setResult(list);

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
    public void onCanceled(AsyncTaskResult<List<SocialMember>> data) {
        super.onCanceled(data);
        releaseResources(data);
    }

    @Override
    public void deliverResult(AsyncTaskResult<List<SocialMember>> data) {

        if (isReset()) {
            releaseResources(data);
            return;
        }

        AsyncTaskResult<List<SocialMember>> oldData = mData;
        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldData != null && oldData != data) {
            releaseResources(oldData);
        }
    }

    private void releaseResources(AsyncTaskResult<List<SocialMember>> data) {



    }

}
