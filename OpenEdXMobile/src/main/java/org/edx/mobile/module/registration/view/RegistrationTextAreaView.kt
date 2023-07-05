package org.edx.mobile.module.registration.view

import android.text.InputType
import android.view.Gravity
import android.view.View
import org.edx.mobile.module.registration.model.RegistrationFormField

internal class RegistrationTextAreaView(
    field: RegistrationFormField,
    view: View,
) : RegistrationEditTextView(field, view) {
    init {
        mBinding.editText.apply {
            setLines(MIN_LINES_COUNT)
            maxLines = MAX_LINES_HEIGHT
            inputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
            gravity = Gravity.START or Gravity.TOP
        }
    }

    companion object {
        private const val MIN_LINES_COUNT = 1
        private const val MAX_LINES_HEIGHT = 7
    }
}
