package org.edx.mobile.model.registration;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class RegistrationFormField {
    private @SerializedName("required")         boolean required;
    private @SerializedName("name")             String name;
    private @SerializedName("placeholder")      String placeholder;
    private @SerializedName("defaultValue")     String defaultValue;
    private @SerializedName("restrictions")     RegistrationRestriction restriction;
    private @SerializedName("errorMessages")    ErrorMessage errorMessage;
    private @SerializedName("instructions")     String instructions;
    private @SerializedName("type")             String fieldType;
    private @SerializedName("label")            String label;
    private @SerializedName("options")          List<RegistrationOption> options; // may be empty
    private @SerializedName("defaultOption")    RegistrationOption defaultOption; // may be nil

    public boolean isRequired() {
        return required;
    }

    public String getName() {
        return name;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public RegistrationOption getDefaultOption() {
        return defaultOption;
    }

    public RegistrationRestriction getRestriction() {
        return restriction;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    public String getInstructions() {
        return instructions;
    }

    public String getFieldType() {
        return fieldType;
    }

    public String getLabel() {
        return label;
    }

    public List<RegistrationOption> getOptions() {
        if (options == null) {
            options = new ArrayList<>();
        }
        return options;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}