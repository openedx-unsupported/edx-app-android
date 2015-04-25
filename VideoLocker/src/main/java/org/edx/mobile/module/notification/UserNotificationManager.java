package org.edx.mobile.module.notification;

import android.content.Context;
import android.text.TextUtils;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.Config;

import java.util.List;

/**
 * we need to keep track of which course is unsubscribed as Parse
 * only return the courses we currently subscribed.
 *
 * The design concerns - TODO -
 * should we keep the record of the subscriptions? if we fetch it from
 * parse server, what's the frequency of fetching?
 * if we cache it locally, when to do the resync with parse server?
 *
 */
public class UserNotificationManager {
    public static final String COURSE_ANNOUNCEMENT_ACTION = "course.announcement";


    private static final Logger logger = new Logger(UserNotificationManager.class.getName());

    private static UserNotificationManager instance;

    private NotificationDelegate delegate;

    public static synchronized UserNotificationManager getInstance(){
        if ( instance == null )
            instance = new UserNotificationManager();
        return instance;
    }

    public static boolean hasNotificationHash(Context context, String notificationId){
        PrefManager.AppInfoPrefManager pmanager = new PrefManager.AppInfoPrefManager(context);
        String prevHashCode = pmanager.getPrevNotificationHashKey();
        pmanager.setPrevNotificationHashKey(notificationId);
        if ( TextUtils.isEmpty(notificationId) && TextUtils.isEmpty(prevHashCode) )
            return true;
        if ( notificationId != null && notificationId.equals(prevHashCode) )
            return true;
        return false;
    }

    public static boolean appCanHandleFormat(BaseNotificationPayload payload){
        return true;
    }

    private UserNotificationManager(){
        if ( Config.getInstance().isNotificationEnabled() ) {
            Config.ParseNotificationConfig parseNotificationConfig =
                    Config.getInstance().getParseNotificationConfig();
            if (parseNotificationConfig.isEnabled()) {
                delegate = new ParseNotificationDelegate();
            }
        }
        if ( delegate == null ){
            delegate = new DummyNotificationDelegate();
        }
    }

    public void checkAppUpgrade(){
        PrefManager.AppInfoPrefManager pmanager =
                new PrefManager.AppInfoPrefManager(MainApplication.instance());
        boolean needResync = pmanager.isAppUpgradeNeedSyncWithParse();
        if ( needResync ){
            pmanager.setAppUpgradeNeedSyncWithParse(false);
            resubscribeAll();
        }
    }

    /**
     * if a user logs out, they should not receive the push notification
     */
    public void unsubscribeAll( ){
        delegate.unsubscribeAll();
    }

    /**
     * if user login,  he should subscribe channels based on local pref setting.
     */
    public void resubscribeAll(){
        delegate.resubscribeAll();
    }

    //check if local subscribed is not in parse server
    //then try to subscribe it
    public void syncWithServer(){
        delegate.syncWithNotificationServer();
    }

    /**
     * sync with current course enrollment.
     * based on current course enrollment, we subscribe/unsubscribe to the Parse
     *  and update the local preference.
     * @param responses
     */
    public void checkCourseEnrollment(List<EnrolledCoursesResponse> responses){
        delegate.checkCourseEnrollment(responses);
    }

    /**
     *
     * @param courseId  also the channel id
     * @param subscribe subscribe or unsubscribe to courseId channel
     */
    public void changeNotificationSetting(String courseId, String channelId, boolean subscribe){
        delegate.changeNotificationSetting(courseId, channelId, subscribe);
    }

    /**
     *
     * @param channel
     * @param subscribe
     */
    public void subscribeAndUnsubscribeToServer(String channel, boolean subscribe){
        delegate.toggleSubscribeToNotificationServer(channel, subscribe);
    }

    /**
     *
     * @param courseId
     * @return
     */
    public boolean isSubscribedByCourseId(String courseId){
        return delegate.isSubscribedByCourseId( courseId );
    }

    /**
     * NOTE - FOR TESTING purpose only.
     * TODO - we will create a Test Environment Object as
     * the context for running testing code
     * @param delegate
     */
    public void setDelegate(NotificationDelegate delegate){
        this.delegate = delegate;
    }
}
