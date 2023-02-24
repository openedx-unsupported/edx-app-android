package org.edx.mobile.view.custom.error

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import org.edx.mobile.R
import org.edx.mobile.databinding.EdxErrorStateBinding
import org.edx.mobile.deeplink.Screen
import org.edx.mobile.deeplink.ScreenDef
import org.edx.mobile.extenstion.setImageDrawable

class EdxErrorState @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val layout: EdxErrorStateBinding by lazy {
        EdxErrorStateBinding.inflate(LayoutInflater.from(context))
    }

    init {
        this.addView(layout.root)
        context.theme.obtainStyledAttributes(attrs, R.styleable.ErrorState, 0, 0).apply {
            try {
                val state = State.values()[getInt(R.styleable.ErrorState_error_state, 0)]
                setState(state)
            } finally {
                recycle()
            }
        }
    }

    fun setState(state: State, @ScreenDef screen: String? = null) {
        when (state) {
            State.NETWORK,
            State.LOAD_ERROR -> {
                layout.icon.setImageDrawable(R.drawable.ic_error_card)
                layout.errorText.text = when (screen) {
                    Screen.MY_COURSES ->
                        context.getText(R.string.message_an_error_occurred_courses)
                    else ->
                        context.getText(R.string.message_an_error_occurred)
                }
                layout.action.text = context.getText(
                    if (state == State.NETWORK) R.string.try_again else R.string.label_go_to_my_course
                )
            }
            State.EMPTY -> {
                layout.icon.setImageDrawable(R.drawable.ic_creative_process)
                layout.errorText.text = context.getText(R.string.find_course_text)
                layout.action.text = context.getText(R.string.find_course_btn_text)
                layout.action.icon = ContextCompat.getDrawable(context, R.drawable.ic_search)
                layout.action.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
                layout.action.iconPadding =
                    context.resources.getDimension(R.dimen.container_padding).toInt()
            }
            State.UPDATE_APP -> {
                layout.icon.setImageDrawable(R.drawable.ic_update_app)
                layout.errorText.text = context.getText(R.string.app_version_unsupported)
                layout.action.text = context.getText(R.string.label_update_now)
            }
        }
    }

    fun setActionListener(onClickListener: OnClickListener) {
        layout.action.setOnClickListener(onClickListener)
    }

    enum class State {
        NETWORK,
        EMPTY,
        LOAD_ERROR,
        UPDATE_APP,
    }
}
