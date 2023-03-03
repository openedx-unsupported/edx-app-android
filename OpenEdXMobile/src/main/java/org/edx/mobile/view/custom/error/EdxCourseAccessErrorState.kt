package org.edx.mobile.view.custom.error

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import org.edx.mobile.R
import org.edx.mobile.databinding.LayoutCourseAccessErrorBinding
import org.edx.mobile.extenstion.setVisibility

class EdxCourseAccessErrorState @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val layout: LayoutCourseAccessErrorBinding by lazy {
        LayoutCourseAccessErrorBinding.inflate(LayoutInflater.from(context))
    }

    init {
        this.addView(layout.root)
        context.theme.obtainStyledAttributes(attrs, R.styleable.AccessErrorState, 0, 0).apply {
            try {
                val state = State.values()[getInt(R.styleable.AccessErrorState_state, 0)]
                setState(state)
            } finally {
                recycle()
            }
        }
    }

    fun setState(state: State, date: String? = null) {
        when (state) {
            State.AUDIT_ACCESS_EXPIRED -> {
                layout.heading.text = context.getString(R.string.course_access_expired)
                layout.description.text = context.getString(R.string.message_no_new_session)
                layout.layoutUpgradeFeatures.root.setVisibility(false)
                replacePrimaryWithSecondaryButton(R.string.find_course_btn_text)
            }
            State.IS_UPGRADEABLE -> {
                layout.heading.text = context.getString(R.string.course_access_expired)
                layout.description.text = context.getString(R.string.message_to_upgrade_course)
                layout.layoutUpgradeFeatures.root.setVisibility(true)
                layout.primaryButton.btnUpgrade.isEnabled = false
                layout.primaryButton.shimmerViewContainer.showShimmer(true)
                layout.secondaryButton.root.setVisibility(true)
                layout.secondaryButton.root.text = context.getText(R.string.label_find_a_course)
            }
            State.NOT_STARTED -> {
                layout.heading.text = context.getString(R.string.course_not_started)
                layout.description.text =
                    context.getString(R.string.message_course_not_started, date)
                layout.layoutUpgradeFeatures.root.setVisibility(false)
                replacePrimaryWithSecondaryButton(R.string.find_course_btn_text)
            }
        }
    }

    fun setPrimaryButtonText(text: String) {
        layout.primaryButton.btnUpgrade.text = text
        layout.primaryButton.btnUpgrade.icon =
            ContextCompat.getDrawable(context, R.drawable.ic_lock)
        layout.primaryButton.shimmerViewContainer.postDelayed({
            layout.primaryButton.shimmerViewContainer.hideShimmer()
            layout.primaryButton.btnUpgrade.isEnabled = true
        }, 500)
    }

    fun replacePrimaryWithSecondaryButton(@StringRes resId: Int) {
        layout.primaryButton.shimmerViewContainer.hideShimmer()
        layout.primaryButton.btnUpgrade.text = context.getText(resId)
        layout.primaryButton.btnUpgrade.icon = null
        layout.primaryButton.btnUpgrade.isEnabled = true
        layout.secondaryButton.root.setVisibility(false)
    }

    fun setPrimaryButtonListener(onClickListener: OnClickListener) {
        layout.primaryButton.btnUpgrade.setOnClickListener(onClickListener)
    }

    fun setSecondaryButtonListener(onClickListener: OnClickListener) {
        layout.secondaryButton.root.setOnClickListener(onClickListener)
    }

    fun enableUpgradeButton(enable: Boolean) {
        layout.primaryButton.btnUpgrade.setVisibility(!enable)
        layout.primaryButton.loadingIndicator.setVisibility(enable)
    }

    enum class State {
        /**
         * The learner's audit access for this course has expired, and they are currently unable to
         * upgrade and gain further access
         */
        AUDIT_ACCESS_EXPIRED,

        /**
         * The learner's audit access for this course has expired, but they are able to upgrade
         * the course to gain further access
         */
        IS_UPGRADEABLE,

        /**
         * The course cannot be accessed yet because it hasn't started
         */
        NOT_STARTED,
    }
}
