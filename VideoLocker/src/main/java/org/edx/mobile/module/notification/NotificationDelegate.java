package org.edx.mobile.module.notification;

import org.edx.mobile.model.api.EnrolledCoursesResponse;
import java.util.List;

/**
 *  abstracted for different implementation.
 *  it may not make much sense for now. because different notification
 *  middleware may invoke different workflow.
 *
 */
public interface NotificationDelegate {
    public void unsubscribeAll( );

    /**
     * if user login,  he should subscribe channels based on local pref setting.
     */
    public void resubscribeAll();

    //check if local subscribed is not in notification server
    //then try to subscribe it
    public void syncWithServerForFailure();

    /**
     * sync with current course enrollment.
     * based on current course enrollment, we subscribe/unsubscribe to the notification
     *  and update the local preference.
     * @param responses
     */
    public void checkCourseEnrollment(List<EnrolledCoursesResponse> responses);

    /**
     *
     * @param courseId  also the channel id
     * @param subscribe subscribe or unsubscribe to courseId channel
     */
    public void changeNotificationSetting(String courseId, String channelId, boolean subscribe);


    /**
     *
     * @param channel
     * @return
     */
    public boolean isSubscribedByCourseId(String channel);

    /**
     * app upgrade or new installation, it may need to resync with notification server
     */
    public void checkAppUpgrade();
}
