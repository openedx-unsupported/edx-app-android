package org.edx.mobile.module.registration.view

import android.text.InputType
import android.view.View
import com.google.android.material.textfield.TextInputLayout
import org.edx.mobile.module.registration.model.RegistrationFormField

internal class RegistrationPasswordView(
    field: RegistrationFormField,
    view: View,
) : RegistrationEditTextView(field, view) {
    init {
        mBinding.editText.inputType = (InputType.TYPE_CLASS_TEXT
                or InputType.TYPE_TEXT_VARIATION_PASSWORD
                or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
        mBinding.inputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
    }
}
