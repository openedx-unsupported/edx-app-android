package org.edx.mobile.tta.data.model.feed;

import android.support.annotation.NonNull;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.user.ProfileImage;

public class SuggestedUser {

    private String username;

    private String name;

    @SerializedName("profile_image")
    @NonNull
    private ProfileImage profileImage;

    private boolean followed;

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

    public boolean isFollowed() {
        return followed;
    }

    public void setFollowed(boolean followed) {
        this.followed = followed;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof SuggestedUser && (((SuggestedUser) obj).username.equals(username));
    }
}
