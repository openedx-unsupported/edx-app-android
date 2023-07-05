package org.edx.mobile.module.registration.model

import com.google.gson.annotations.SerializedName

data class RegistrationFormField(
    @SerializedName("required")
    val isRequired: Boolean = false,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("placeholder")
    val placeholder: String? = null,

    @SerializedName("defaultValue")
    val defaultValue: String? = null,

    @SerializedName("restrictions")
    val restriction: RegistrationRestriction = RegistrationRestriction(),

    @SerializedName("errorMessages")
    val errorMessage: ErrorMessage? = null,

    @SerializedName("instructions")
    val instructions: String? = null,

    @SerializedName("type")
    val fieldType: RegistrationFieldType? = RegistrationFieldType.UNKNOWN,

    @SerializedName("label")
    val label: String? = null,

    @SerializedName("options")
    var options: List<RegistrationOption> = arrayListOf(),

    @SerializedName("defaultOption")
    val defaultOption: RegistrationOption? = null,

    @SerializedName("supplementalText")
    val supplementalText: String? = null,

    @SerializedName("supplementalLink")
    val supplementalLink: String? = null,

    @SerializedName("exposed")
    val isExposed: Boolean = false,
) {
    val isEmailField
        get() = RegistrationFieldType.EMAIL.name.equals(name, ignoreCase = true)
    val isConfirmEmailField
        get() = RegistrationFieldType.CONFIRM_EMAIL.name.equals(name, ignoreCase = true)
    val isPasswordField
        get() = RegistrationFieldType.PASSWORD.name.equals(name, ignoreCase = true)
    val isMultiField
        get() = RegistrationFieldType.MULTI.name.equals(name, ignoreCase = true)
}
