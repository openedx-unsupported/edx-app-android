package org.edx.mobile.view.custom.error

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
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

    fun setState(state: State) {
        when (state) {
            State.AUDIT_ACCESS_EXPIRED -> {
                layout.heading.text = context.getString(R.string.course_access_expired)
                layout.description.text = context.getString(R.string.message_no_new_session)
                layout.layoutUpgradeFeatures.root.setVisibility(false)
                layout.primaryButton.shimmerViewContainer.hideShimmer()
                layout.primaryButton.btnUpgrade.text = context.getText(R.string.label_find_a_course)
                layout.primaryButton.btnUpgrade.icon = null
                layout.secondaryButton.root.setVisibility(false)
            }
            State.IS_UPGRADEABLE -> {
                layout.heading.text = context.getString(R.string.course_access_expired)
                layout.description.text = context.getString(R.string.message_to_upgrade_course)
                layout.layoutUpgradeFeatures.root.setVisibility(true)
                layout.primaryButton.btnUpgrade.isEnabled = false
                layout.primaryButton.shimmerViewContainer.startShimmer()
                layout.secondaryButton.root.setVisibility(true)
                layout.secondaryButton.root.text = context.getText(R.string.label_find_a_course)
            }
            else -> {
                layout.heading.text = context.getString(R.string.course_access_expired)
                layout.layoutUpgradeFeatures.root.setVisibility(false)
                layout.primaryButton.shimmerViewContainer.hideShimmer()
                layout.primaryButton.btnUpgrade.text = context.getText(R.string.label_find_a_course)
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
        NONE, // User don't have the course access of un-known error
        AUDIT_ACCESS_EXPIRED, // User's audit access expired, can't upgrade the course for access.
        IS_UPGRADEABLE // User's audit access expired, can upgrade the course for access.
    }
}
