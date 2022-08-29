package org.edx.mobile.module.notification

import org.edx.mobile.model.api.EnrolledCoursesResponse

/**
 * Abstracted for different implementation.
 * It may not make much sense for now because different notification middleware may invoke
 * different workflow.
 */
interface NotificationDelegate {
    fun unsubscribeAll()

    /**
     * If learner logins, he should subscribe channels based on local pref setting
     */
    fun resubscribeAll()

    /**
     * Checks if local subscribed is not in the notification server then try to subscribe it
     */
    fun syncWithServerForFailure()

    /**
     * Sync with current course enrollment.
     * Based on current course enrollment, we subscribe/unsubscribe to the notification
     * and update the local preference.
     *
     * @param responses enrolled courses and remote config data
     */
    fun checkCourseEnrollment(responses: List<EnrolledCoursesResponse>)

    /**
     * @param courseId the course id of the underline course
     * @param channelId the notification channel id of this course
     * @param subscribe subscribe or unsubscribe to courseId channel
     */
    fun changeNotificationSetting(courseId: String, channelId: String, subscribe: Boolean)

    /**
     *
     * @param channel the notification channel
     * @return
     */
    fun isSubscribedByCourseId(channel: String): Boolean

    /**
     * App upgrade or new installation, it may need to re-sync with notification server
     */
    fun checkAppUpgrade()
}
