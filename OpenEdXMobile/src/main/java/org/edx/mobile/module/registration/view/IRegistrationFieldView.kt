package org.edx.mobile.module.registration.view

import android.view.LayoutInflater
import android.view.View
import com.google.gson.JsonElement
import org.edx.mobile.R
import org.edx.mobile.logger.Logger
import org.edx.mobile.module.registration.model.RegistrationFieldType
import org.edx.mobile.module.registration.model.RegistrationFormField

interface IRegistrationFieldView {
    // Returns the value that should be sent to the server when registering.
    // Can be null to indicate do not send the field
    fun getCurrentValue(): JsonElement?
    fun hasValue(): Boolean
    fun getField(): RegistrationFormField
    fun getView(): View
    fun setInstructions(instructions: String?)
    fun handleError(errorMessage: String?)
    fun isValidInput(): Boolean
    fun setEnabled(enabled: Boolean)
    fun setActionListener(actionListener: IActionListener)

    /**
     * Get the specific child view which should be focused when the error child view is visible.
     *
     * @return Child view which needs to be focused in case of error.
     */
    fun getOnErrorFocusView(): View

    /**
     * used to programmatically set the value
     * return false if not implemented yet, or can not set the value
     *
     */
    fun setRawValue(value: String?): Boolean

    interface IActionListener {
        fun onClickAgreement()
    }

    /**
     * Factory class to get instance [IRegistrationFieldView]
     * for the given [org.edx.mobile.module.registration.model.RegistrationFormField].
     */
    object Factory {
        private val logger = Logger(Factory::class.java)

        @JvmStatic
        fun getInstance(
            inflater: LayoutInflater,
            field: RegistrationFormField
        ): IRegistrationFieldView? {
            return when (field.fieldType) {
                RegistrationFieldType.EMAIL -> {
                    val view = inflater.inflate(R.layout.view_register_edit_text, null)
                    RegistrationEmailView(field, view)
                }

                RegistrationFieldType.PASSWORD -> {
                    val view = inflater.inflate(R.layout.view_register_edit_text, null)
                    RegistrationPasswordView(field, view)
                }

                RegistrationFieldType.TEXT -> {
                    val view = inflater.inflate(R.layout.view_register_edit_text, null)
                    RegistrationTextView(field, view)
                }

                RegistrationFieldType.TEXTAREA -> {
                    val view = inflater.inflate(R.layout.view_register_edit_text, null)
                    RegistrationTextAreaView(field, view)
                }

                RegistrationFieldType.MULTI -> {
                    val view = inflater.inflate(R.layout.view_register_auto_complete, null)
                    RegistrationSelectView(field, view)
                }

                RegistrationFieldType.PLAINTEXT -> {
                    // For now we aren't using this field type
                    null
                }

                RegistrationFieldType.CHECKBOX -> {
                    val view = inflater.inflate(R.layout.view_register_checkbox, null)
                    RegistrationCheckBoxView(field, view)
                }

                else -> {
                    logger.error(
                        Exception("Unknown field type found for field named: ${field.name} in RegistrationDescription, skipping it!")
                    )
                    null
                }
            }
        }

        @JvmStatic
        fun getResourceId(field: RegistrationFormField): Int {
            return when (field.name) {
                "name" -> R.id.et_name
                "username" -> R.id.et_username
                "email" -> R.id.et_email
                "password" -> R.id.et_password
                "country" -> R.id.et_country
                "level_of_education" -> R.id.et_level_of_education
                "gender" -> R.id.et_gender
                else -> {
                    -1
                }
            }
        }
    }
}
