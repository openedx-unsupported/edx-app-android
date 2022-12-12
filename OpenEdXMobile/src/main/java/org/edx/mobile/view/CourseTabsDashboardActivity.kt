package org.edx.mobile.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.R
import org.edx.mobile.base.BaseFragmentActivity
import org.edx.mobile.deeplink.ScreenDef
import org.edx.mobile.event.CourseDashboardRefreshEvent
import org.edx.mobile.event.CourseUpgradedEvent
import org.edx.mobile.http.notifications.SnackbarErrorNotification
import org.edx.mobile.interfaces.RefreshListener
import org.edx.mobile.interfaces.SnackbarStatusListener
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.util.NetworkUtil
import org.greenrobot.eventbus.EventBus

@AndroidEntryPoint
class CourseTabsDashboardActivity : BaseFragmentActivity(), SnackbarStatusListener,
    RefreshListener {

    private lateinit var snackBarErrorNotification: SnackbarErrorNotification
    private var isFullScreenErrorVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_tabs_dashboard)
        snackBarErrorNotification =
            SnackbarErrorNotification(findViewById(R.id.fragment_container_view))

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                add(
                    R.id.fragment_container_view,
                    CourseTabsDashboardFragment.newInstance(intent.extras),
                    null
                )
                disallowAddToBackStack()
                commit()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().removeStickyEvent(CourseUpgradedEvent::class.java)
    }

    override fun hideSnackBar() {
        snackBarErrorNotification.hideError()
    }

    override fun onRestart() {
        super.onRestart()
        if (NetworkUtil.isConnected(this)) {
            snackBarErrorNotification.hideError()
        } else if (!isFullScreenErrorVisible) {
            snackBarErrorNotification.showOfflineError(this)
        }
    }

    override fun resetSnackbarVisibility(fullScreenErrorVisibility: Boolean) {
        isFullScreenErrorVisible = fullScreenErrorVisibility
        val isNetworkConnected = NetworkUtil.isConnected(this)
        if (fullScreenErrorVisibility || isNetworkConnected) {
            snackBarErrorNotification.hideError()
        } else {
            snackBarErrorNotification.showOfflineError(this)
        }
    }

    override fun onRefresh() {
        EventBus.getDefault().post(CourseDashboardRefreshEvent())
    }

    companion object {
        @JvmStatic
        fun newIntent(
            activity: Context,
            courseData: EnrolledCoursesResponse?,
            courseId: String?,
            componentId: String?,
            topicId: String?,
            threadId: String?, announcements: Boolean,
            @ScreenDef screenName: String?
        ): Intent {
            return Intent(activity, CourseTabsDashboardActivity::class.java).apply {
                putExtra(Router.EXTRA_COURSE_DATA, courseData)
                putExtra(Router.EXTRA_COURSE_ID, courseId)
                putExtra(Router.EXTRA_COURSE_COMPONENT_ID, componentId)
                putExtra(Router.EXTRA_DISCUSSION_TOPIC_ID, topicId)
                putExtra(Router.EXTRA_DISCUSSION_THREAD_ID, threadId)
                putExtra(Router.EXTRA_ANNOUNCEMENTS, announcements)
                putExtra(Router.EXTRA_SCREEN_NAME, screenName)
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
        }
    }
}
