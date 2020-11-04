package org.edx.mobile.util

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import org.edx.mobile.R
import org.edx.mobile.model.course.CourseBannerInfoModel
import org.edx.mobile.model.course.CourseBannerType

/**
 * Utils class to populate the CourseDates banner view
 */
object CourseDateUtil {

    fun setupCourseDatesBanner(view: View, courseBannerInfoModel: CourseBannerInfoModel, clickListener: View.OnClickListener) {
        val context = view.context as Context
        val textView = view.findViewById(R.id.banner_info) as TextView
        val button = view.findViewById(R.id.btn_shift_dates) as Button
        var buttonText = ""
        // Currently we are only handling RESET_DATES case,
        // TODO UPGRADE_TO_GRADED & UPGRADE_TO_RESET will be enable once we are allowed to do payment through mobile
        when (courseBannerInfoModel.datesBannerInfo.getCourseBannerType()) {
            CourseBannerType.UPGRADE_TO_GRADED -> textView.text = context.getText(R.string.course_dates_banner_upgrade_to_graded)
            CourseBannerType.UPGRADE_TO_RESET -> textView.text = context.getText(R.string.course_dates_banner_upgrade_to_reset)
            CourseBannerType.RESET_DATES -> {
                textView.text = context.getText(R.string.course_dates_banner_reset_date)
                buttonText = context.getString(R.string.course_dates_banner_reset_date_button)
            }
            CourseBannerType.INFO_BANNER -> textView.text = context.getText(R.string.course_dates_info_banner)
            CourseBannerType.BLANK -> view.visibility = View.GONE
        }

        if (!TextUtils.isEmpty(textView.text)) {
            if (!TextUtils.isEmpty(buttonText)) {
                button.text = buttonText
                button.visibility = View.VISIBLE
                button.setOnClickListener(clickListener)
            } else {
                button.visibility = View.GONE
            }
            view.visibility = View.VISIBLE
        }
    }
}
