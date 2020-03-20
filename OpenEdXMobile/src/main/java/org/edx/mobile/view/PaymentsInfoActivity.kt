package org.edx.mobile.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import org.edx.mobile.base.BaseSingleFragmentActivity
import org.edx.mobile.event.CourseUpgradedEvent
import org.edx.mobile.model.api.CourseUpgradeResponse
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.view.Router.EXTRA_COURSE_DATA
import org.edx.mobile.view.Router.EXTRA_COURSE_UPGRADE_DATA

class PaymentsInfoActivity : BaseSingleFragmentActivity() {
    companion object {
        fun newIntent(context: Context, courseData: EnrolledCoursesResponse, courseUpgrade: CourseUpgradeResponse): Intent {
            val intent = Intent(context, PaymentsInfoActivity::class.java)
            intent.putExtra(EXTRA_COURSE_DATA, courseData)
            intent.putExtra(EXTRA_COURSE_UPGRADE_DATA, courseUpgrade)
            return intent
        }
    }

    override fun getToolbarLayoutId(): Int {
        return -1
    }

    override fun getFirstFragment(): Fragment {
        val extras = intent.extras
        if (extras == null) {
            throw IllegalArgumentException()
        } else {
            return PaymentsInfoFragment.newInstance(extras)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        environment.analyticsRegistry.trackScreenView(Analytics.Screens.PAYMENTS_INFO_SCREEN)
    }

    fun onEvent(event: CourseUpgradedEvent) {
        finish()
    }
}
