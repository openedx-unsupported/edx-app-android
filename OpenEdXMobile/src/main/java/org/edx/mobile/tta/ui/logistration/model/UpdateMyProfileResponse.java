package org.edx.mobile.tta.ui.logistration.model;

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

}
