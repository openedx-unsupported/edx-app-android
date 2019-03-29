package org.edx.mobile.tta.data.model.feed;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.user.ProfileImage;

public class SuggestedUser {

    private String username;

    private String name;

    @SerializedName("profile_image")
    @NonNull
    private ProfileImage profileImage;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NonNull
    public ProfileImage getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(@NonNull ProfileImage profileImage) {
        this.profileImage = profileImage;
    }
}
