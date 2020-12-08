package org.edx.mobile.util

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import org.edx.mobile.R
import org.edx.mobile.model.course.CourseBannerInfoModel
import org.edx.mobile.model.course.CourseBannerType
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.module.analytics.AnalyticsRegistry

/**
 * Utils class to populate the CourseDates banner view
 */
object CourseDateUtil {

    fun setupCourseDatesBanner(view: View, courseId: String, enrollmentMode: String, screenName: String,
                               analyticsRegistry: AnalyticsRegistry, courseBannerInfoModel: CourseBannerInfoModel,
                               clickListener: View.OnClickListener) {
        val context = view.context as Context
        val textView = view.findViewById(R.id.banner_info) as TextView
        val button = view.findViewById(R.id.btn_shift_dates) as Button
        var buttonText = ""
        var bannerType = ""
        var biValue = ""
        // Currently we are only handling RESET_DATES case,
        // TODO UPGRADE_TO_GRADED & UPGRADE_TO_RESET will be enable once we are allowed to do payment through mobile
        when (courseBannerInfoModel.datesBannerInfo.getCourseBannerType()) {
            CourseBannerType.UPGRADE_TO_GRADED -> {
                textView.text = context.getText(R.string.course_dates_banner_upgrade_to_graded)
                biValue = Analytics.Values.COURSE_DATES_BANNER_UPGRADE_TO_PARTICIPATE
                bannerType = Analytics.Values.PLS_BANNER_TYPE_UPGRADE_TO_PARTICIPATE
            }
            CourseBannerType.UPGRADE_TO_RESET -> {
                textView.text = context.getText(R.string.course_dates_banner_upgrade_to_reset)
                biValue = Analytics.Values.COURSE_DATES_BANNER_UPGRADE_TO_SHIFT
                bannerType = Analytics.Values.PLS_BANNER_TYPE_UPGRADE_TO_SHIFT
            }
            CourseBannerType.RESET_DATES -> {
                textView.text = context.getText(R.string.course_dates_banner_reset_date)
                buttonText = context.getString(R.string.course_dates_banner_reset_date_button)
                biValue = Analytics.Values.COURSE_DATES_BANNER_SHIFT_DATES
                bannerType = Analytics.Values.PLS_BANNER_TYPE_SHIFT_DATES
            }
            CourseBannerType.INFO_BANNER -> {
                textView.text = context.getText(R.string.course_dates_info_banner)
                biValue = Analytics.Values.COURSE_DATES_BANNER_INFO
                bannerType = Analytics.Values.PLS_BANNER_TYPE_INFO
            }
            CourseBannerType.BLANK -> view.visibility = View.GONE
        }

        if (!TextUtils.isEmpty(textView.text)) {
            if (!TextUtils.isEmpty(buttonText)) {
                button.text = buttonText
                button.visibility = View.VISIBLE
                button.setOnClickListener { v ->
                    clickListener.onClick(v)
                    analyticsRegistry.trackPLSShiftButtonTapped(courseId, enrollmentMode, screenName)
                }
            } else {
                button.visibility = View.GONE
            }
            view.visibility = View.VISIBLE
            analyticsRegistry.trackPLSCourseDatesBanner(biValue, courseId, enrollmentMode, screenName, bannerType)
        }
    }
}
