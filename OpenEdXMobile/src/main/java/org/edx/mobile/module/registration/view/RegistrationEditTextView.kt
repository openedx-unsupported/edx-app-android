package org.edx.mobile.module.registration.view

import android.text.Editable
import android.text.Html
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import androidx.core.view.ViewCompat
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import org.edx.mobile.R
import org.edx.mobile.databinding.ViewRegisterEditTextBinding
import org.edx.mobile.extenstion.isNotNullOrEmpty
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.logger.Logger
import org.edx.mobile.module.registration.model.RegistrationFormField
import org.edx.mobile.module.registration.view.IRegistrationFieldView.IActionListener

open class RegistrationEditTextView(private var mField: RegistrationFormField, view: View) :
    IRegistrationFieldView {
    protected val mBinding: ViewRegisterEditTextBinding = ViewRegisterEditTextBinding.bind(view)
    private var hasFocusLost = false

    init {
        // set max lines for this input to be 1
        mBinding.editText.setLines(1)

        // apply max length
        if (mField.restriction.maxLength > 0) {
            // otherwise, you may end up disabling the field
            mBinding.editText.filters = arrayOf(LengthFilter(mField.restriction.maxLength))
        }
        setInstructions(mField.instructions)

        // set hint
        mBinding.inputLayout.hint = mField.label

        // set default
        mBinding.editText.setText(mField.defaultValue)

        // hide error text view
        mBinding.errorMessage.visibility = View.GONE

        // This tag is necessary for End-to-End tests to work properly
        mBinding.inputLayout.tag = mField.name

        // Do a11y adjustment
        mBinding.inputLayout.contentDescription =
            String.format("%s. %s.", mField.label, mField.instructions)
        mBinding.editText.contentDescription = mField.label
        ViewCompat.setImportantForAccessibility(
            mBinding.instructions,
            ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO
        )

        // Add text change listener
        mBinding.editText.addTextChangedListener(object : TextWatcher {
            /*
             TextWatcher events also trigger at time of registration of this listener. Which we
             don't want in this case. So, to handle it, a flag is required to check if the text
             is being changed by the user or not.
             This issue has also been discussed on stackoverflow through different questions.
             e.g. https://stackoverflow.com/questions/33257937/edittext-addtextchangedlistener-only-for-user-input/33258065#33258065
            */
            private var isChangedByUser = false
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!isChangedByUser) {
                    isChangedByUser = true
                    return
                }
                // Don't show the error until view has lost the focus at least once
                if (hasFocusLost) {
                    isValidInput()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        mBinding.editText.onFocusChangeListener = OnFocusChangeListener { _, hasFocus: Boolean ->
            if (!hasFocus) {
                isValidInput()
                hasFocusLost = true
            }
        }
    }

    override fun setRawValue(value: String?): Boolean {
        mBinding.editText.setText(value)
        return true
    }

    override fun getCurrentValue(): JsonElement? {
        // turn text view content into a JsonElement and return it
        return JsonPrimitive(mBinding.editText.text.toString())
    }

    override fun hasValue(): Boolean {
        return mBinding.editText.text.toString().isNotEmpty()
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
            mBinding.instructions.text = Html.fromHtml(instructions)
        }
    }

    override fun handleError(errorMessage: String?) {
        if (errorMessage.isNotNullOrEmpty()) {
            mBinding.errorMessage.text = Html.fromHtml(errorMessage)
            mBinding.errorMessage.setVisibility(true)
            // Add error message in a11y content for mTextInputLayout
            val errorTag = mBinding.inputLayout.resources.getString(R.string.label_error)
            mBinding.inputLayout.contentDescription = String.format(
                "%s. %s. %s, %s.",
                mField.label, mField.instructions, errorTag, errorMessage
            )
        } else {
            logger.warn("error message not provided, so not informing the user about this error")
        }
    }

    override fun isValidInput(): Boolean {
        // hide error as we are re-validating the input
        mBinding.errorMessage.setVisibility(false)

        // Update a11y content for mTextInputLayout
        mBinding.inputLayout.contentDescription =
            String.format("%s. %s.", mField.label, mField.instructions)

        // check if this is required field and has an input value
        if (mField.isRequired && !hasValue()) {
            var errorMessage = mField.errorMessage?.required
            if (errorMessage.isNullOrEmpty()) {
                errorMessage = getView().resources.getString(
                    R.string.error_enter_field,
                    mField.label
                )
            }
            handleError(errorMessage)
            return false
        }

        // check if length restrictions are followed
        val inputLength = getCurrentValue()?.asString?.length ?: 0
        if (inputLength < mField.restriction.minLength) {
            var errorMessage = mField.errorMessage?.minLength
            if (errorMessage.isNullOrEmpty()) {
                errorMessage = getView().resources.getString(
                    R.string.error_min_length,
                    mField.label, mField.restriction.minLength
                )
            }
            handleError(errorMessage)
            return false
        }
        if ((mField.restriction.maxLength > 0) && (inputLength > mField.restriction.maxLength)) {
            var errorMessage = mField.errorMessage?.maxLength
            if (errorMessage.isNullOrEmpty()) {
                errorMessage = getView().resources.getString(
                    R.string.error_max_length,
                    mField.label, mField.restriction.maxLength
                )
            }
            handleError(errorMessage)
            return false
        }
        return true
    }

    override fun setEnabled(enabled: Boolean) {
        mBinding.inputLayout.isEnabled = enabled
    }

    override fun setActionListener(actionListener: IActionListener?) {
        // no actions for this field
    }

    override fun getOnErrorFocusView(): View {
        return mBinding.inputLayout
    }

    companion object {
        protected val logger = Logger(
            RegistrationEditTextView::class.java
        )
    }
}
