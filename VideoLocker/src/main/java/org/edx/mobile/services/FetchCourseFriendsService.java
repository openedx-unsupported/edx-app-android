package org.edx.mobile.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import org.edx.mobile.http.Api;
import org.edx.mobile.loader.AsyncTaskResult;
import org.edx.mobile.social.SocialMember;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yervant on 1/22/15.
 */
public class FetchCourseFriendsService extends IntentService {

    public static final String TAG = FetchCourseFriendsService.class.getSimpleName();

    public static final String NOTIFY_FILTER = "org.edx.mobile.FETCH_COURSE_FRIENDS";
    public static final String EXTRA_BROADCAST_COURSE_ID = TAG + ".course_identity";

    public static final String TAG_COURSE_ID = TAG + ".courseID";
    public static final String TAG_COURSE_OAUTH = TAG + ".oauthToken";
    public static final String TAG_FORCE_REFRESH = TAG + ".force_refresh";

    private static final Map<String, AsyncTaskResult<List<SocialMember>>> results = new HashMap<>();

    public FetchCourseFriendsService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle args = intent.getExtras();

        String courseID;
        String oauthToken;

        if(args.containsKey(TAG_FORCE_REFRESH)) {
            clearAllData();
            return;
        }

        if(args.containsKey(TAG_COURSE_ID)) {
            courseID = args.getString(TAG_COURSE_ID);
        }
        else return;

        if (args.containsKey(TAG_COURSE_OAUTH)){
            oauthToken = args.getString(TAG_COURSE_OAUTH);
        }
        else return;

        fetchFriends(courseID, oauthToken);
    }

    private void fetchFriends(String courseID, String oauthToken){

        Api api = new Api(this);
        if (TextUtils.isEmpty(courseID) || TextUtils.isEmpty(oauthToken)){
            return;
        }

        AsyncTaskResult<List<SocialMember>> currentResults = fetchResult(courseID);

        if(currentResults != null) {
            notify(courseID);
            return;
        }

        AsyncTaskResult<List<SocialMember>> result = new AsyncTaskResult<>();
        try {

            List<SocialMember> list = api.getFriendsInCourse(false, courseID, oauthToken);

            result.setResult(list);


        } catch (Exception e) {
            result.setEx(e);
        }

        putResult(courseID, result);

        notify(courseID);

        return;
    }

    private synchronized static void putResult(String courseID, AsyncTaskResult<List<SocialMember>> result){
        results.put(courseID, result);
    }

    private void notify(String courseID){
        Intent intent = new Intent(NOTIFY_FILTER);

        intent.putExtra(EXTRA_BROADCAST_COURSE_ID, courseID);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public synchronized static AsyncTaskResult<List<SocialMember>> fetchResult(String courseID){
        return results.get(courseID);
    }

    private synchronized static void clearAllData(){
        results.clear();
    }
}
