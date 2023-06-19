package org.edx.mobile.module.registration.view

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import androidx.core.view.ViewCompat
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import org.edx.mobile.R
import org.edx.mobile.databinding.ViewRegisterAutoCompleteBinding
import org.edx.mobile.extenstion.isNotNullOrEmpty
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.logger.Logger
import org.edx.mobile.module.registration.model.RegistrationFormField
import org.edx.mobile.module.registration.view.IRegistrationFieldView.IActionListener

class RegistrationSelectView(private val mField: RegistrationFormField, mView: View) :
    IRegistrationFieldView {
    private var mBinding: ViewRegisterAutoCompleteBinding =
        ViewRegisterAutoCompleteBinding.bind(mView)
    private var hasFocusLost = false

    init {
        // Remove JSON defined default value, which is appropriate for web but not for mobile.
        // e.g. server sends "--" as the default value for a select box, but on mobile we want
        // the default value to be the label of select box like Gender, Country etc.
        for (option in mField.options) {
            if (option.isDefaultValue) {
                mField.options -= option
                break
            }
        }
        // Set Hint using label text
        mBinding.autoCompleteLayout.hint = mField.label

        //Set options
        mBinding.etAutoComplete.setItems(mField.options)
        setInstructions(mField.instructions)

        // hide error text view
        mBinding.errorMessage.setVisibility(false)

        // This tag is necessary for End-to-End tests to work properly
        mBinding.etAutoComplete.tag = mField.name

        // Do a11y adjustment
        mBinding.etAutoComplete.contentDescription =
            String.format("%s. %s.", mBinding.etAutoComplete.selectedItemName, mField.instructions)
        ViewCompat.setImportantForAccessibility(
            mBinding.instructions,
            ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO
        )
        mBinding.etAutoComplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!mBinding.etAutoComplete.hasValue(s.toString().trim { it <= ' ' })) {
                    mBinding.etAutoComplete.selectedItem = null
                }
                // Don't show the error until view has lost the focus at least once
                if (hasFocusLost) {
                    isValidInput()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        mBinding.etAutoComplete.onFocusChangeListener =
            OnFocusChangeListener { _, hasFocus: Boolean ->
                if (hasFocus) {
                    mBinding.etAutoComplete.showDropDown()
                } else {
                    hasFocusLost = true
                    isValidInput()
                }
            }
    }

    override fun getCurrentValue(): JsonElement {
        // turn text view content into a JsonElement and return it
        return JsonPrimitive(mBinding.etAutoComplete.selectedItemValue)
    }

    override fun setRawValue(value: String?): Boolean {
        if (mBinding.etAutoComplete.hasValue(value)) {
            mBinding.etAutoComplete.select(value)
            return true
        }
        return false
    }

    override fun hasValue(): Boolean {
        return (mBinding.etAutoComplete.selectedItem != null &&
                mBinding.etAutoComplete.selectedItemValue.isNotNullOrEmpty())
    }

    override fun getField(): RegistrationFormField {
        return mField
    }

    override fun getView(): View {
        return mBinding.root
    }

    override fun setInstructions(instructions: String?) {
        mBinding.instructions.setVisibility(instructions.isNotNullOrEmpty())
        if (instructions.isNotNullOrEmpty()) {
            mBinding.instructions.text = instructions
        }
    }

    override fun handleError(errorMessage: String?) {
        if (errorMessage.isNotNullOrEmpty()) {
            mBinding.errorMessage.setVisibility(true)
            mBinding.errorMessage.text = errorMessage
            val errorTag = mBinding.etAutoComplete.resources.getString(R.string.label_error)
            mBinding.etAutoComplete.contentDescription = String.format(
                "%s. %s. %s, %s.",
                mBinding.etAutoComplete.selectedItemName, mField.instructions, errorTag, errorMessage
            )
        } else {
            logger.warn("error message not provided, so not informing the user about this error")
        }
    }

    override fun isValidInput(): Boolean {
        // hide error as we are re-validating the input
        mBinding.errorMessage.setVisibility(false)
        mBinding.etAutoComplete.contentDescription =
            String.format("%s. %s.", mBinding.etAutoComplete.selectedItemName, mField.instructions)

        // check if this is required field and has an input value or field is optional and have some value
        if ((mField.isRequired || (mField.isMultiField && mBinding.etAutoComplete.text.isNotEmpty())) && !hasValue()) {
            initializeErrorMessage()
            return false
        }

        //For select we should not have length checks as there is no input
        return true
    }

    private fun initializeErrorMessage() {
        var errorMessage = mField.errorMessage!!.required
        if (errorMessage.isNotNullOrEmpty()) {
            errorMessage = getView().resources.getString(
                R.string.error_select_or_enter_field,
                mField.label
            )
        }
        handleError(errorMessage)
    }

    override fun setEnabled(enabled: Boolean) {
        mBinding.etAutoComplete.isEnabled = enabled
    }

    override fun setActionListener(actionListener: IActionListener?) {
        // no actions for this field
    }

    override fun getOnErrorFocusView(): View {
        return mBinding.etAutoComplete
    }

    companion object {
        private val logger = Logger(RegistrationSelectView::class.java)
    }
}
