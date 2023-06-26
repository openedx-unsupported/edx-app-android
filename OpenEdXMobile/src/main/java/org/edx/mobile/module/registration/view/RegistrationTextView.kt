package org.edx.mobile.module.registration.view

import android.text.InputType
import android.view.View
import org.edx.mobile.module.registration.model.RegistrationFormField

internal class RegistrationTextView(
    field: RegistrationFormField,
    view: View,
) : RegistrationEditTextView(field, view) {
    init {
        mBinding.editText.inputType = InputType.TYPE_CLASS_TEXT
    }
}
