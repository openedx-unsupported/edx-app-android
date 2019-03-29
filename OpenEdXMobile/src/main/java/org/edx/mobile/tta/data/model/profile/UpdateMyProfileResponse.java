package org.edx.mobile.tta.data.model.profile;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.tta.data.model.authentication.RegistrationError;

public class UpdateMyProfileResponse {

    boolean success;
    String name;
    String email;
    String gender;
    String title;
    String classes_taught;
    String state;
    String district;
    String block;
    String pmis_code;
    String diet_code;
    RegistrationError error;

    //TTA Chirag: Tags associated with user
    @SerializedName("tag_label")
    private String tagLabel;
    private long following;
    private long followers;

    public boolean getSuccess() {
        return success;
    }
    public String getName() {
        return name;
    }
    public String getGender() {
        return gender;
    }
    public String getEmail() {
        return email;
    }
    public String getTitle() {
        return title;
    }
    public String getClasses_taught() {
        return classes_taught;
    }
    public String getState() {
        return state;
    }
    public String getDistrict() {
        return district;
    }
    public String getBlock() {
        return block;
    }
    public String getPMIS_code() {
        return pmis_code;
    }
    public String getDIETCode() {
        return diet_code;
    }
    public RegistrationError getRegistrationError() {
        return error;
    }

    public String getTagLabel() {
        return tagLabel;
    }
    public long getFollowing() {
        return following;
    }
    public long getFollowers() {
        return followers;
    }
}
