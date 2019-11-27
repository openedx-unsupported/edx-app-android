package org.edx.mobile.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Account {

    public static final String ACCOUNT_PRIVACY_SERIALIZED_NAME = "account_privacy";
    public static final String PRIVATE_SERIALIZED_NAME = "private";
    public static final String YEAR_OF_BIRTH_SERIALIZED_NAME = "year_of_birth";

    @SerializedName("username")
    @NonNull
    private String username;

    @SerializedName("bio")
    @Nullable
    private String bio;

    @SerializedName("requires_parental_consent")
    private boolean requiresParentalConsent;

    @SerializedName("name")
    @Nullable // Nullability not specified by API
    private String name;

    @SerializedName("country")
    @Nullable
    private String country; // ISO 3166 country code

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("profile_image")
    @NonNull
    private ProfileImage profileImage;

    @SerializedName(YEAR_OF_BIRTH_SERIALIZED_NAME)
    @Nullable
    private Integer yearOfBirth;

    @SerializedName("level_of_education")
    @Nullable
    private String levelOfEducation;

    @SerializedName("goals")
    @Nullable
    private String goals;

    @SerializedName("language_proficiencies")
    @NonNull
    private List<LanguageProficiency> languageProficiencies = new ArrayList<LanguageProficiency>();

    @SerializedName("gender")
    @Nullable
    private String gender;

    @SerializedName("mailing_address")
    @Nullable
    private String mailingAddress;

    @SerializedName("email")
    @Nullable // Nullability not specified by API
    private String email;

    @SerializedName("date_joined")
    @Nullable // Nullability not specified by API
    private Date dateJoined;

    @SerializedName(ACCOUNT_PRIVACY_SERIALIZED_NAME)
    @Nullable
    private Privacy accountPrivacy;

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @Nullable
    public String getBio() {
        return bio;
    }

    public void setBio(@Nullable String bio) {
        this.bio = bio;
    }

    public boolean requiresParentalConsent() {
        return requiresParentalConsent;
    }

    public void setRequiresParentalConsent(boolean requiresParentalConsent) {
        this.requiresParentalConsent = requiresParentalConsent;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @Nullable
    public String getCountry() {
        return country;
    }

    public void setCountry(@Nullable String country) {
        this.country = country;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    @NonNull
    public ProfileImage getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(@NonNull ProfileImage profileImage) {
        this.profileImage = profileImage;
    }

    @Nullable
    public Integer getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(@Nullable Integer yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    @Nullable
    public String getLevelOfEducation() {
        return levelOfEducation;
    }

    public void setLevelOfEducation(@Nullable String levelOfEducation) {
        this.levelOfEducation = levelOfEducation;
    }

    @Nullable
    public String getGoals() {
        return goals;
    }

    public void setGoals(@Nullable String goals) {
        this.goals = goals;
    }

    @NonNull
    public List<LanguageProficiency> getLanguageProficiencies() {
        return languageProficiencies;
    }

    public void setLanguageProficiencies(@NonNull List<LanguageProficiency> languageProficiencies) {
        this.languageProficiencies = languageProficiencies;
    }

    @Nullable
    public String getGender() {
        return gender;
    }

    public void setGender(@Nullable String gender) {
        this.gender = gender;
    }

    @Nullable
    public String getMailingAddress() {
        return mailingAddress;
    }

    public void setMailingAddress(@Nullable String mailingAddress) {
        this.mailingAddress = mailingAddress;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    public void setEmail(@Nullable String email) {
        this.email = email;
    }

    @NonNull
    public Date getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(@NonNull Date dateJoined) {
        this.dateJoined = dateJoined;
    }

    @Nullable
    public Privacy getAccountPrivacy() {
        return accountPrivacy;
    }

    public void setAccountPrivacy(@Nullable Privacy accountPrivacy) {
        this.accountPrivacy = accountPrivacy;
    }

    public enum Privacy {

        @SerializedName(PRIVATE_SERIALIZED_NAME)
        PRIVATE,

        @SerializedName("all_users")
        ALL_USERS
    }
}
