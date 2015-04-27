package org.edx.mobile.module.notification;

import android.text.TextUtils;

import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.prefs.UserBasedPrefManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanning on 4/9/15.
 */
public class ParseNotificationDelegate implements NotificationDelegate{
    private static final Logger logger = new Logger(ParseNotificationDelegate.class.getName());

    ParseNotificationDelegate() {
    }

    /**
     * if a user logs out, they should not receive the push notification
     */
    public void unsubscribeAll() {
        new ParseSyncTask(MainApplication.instance().getApplicationContext()) {
            @Override
            public void onFinish(Void result) {
                if (subscribedChannels != null) {
                    for (String channel : subscribedChannels) {
                        subscribeAndUnsubscribeToServer(channel, false);
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
                subscribeAndUnsubscribeToServer(pc.getChannelId(), true);
        }
    }

    //check if local subscribed is not in parse server
    //then try to subscribe it
    public void syncWithNotificationServer() {
        new ParseSyncTask(MainApplication.instance().getApplicationContext()) {
            @Override
            public void onFinish(Void result) {
                UserBasedPrefManager prefManager = UserBasedPrefManager.getInstance(UserBasedPrefManager.UserPrefType.NOTIFICATION);
                NotificationPreference preference = prefManager.getNotificationPreference();
                for (EdxLocalParseChannel pc : preference) {
                    if (pc.isSubscribed() && subscribedChannels != null &&
                            !subscribedChannels.contains(pc.getChannelId()))
                        subscribeAndUnsubscribeToServer(pc.getChannelId(), true);
                    if (!pc.isSubscribed() && subscribedChannels != null &&
                            subscribedChannels.contains(pc.getChannelId()))
                        subscribeAndUnsubscribeToServer(pc.getChannelId(), false);
                }
            }
        }.execute();

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
            changeSubscriptionToNotificationServer(pc.getChannelId(), true, true);
            preference.add(pc);
        }

        List<EdxLocalParseChannel> inactiveCourseList = preference.filterForInactiveCourses(activeList);
        for (EdxLocalParseChannel pc : inactiveCourseList) {
            if (pc.isSubscribed()) {
                changeSubscriptionToNotificationServer(pc.getChannelId(), false, true);
                pc.setSubscribed(false);
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
        changeSubscriptionToNotificationServer(pc.getChannelId(), subscribe, true);
    }

    /**
     * try to subscribe and unsubscribe to the parser server.
     * it is used when local cached data wont change regardless of the result of
     * the operation. 
     * @param channel
     * @param subscribe
     */
    public void subscribeAndUnsubscribeToServer(String channel, boolean subscribe) {
        //false means if failed, we won't change the locale cache.
        changeSubscriptionToNotificationServer(channel, subscribe, false);
    }

    /**
     * if subscription fails, we may need to update local cache.
     * @param channel
     * @param subscribe
     * @param updateCacheOnError if operation fails, we should update the local cache
     */
    private void changeSubscriptionToNotificationServer(final String channel, boolean subscribe, final boolean updateCacheOnError) {
        if (subscribe) {
            ParsePush.subscribeInBackground(channel, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        logger.debug("successfully subscribed to the broadcast channel.");
                    } else {
                        logger.error(e);
                        if ( updateCacheOnError ){
                            deleteLocalRecordForSubscription(channel);
                        }
                    }
                }
            });
        } else {
            ParsePush.unsubscribeInBackground(channel, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        logger.debug("successfully UNsubscribed to the broadcast channel.");
                    } else {
                        logger.error(e);
                        if (updateCacheOnError) {
                            deleteLocalRecordForSubscription(channel);
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
    private void deleteLocalRecordForSubscription(String channelId){
        UserBasedPrefManager prefManager = UserBasedPrefManager.getInstance(UserBasedPrefManager.UserPrefType.NOTIFICATION);
        NotificationPreference preference = prefManager.getNotificationPreference();
        if ( preference.removeByChannelId(channelId) )
            prefManager.saveNotificationPreference(preference);
    }


}

