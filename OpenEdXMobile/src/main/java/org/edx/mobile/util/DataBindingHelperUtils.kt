package org.edx.mobile.util

import android.content.Context
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import com.joanzapata.iconify.IconDrawable
import com.joanzapata.iconify.fonts.FontAwesomeIcons
import kotlinx.android.synthetic.main.sub_item_course_date_block.view.*
import org.edx.mobile.R
import org.edx.mobile.interfaces.OnDateBlockListener
import org.edx.mobile.model.course.CourseDateBlock

/**
 * DataBindingHelperUtils allows you to specify the method called to set a value,
 * provide your own binding logic, and specify the type of the returned object by using adapters.
 * Ref: https://developer.android.com/topic/libraries/data-binding/binding-adapters
 */
class DataBindingHelperUtils {

    companion object {
        @JvmStatic
        @BindingAdapter("binding:isUserHasAccess")
        fun isViewAccessible(view: View, type: CourseDateType) {
            when (type) {
                CourseDateType.VERIFIED_ONLY,
                CourseDateType.NOT_YET_RELEASED ->
                    view.isEnabled = false
                else ->
                    view.isEnabled = true
            }
        }

        @JvmStatic
        @BindingAdapter("binding:setText")
        fun setText(textView: TextView, text: String?) {
            if (text.isNullOrBlank().not()) {
                textView.text = text
                textView.visibility = View.VISIBLE
            } else {
                textView.visibility = View.GONE
            }
        }

        @JvmStatic
        @BindingAdapter("binding:isVisible")
        fun isViewVisible(view: View, isVisible: Boolean) {
            view.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        }

        @JvmStatic
        @BindingAdapter("binding:addView", "binding:clickListener", requireAll = true)
        fun addView(linearLayout: LinearLayout, list: ArrayList<CourseDateBlock>, clickListener: OnDateBlockListener) {
            val inflater: LayoutInflater = linearLayout.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            if (linearLayout.childCount < 2) {
                list.forEach { item ->
                    val childView = inflater.inflate(R.layout.sub_item_course_date_block, null)

                    var labelType = ""
                    val title = SpannableString(item.title)

                    if (item.assignmentType.isNullOrBlank().not()) {
                        labelType = "${item.assignmentType}: "
                    }

                    if (item.showLink()) {
                        title.setSpan(UnderlineSpan(), 0, title.length, 0)
                    }

                    isViewVisible(childView.title, item.title.isNullOrBlank().not())
                    childView.title.text = TextUtils.concat(labelType, title)
                    isViewAccessible(childView.title, item.dateBlockBadge)

                    setText(childView.description, item.description)
                    isViewAccessible(childView.description, item.dateBlockBadge)

                    childView.setOnClickListener {
                        if (item.showLink()) {
                            clickListener.onClick(item.link)
                        }
                    }
                    linearLayout.addView(childView)
                }
            }
        }

        @JvmStatic
        @BindingAdapter("binding:bulletBackground", "binding:isDatePast", requireAll = true)
        fun setBulletBackground(bulletView: View, type: CourseDateType, isDatePast: Boolean) {
            bulletView.bringToFront()
            when (type) {
                CourseDateType.PAST_DUE -> {
                    bulletView.background = ContextCompat.getDrawable(bulletView.context, R.drawable.black_border_gray_circle)
                }
                CourseDateType.BLANK,
                CourseDateType.COMPLETED,
                CourseDateType.DUE_NEXT,
                CourseDateType.NOT_YET_RELEASED,
                CourseDateType.VERIFIED_ONLY -> {
                    if (isDatePast && (type == CourseDateType.VERIFIED_ONLY).not()) {
                        bulletView.background = ContextCompat.getDrawable(bulletView.context, R.drawable.black_border_white_circle)
                    } else {
                        bulletView.background = ContextCompat.getDrawable(bulletView.context, R.drawable.black_circle)
                    }
                }
            }
        }

        @JvmStatic
        @BindingAdapter("binding:badgeBackground", "binding:isToday", requireAll = true)
        fun setBadgeBackground(textView: TextView, type: CourseDateType, isToday: Boolean) {
            if (isToday || type == CourseDateType.TODAY) {
                textView.text = CourseDateType.TODAY.getTitle()
                textView.setTextColor(ContextCompat.getColor(textView.context, R.color.course_date_badge_black))
                textView.background = ContextCompat.getDrawable(textView.context, R.drawable.yellow_roundedbg)
            } else {
                setText(textView, type.getTitle())
                when (type) {
                    CourseDateType.VERIFIED_ONLY -> {
                        textView.setTextColor(ContextCompat.getColor(textView.context, R.color.course_date_badge_white))
                        textView.background = ContextCompat.getDrawable(textView.context, R.drawable.black_roundedbg)
                        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(textView,
                                IconDrawable(textView.context, FontAwesomeIcons.fa_lock)
                                        .sizeRes(textView.context, R.dimen.small_icon_size)
                                        .colorRes(textView.context, R.color.white),
                                null, null, null
                        )
                    }
                    CourseDateType.COMPLETED -> {
                        textView.setTextColor(ContextCompat.getColor(textView.context, R.color.course_date_badge_gray))
                        textView.background = ContextCompat.getDrawable(textView.context, R.drawable.light_silver_roundedbg)
                    }
                    CourseDateType.PAST_DUE -> {
                        textView.setTextColor(ContextCompat.getColor(textView.context, R.color.course_date_badge_gray))
                        textView.background = ContextCompat.getDrawable(textView.context, R.drawable.light_gray_roundedbg)
                    }
                    CourseDateType.DUE_NEXT -> {
                        textView.setTextColor(ContextCompat.getColor(textView.context, R.color.course_date_badge_white))
                        textView.background = ContextCompat.getDrawable(textView.context, R.drawable.dark_gray_roundedbg)
                    }
                    CourseDateType.NOT_YET_RELEASED -> {
                        textView.setTextColor(ContextCompat.getColor(textView.context, R.color.course_date_badge_silver))
                        textView.background = ContextCompat.getDrawable(textView.context, R.drawable.silver_border_transparent_roundedbg)
                    }
                    else -> {
                        textView.visibility = View.INVISIBLE
                    }
                }
            }
            textView.visibility = View.VISIBLE
        }
    }
}
