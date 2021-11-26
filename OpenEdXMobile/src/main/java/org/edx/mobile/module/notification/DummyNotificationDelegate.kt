package org.edx.mobile.module.notification

import org.edx.mobile.model.api.EnrolledCoursesResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DummyNotificationDelegate @Inject constructor() : NotificationDelegate {
    override fun unsubscribeAll() {}
    override fun resubscribeAll() {}
    override fun syncWithServerForFailure() {}
    override fun checkCourseEnrollment(responses: List<EnrolledCoursesResponse>) {}
    override fun changeNotificationSetting(
        courseId: String,
        channelId: String,
        subscribe: Boolean
    ) {
    }

    override fun isSubscribedByCourseId(channel: String): Boolean {
        return false
    }

    override fun checkAppUpgrade() {}
}
