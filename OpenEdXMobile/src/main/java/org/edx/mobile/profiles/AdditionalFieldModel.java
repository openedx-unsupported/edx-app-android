package org.edx.mobile.profiles;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AdditionalFieldModel {

    @SerializedName("grade")
    @Expose
    private String grade;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("education_board")
    @Expose
    private String education_board;
    @SerializedName("user")
    @Expose
    private String user;

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEducation_board() {
        return education_board;
    }

    public void setEducation_board(String education_board) {
        this.education_board = education_board;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    @SerializedName("user_type")
    @Expose
    private String user_type;

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    @SerializedName("school")
    @Expose
    private String school;
}
