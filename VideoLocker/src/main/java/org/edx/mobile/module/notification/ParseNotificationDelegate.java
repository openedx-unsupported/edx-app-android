package org.edx.mobile.module.notification;

import android.text.TextUtils;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.module.prefs.UserBasedPrefManager;
import org.edx.mobile.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanning on 4/9/15.
 */
public class ParseNotificationDelegate implements NotificationDelegate{
    private static final Logger logger = new Logger(ParseNotificationDelegate.class.getName());

    /**
     * if a user logs out, they should not receive the push notification
     */
    public void unsubscribeAll() {
        new ParseSyncTask(MainApplication.instance().getApplicationContext()) {
            @Override
            public void onSuccess(Void result) {
                if (subscribedChannels != null) {
                    for (String channel : subscribedChannels) {
                        changeSubscriptionToNotificationServer(channel, false, false);
                    }
                }
            }
        }.execute();
    }

    /**
     * if user login,  he should subscribe channels based on local pref setting.
     */
    public void resubscribeAll() {
        UserBasedPrefManager prefManager = UserBasedPrefManager.getInstance(UserBasedPrefManager.UserPrefType.NOTIFICATION);
        NotificationPreference preference = prefManager.getNotificationPreference();
        for (EdxLocalParseChannel pc : preference) {
            if (pc.isSubscribed())
                changeSubscriptionToNotificationServer(pc, true);
        }
    }

    //check if local subscribed is not in parse server
    //then try to subscribe it
    public void syncWithServerForFailure() {
        if (!NetworkUtil.isConnected(MainApplication.instance()))
            return;
        List<String> subscribedChannels = ParseInstallation.getCurrentInstallation().getList("channels");

        UserBasedPrefManager prefManager = UserBasedPrefManager.getInstance(UserBasedPrefManager.UserPrefType.NOTIFICATION);
        NotificationPreference preference = prefManager.getNotificationPreference();
        List<EdxLocalParseChannel> failedOps = preference.getAllFailedUpdate();
        if ( failedOps.size() > 0 ){
            for (EdxLocalParseChannel pc : failedOps) {
                changeSubscriptionToNotificationServer(pc, pc.isSubscribed());
            }
            prefManager.saveNotificationPreference(preference);
        }
    }

    /**
     * sync with current course enrollment.
     * based on current course enrollment, we subscribe/unsubscribe to the Parse
     * and update the local preference.
     *
     * @param responses
     */
    public void checkCourseEnrollment(List<EnrolledCoursesResponse> responses) {
        List<CourseEntry> activeList = new ArrayList<>();
        for (EnrolledCoursesResponse response : responses) {
            if (response.isCourse() && response.isIs_active()) {
                activeList.add(response.getCourse());
            }
        }
        if (activeList.isEmpty())
            return;

        UserBasedPrefManager prefManager = UserBasedPrefManager.getInstance(UserBasedPrefManager.UserPrefType.NOTIFICATION);
        NotificationPreference preference = prefManager.getNotificationPreference();
        List<CourseEntry> newCourseList = preference.filterForNewCourses(activeList);
        for (CourseEntry courseEntry : newCourseList) {
            String subscriptionId = courseEntry.getSubscription_id();
            if (TextUtils.isEmpty( subscriptionId ) )
                continue;
            EdxLocalParseChannel pc = new EdxLocalParseChannel(courseEntry.getId(), subscriptionId, true);
            changeSubscriptionToNotificationServer(pc, true);
            preference.add(pc);
        }

        List<EdxLocalParseChannel> inactiveCourseList = preference.filterForInactiveCourses(activeList);
        for (EdxLocalParseChannel pc : inactiveCourseList) {
            if (pc.isSubscribed()) {
                changeSubscriptionToNotificationServer(pc, false);
            }
        }

        if (newCourseList.size() > 0 || inactiveCourseList.size() > 0)
            prefManager.saveNotificationPreference(preference);

    }

    @Override
    public boolean isSubscribedByCourseId(String courseId){
        UserBasedPrefManager prefManager = UserBasedPrefManager.getInstance(UserBasedPrefManager.UserPrefType.NOTIFICATION);
        NotificationPreference preference = prefManager.getNotificationPreference();
        EdxLocalParseChannel pc = preference.getByCourseId(courseId);
        return pc == null ? false : pc.isSubscribed();
    }

    /**
     * @param courseId  also the channel id
     * @param subscribe subscribe or unsubscribe to courseId channel
     */
    public void changeNotificationSetting(String courseId, String channelId, boolean subscribe) {
        UserBasedPrefManager prefManager = UserBasedPrefManager.getInstance(UserBasedPrefManager.UserPrefType.NOTIFICATION);
        NotificationPreference preference = prefManager.getNotificationPreference();
        EdxLocalParseChannel pc = preference.getByCourseId(courseId);
        if (pc == null) {
            pc = new EdxLocalParseChannel(courseId, channelId, subscribe);
            preference.add(pc);
        } else {
            pc.setSubscribed(subscribe);
        }
        prefManager.saveNotificationPreference(preference);
        changeSubscriptionToNotificationServer(pc, subscribe);
    }

    /**
     * this api handle the failure case for sync with parse server
     * @param channel
     * @param subscribe
     */
    private void changeSubscriptionToNotificationServer(EdxLocalParseChannel channel, boolean subscribe) {
         channel.setOperationFailed(false);
        changeSubscriptionToNotificationServer(channel.getChannelId(), subscribe, true);
    }
    /**
     * if subscription fails, we may need to update local cache.
     * @param channelId
     * @param subscribe
     * @param updateCacheOnError if operation fails, we should update the local cache
     */
    private void changeSubscriptionToNotificationServer(final String channelId, boolean subscribe, final boolean updateCacheOnError) {

        if (subscribe) {

            ParsePush.subscribeInBackground(channelId, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        logger.debug("successfully subscribed to the broadcast channel.");
                    } else {
                        logger.error(e);
                        if ( updateCacheOnError ){
                            markFailureForSubscriptionOp(channelId);
                        }
                    }
                }
            });

        } else {
            ParsePush.unsubscribeInBackground(channelId, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        logger.debug("successfully UNsubscribed to the broadcast channel.");
                    } else {
                        logger.error(e);
                        if (updateCacheOnError) {
                            markFailureForSubscriptionOp(channelId);
                        }
                    }
                }
            });
        }
    }

    /**
     * The simplest way to handle the error from synchronization between local cache and remote server -
     * is to clear the record in the cache.
     * @param channelId
     */
    private void markFailureForSubscriptionOp(String channelId){
        UserBasedPrefManager prefManager = UserBasedPrefManager.getInstance(UserBasedPrefManager.UserPrefType.NOTIFICATION);
        NotificationPreference preference = prefManager.getNotificationPreference();
        EdxLocalParseChannel channel = preference.getByChannelId(channelId);
        if ( channel != null ){
            channel.setOperationFailed(true);
            prefManager.saveNotificationPreference(preference);
        }
    }


    public void checkAppUpgrade(){
        if (!NetworkUtil.isConnected(MainApplication.instance()))
            return;
        PrefManager.AppInfoPrefManager pmanager =
            new PrefManager.AppInfoPrefManager(MainApplication.instance());
        if ( pmanager.isAppUpgradeNeedSyncWithParse() ){
            pmanager.setAppUpgradeNeedSyncWithParse(false);
            resubscribeAll();
        }
        if ( pmanager.isAppSettingNeedSyncWithParse() ){
            pmanager.setAppSettingNeedSyncWithParse(false);
            ParseHandleHelper.tryToSaveLanguageSetting();
        }
    }
}

