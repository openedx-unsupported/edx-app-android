package org.edx.mobile.model.api;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.module.registration.view.IRegistrationFieldView;

import java.util.List;

/**
 * Created by rohan on 2/11/15.
 */
public class RegisterResponse {

    private @SerializedName("success") boolean success = false;
    private @SerializedName("redirect_url") String redirectUrl;
    private @SerializedName("field") String field;
    private @SerializedName("value") String value;

    /* Registration Field error messages */
    private @SerializedName(IRegistrationFieldView.FieldName.PASSWORD) List<RegisterResponseFieldError> passwordErrors;
    private @SerializedName(IRegistrationFieldView.FieldName.EMAIL) List<RegisterResponseFieldError> emailErrors;
    private @SerializedName(IRegistrationFieldView.FieldName.USERNAME) List<RegisterResponseFieldError> usernameErrors;
    private @SerializedName(IRegistrationFieldView.FieldName.NAME) List<RegisterResponseFieldError> nameErrors;
    private @SerializedName(IRegistrationFieldView.FieldName.COUNTRY) List<RegisterResponseFieldError> countryErrors;
    private @SerializedName(IRegistrationFieldView.FieldName.GENDER) List<RegisterResponseFieldError> genderErrors;
    private @SerializedName(IRegistrationFieldView.FieldName.YEAR_OF_BIRTH) List<RegisterResponseFieldError> yearOfBirthErrors;
    private @SerializedName(IRegistrationFieldView.FieldName.LEVEL_OF_EDUCATION) List<RegisterResponseFieldError> levelOfEducationErrors;
    private @SerializedName(IRegistrationFieldView.FieldName.MAILING_ADDRESS) List<RegisterResponseFieldError> mailingAddressErrors;
    private @SerializedName(IRegistrationFieldView.FieldName.GOALS) List<RegisterResponseFieldError> goalErrors;
    private @SerializedName(IRegistrationFieldView.FieldName.HONOR_CODE) List<RegisterResponseFieldError> honorCodeErrors;

    public boolean isSuccess() {
        return success;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    public List<RegisterResponseFieldError> getPasswordErrors() {
        return passwordErrors;
    }

    public List<RegisterResponseFieldError> getEmailErrors() {
        return emailErrors;
    }

    public List<RegisterResponseFieldError> getUsernameErrors() {
        return usernameErrors;
    }

    public List<RegisterResponseFieldError> getNameErrors() {
        return nameErrors;
    }

    public List<RegisterResponseFieldError> getCountryErrors() {
        return countryErrors;
    }

    public List<RegisterResponseFieldError> getGenderErrors() {
        return genderErrors;
    }

    public List<RegisterResponseFieldError> getLevelOfEducationErrors() {
        return levelOfEducationErrors;
    }

    public List<RegisterResponseFieldError> getYearOfBirthErrors() {
        return yearOfBirthErrors;
    }

    public List<RegisterResponseFieldError> getMailingAddressErrors() {
        return mailingAddressErrors;
    }

    public List<RegisterResponseFieldError> getGoalErrors() {
        return goalErrors;
    }

    public List<RegisterResponseFieldError> getHonorCodeErrors() {
        return honorCodeErrors;
    }
}
