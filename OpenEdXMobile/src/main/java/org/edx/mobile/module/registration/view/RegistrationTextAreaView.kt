package org.edx.mobile.module.registration.view

import android.text.InputType
import android.view.Gravity
import android.view.View
import org.edx.mobile.module.registration.model.RegistrationFormField

internal class RegistrationTextAreaView(field: RegistrationFormField, view: View) :
    RegistrationEditTextView(field, view) {
    init {
        mBinding.editText.setLines(INIT_LINES)
        mBinding.editText.maxLines = MAX_LINES

        // allow multiline text
        mBinding.editText.inputType = (InputType.TYPE_CLASS_TEXT
                or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
        // text should start from the left-top
        mBinding.editText.gravity = Gravity.START or Gravity.TOP
    }

    companion object {
        // Number of lines for TextArea
        private const val INIT_LINES = 1
        private const val MAX_LINES = 7
    }
}
