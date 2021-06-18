package org.edx.mobile.util

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
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

    fun setupCourseDatesBanner(view: View, courseId: String, enrollmentMode: String, isSelfPaced: Boolean,
                               screenName: String, analyticsRegistry: AnalyticsRegistry,
                               courseBannerInfoModel: CourseBannerInfoModel, clickListener: View.OnClickListener) {
        setupCourseDatesBanner(view = view, isCourseDatePage = false, courseId = courseId, enrollmentMode = enrollmentMode,
                isSelfPaced = isSelfPaced, screenName = screenName, analyticsRegistry = analyticsRegistry,
                courseBannerInfoModel = courseBannerInfoModel, clickListener = clickListener)
    }

    fun setupCourseDatesBanner(view: View, isCourseDatePage: Boolean, courseId: String, enrollmentMode: String, isSelfPaced: Boolean,
                               screenName: String, analyticsRegistry: AnalyticsRegistry,
                               courseBannerInfoModel: CourseBannerInfoModel, clickListener: View.OnClickListener) {
        val context = view.context as Context
        val containerLayout = view as LinearLayout
        val title = view.findViewById(R.id.banner_title) as TextView
        val bannerMessage = view.findViewById(R.id.banner_info) as TextView
        val imgView = view.findViewById(R.id.iv_calender) as ImageView
        val button = view.findViewById(R.id.btn_shift_dates) as Button
        var buttonText = ""
        var bannerTypeValue = ""
        var biValue = ""
        val bannerType: CourseBannerType = courseBannerInfoModel.datesBannerInfo.getCourseBannerType()
        // Currently we are only handling RESET_DATES case,
        // TODO UPGRADE_TO_GRADED & UPGRADE_TO_RESET will be enable once we are allowed to do payment through mobile
        if (isCourseDatePage) {
            title.visibility = View.VISIBLE
            containerLayout.setBackgroundColor(Color.TRANSPARENT)
        }
        when (bannerType) {
            CourseBannerType.UPGRADE_TO_GRADED -> {
                bannerMessage.text = context.getText(if (isCourseDatePage) R.string.course_dates_banner_upgrade_to_graded else R.string.course_dashboard_banner_upgrade_to_graded)
                biValue = Analytics.Values.COURSE_DATES_BANNER_UPGRADE_TO_PARTICIPATE
                bannerTypeValue = Analytics.Values.PLS_BANNER_TYPE_UPGRADE_TO_PARTICIPATE
            }
            CourseBannerType.UPGRADE_TO_RESET -> {
                bannerMessage.text = context.getText(if (isCourseDatePage) R.string.course_dates_banner_upgrade_to_reset else R.string.course_dashboard_banner_upgrade_to_reset)
                biValue = Analytics.Values.COURSE_DATES_BANNER_UPGRADE_TO_SHIFT
                bannerTypeValue = Analytics.Values.PLS_BANNER_TYPE_UPGRADE_TO_SHIFT
            }
            CourseBannerType.RESET_DATES -> {
                bannerMessage.text = context.getText(if (isCourseDatePage) R.string.course_dates_banner_reset_date else R.string.course_dashboard_banner_reset_date)
                buttonText = context.getString(R.string.course_dates_banner_reset_date_button)
                biValue = Analytics.Values.COURSE_DATES_BANNER_SHIFT_DATES
                bannerTypeValue = Analytics.Values.PLS_BANNER_TYPE_SHIFT_DATES
            }
            CourseBannerType.INFO_BANNER -> {
                bannerMessage.text = context.getText(if (isCourseDatePage) R.string.course_dates_info_banner else R.string.course_dashboard_info_banner)
                biValue = Analytics.Values.COURSE_DATES_BANNER_INFO
                bannerTypeValue = Analytics.Values.PLS_BANNER_TYPE_INFO
            }
            CourseBannerType.BLANK -> view.visibility = View.GONE
        }

        if (!TextUtils.isEmpty(bannerMessage.text) && (isSelfPaced || (isSelfPaced.not() && bannerType == CourseBannerType.UPGRADE_TO_GRADED))) {
            if (!TextUtils.isEmpty(buttonText)) {
                button.text = buttonText
                button.visibility = View.VISIBLE
                imgView.visibility = if (isCourseDatePage) View.GONE else View.VISIBLE
                button.setOnClickListener { v ->
                    clickListener.onClick(v)
                    analyticsRegistry.trackPLSShiftButtonTapped(courseId, enrollmentMode, screenName)
                }
            } else {
                button.visibility = View.GONE
                imgView.visibility = View.GONE
            }
            view.visibility = View.VISIBLE
            analyticsRegistry.trackPLSCourseDatesBanner(biValue, courseId, enrollmentMode, screenName, bannerTypeValue)
        } else {
            view.visibility = View.GONE
        }
    }
}
