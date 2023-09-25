package org.edx.mobile.module.registration.view

import android.text.InputType
import android.view.View
import org.edx.mobile.R
import org.edx.mobile.module.registration.model.RegistrationFormField
import org.edx.mobile.util.InputValidationUtil

internal class RegistrationEmailView(
    field: RegistrationFormField,
    view: View,
) : RegistrationEditTextView(field, view) {

    init {
        mBinding.editText.inputType = (InputType.TYPE_CLASS_TEXT
                or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
    }

    override fun isValidInput(): Boolean {
        var isValidInput = super.isValidInput()
        if (isValidInput) {
            if (!InputValidationUtil.isValidEmail(getCurrentValue()?.asString)) {
                handleError(getView().resources.getString(R.string.error_invalid_email))
                isValidInput = false
            }
        }
        return isValidInput
    }
}
