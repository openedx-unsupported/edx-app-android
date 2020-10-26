package org.edx.mobile.util

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ImageSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.chip.ChipDrawable
import com.joanzapata.iconify.IconDrawable
import com.joanzapata.iconify.fonts.FontAwesomeIcons
import com.joanzapata.iconify.internal.ParsingUtil
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
        @BindingAdapter("binding:spanText")
        fun setSpanText(textView: TextView, text: String?) {
            if (text.isNullOrBlank().not()) {
                // set text as SPANNABLE so can add more SPANNABLE text e.g `addDateBadge`
                textView.setText(SpannableStringBuilder(text), TextView.BufferType.SPANNABLE)
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
                // if all the item has the same date type so it means badge is already added to parent
                val parentBadgeAdded = hasSameDateTypes(list)
                list.forEach { item ->
                    val childView = inflater.inflate(R.layout.sub_item_course_date_block, null)

                    var labelType = ""
                    val title = SpannableStringBuilder(item.title)

                    if (item.assignmentType.isNullOrBlank().not()) {
                        labelType = "${item.assignmentType}: "
                    }

                    if (item.showLink()) {
                        title.setSpan(UnderlineSpan(), 0, title.length, 0)
                    }

                    isViewVisible(childView.title, item.title.isNullOrBlank().not())
                    childView.title.setText(TextUtils.concat(labelType, title), TextView.BufferType.SPANNABLE)
                    isViewAccessible(childView.title, item.dateBlockBadge)

                    setSpanText(childView.description, item.description)
                    isViewAccessible(childView.description, item.dateBlockBadge)

                    childView.setOnClickListener {
                        if (item.showLink()) {
                            clickListener.onClick(item.link)
                        }
                    }
                    if (!parentBadgeAdded || (item.isToday() && item.dateBlockBadge == CourseDateType.DUE_NEXT)) {
                        setBadge(childView.title, item.dateBlockBadge, item.isToday(), null)
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
        @BindingAdapter("binding:badge", "binding:isToday", "binding:dateBlockItems", requireAll = true)
        fun setBadge(textView: TextView, type: CourseDateType, isToday: Boolean,
                     dateBlockItems: ArrayList<CourseDateBlock>?) {
            if (!hasSameDateTypes(dateBlockItems) && !isToday) {
                return
            }
            var courseDateType = type
            // check dateBlockItems not null its means we can adding the badge in Parent date view
            if (isToday && dateBlockItems != null) {
                courseDateType = CourseDateType.TODAY
            }
            val badgeBackground: Int
            val textAppearance: Int
            var badgeStrokeColor: Int = -1
            var badgeIcon: IconDrawable? = null
            when (courseDateType) {
                CourseDateType.TODAY -> {
                    badgeBackground = R.color.course_today_date
                    textAppearance = R.style.today_date_badge_text_appearance
                }
                CourseDateType.VERIFIED_ONLY -> {
                    badgeBackground = R.color.black
                    textAppearance = R.style.verified_only_badge_text_appearance
                    badgeIcon = IconDrawable(textView.context, FontAwesomeIcons.fa_lock)
                            .sizeRes(textView.context, R.dimen.small_icon_size)
                            .colorRes(textView.context, R.color.white)
                }
                CourseDateType.COMPLETED -> {
                    badgeBackground = R.color.tag_light_silver
                    textAppearance = R.style.completed_badge_text_appearance
                }
                CourseDateType.PAST_DUE -> {
                    badgeBackground = R.color.bullet_light_gray
                    textAppearance = R.style.past_due_badge_text_appearance
                }
                CourseDateType.DUE_NEXT -> {
                    badgeBackground = R.color.bullet_dark_gray
                    textAppearance = R.style.due_next_badge_text_appearance
                }
                CourseDateType.NOT_YET_RELEASED -> {
                    badgeBackground = android.R.color.transparent
                    textAppearance = R.style.not_yet_released_badge_text_appearance
                    badgeStrokeColor = R.color.tag_light_silver_border
                }
                else -> {
                    return
                }
            }
            addDateBadge(textView, courseDateType.getTitle(), badgeBackground, textAppearance, badgeIcon, badgeStrokeColor)
            if (isToday && type == CourseDateType.COMPLETED) {
                setBadge(textView, type, false, dateBlockItems)
            }
        }

        private fun hasSameDateTypes(dateBlockItems: ArrayList<CourseDateBlock>?): Boolean {
            if (dateBlockItems != null && dateBlockItems.isEmpty().not() && dateBlockItems.size > 1) {
                val dateType = dateBlockItems[0].dateBlockBadge
                for (i in 1 until dateBlockItems.size) {
                    if (dateBlockItems[i].dateBlockBadge != dateType &&
                            dateBlockItems[i].dateBlockBadge != CourseDateType.BLANK) {
                        return false
                    }
                }
            }
            return true
        }

        /**
         * Method to add the date badge at the end in given title with styles attributes
         */
        private fun addDateBadge(textView: TextView, title: String, badgeBackground: Int,
                                 textAppearance: Int,
                                 badgeIcon: IconDrawable?,
                                 badgeStrokeColor: Int) {
            // add badge title so can identify the actual badge position
            val titleWithBadge = SpannableStringBuilder((textView.text as Spannable))
            // add space before badge title
            titleWithBadge.append("  $title")
            textView.setText(titleWithBadge, TextView.BufferType.SPANNABLE)

            // setup the date badge
            val string = textView.text as Spannable
            val chipDrawable = ChipDrawable.createFromResource(textView.context,
                    R.xml.dates_badge_chip)
            chipDrawable.text = title
            chipDrawable.setChipBackgroundColorResource(badgeBackground)
            chipDrawable.setTextAppearanceResource(textAppearance)
            if (badgeIcon != null) {
                chipDrawable.chipIcon = badgeIcon
            }
            if (badgeStrokeColor != -1) {
                chipDrawable.setChipStrokeColorResource(badgeStrokeColor)
                chipDrawable.strokeWidth = ParsingUtil.dpToPx(textView.context, 1.0F)
            }
            // Reduce the chip height to match with the provided design.
            chipDrawable.setBounds(0, (chipDrawable.intrinsicHeight * .1).toInt(),
                    chipDrawable.intrinsicWidth, (chipDrawable.intrinsicHeight * .9).toInt())

            val length = textView.text.toString().length
            // Find & replace the badge
            string.setSpan(ImageSpan(chipDrawable), textView.text.indexOf(title), length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            textView.setText(string, TextView.BufferType.SPANNABLE)
        }
    }
}
