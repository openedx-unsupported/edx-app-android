package org.edx.mobile.user;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.module.registration.model.ErrorMessage;
import org.edx.mobile.module.registration.model.RegistrationAgreement;
import org.edx.mobile.module.registration.model.RegistrationFieldType;
import org.edx.mobile.module.registration.model.RegistrationOption;
import org.edx.mobile.module.registration.model.RegistrationRestriction;

import java.util.ArrayList;
import java.util.List;

public class FormField {
    private
    @SerializedName("required")
    boolean required;
    private
    @SerializedName("name")
    String name;
    private
    @SerializedName("placeholder")
    String placeholder;
    private
    @SerializedName("defaultValue")
    String defaultValue;
    private
    @SerializedName("errorMessages")
    ErrorMessage errorMessage;
    private
    @SerializedName("instructions")
    String instructions;
    private
    @SerializedName("type")
    FieldType fieldType;
    private
    @SerializedName("data_type")
    DataType dataType;
    private
    @SerializedName("label")
    String label;
    private
    @SerializedName("options")
    FormOptions options;

    public boolean isRequired() {
        return required;
    }

    public String getName() {
        return name;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    public String getInstructions() {
        return instructions;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public DataType getDataType() {
        return dataType;
    }

    public String getLabel() {
        return label;
    }

    public FormOptions getOptions() {
        return options;
    }
}