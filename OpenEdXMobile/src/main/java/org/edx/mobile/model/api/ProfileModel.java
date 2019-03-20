package org.edx.mobile.model.api;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProfileModel implements Serializable {

    public Long id;
    public String username;
    public String email;
    public String name;
    public String course_enrollments;

    public String gender;
    public String title;
    public String classes_taught;
    public String state;
    public String diet_code;
    public String district;
    public String block;
    public String pmis_code;

    //TTA Chirag
    @SerializedName("tag_label")
    private String tagLabel;
    private long following;
    private long followers;

    public String getState()
    {
        if(pmis_code==null)
            return "";
        else
            return pmis_code;
    }
    // public String url;

    public String getTagLabel() {
        return tagLabel;
    }

    public void setTagLabel(String tagLabel) {
        this.tagLabel = tagLabel;
    }

    public long getFollowing() {
        return following;
    }

    public void setFollowing(long following) {
        this.following = following;
    }

    public long getFollowers() {
        return followers;
    }

    public void setFollowers(long followers) {
        this.followers = followers;
    }
}
