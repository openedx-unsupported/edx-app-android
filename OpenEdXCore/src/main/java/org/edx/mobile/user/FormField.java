package org.edx.mobile.user;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.module.registration.model.ErrorMessage;

import java.io.Serializable;

public class FormField implements Serializable {
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
    @SerializedName("sub_instructions")
    String subInstructions;
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

    public String getSubInstructions() {
        return subInstructions;
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
