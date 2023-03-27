package org.edx.mobile.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.R
import org.edx.mobile.databinding.ActivityCourseTabsDashboardBinding
import org.edx.mobile.deeplink.ScreenDef
import org.edx.mobile.event.CourseDashboardRefreshEvent
import org.edx.mobile.event.CourseUpgradedEvent
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.greenrobot.eventbus.EventBus

@AndroidEntryPoint
class CourseTabsDashboardActivity :
    OfflineSupportBaseActivity<ActivityCourseTabsDashboardBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.add(
                R.id.fragment_container_view,
                CourseTabsDashboardFragment.newInstance(intent.extras),
                null
            )
            fragmentTransaction.disallowAddToBackStack()
            fragmentTransaction.commit()
        }
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().removeStickyEvent(CourseUpgradedEvent::class.java)
    }

    override fun getViewResourceId(): Int {
        return R.layout.activity_course_tabs_dashboard
    }

    override fun getRefreshEvent(): Any {
        return CourseDashboardRefreshEvent()
    }

    companion object {
        @JvmStatic
        fun newIntent(
            activity: Context,
            courseData: EnrolledCoursesResponse?,
            courseId: String?,
            componentId: String?,
            topicId: String?,
            threadId: String?,
            @ScreenDef screenName: String?
        ): Intent {
            return Intent(activity, CourseTabsDashboardActivity::class.java).apply {
                putExtra(Router.EXTRA_COURSE_DATA, courseData)
                putExtra(Router.EXTRA_COURSE_ID, courseId)
                putExtra(Router.EXTRA_COURSE_COMPONENT_ID, componentId)
                putExtra(Router.EXTRA_DISCUSSION_TOPIC_ID, topicId)
                putExtra(Router.EXTRA_DISCUSSION_THREAD_ID, threadId)
                putExtra(Router.EXTRA_SCREEN_NAME, screenName)
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
        }
    }
}
