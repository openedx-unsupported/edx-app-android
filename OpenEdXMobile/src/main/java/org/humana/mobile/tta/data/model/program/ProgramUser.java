package org.humana.mobile.tta.data.model.program;

import com.google.gson.annotations.SerializedName;

import org.humana.mobile.user.ProfileImage;

import java.util.List;

public class ProgramUser {

    public String username;

    public String name;

    @SerializedName("education")
    public String education;

    @SerializedName("profile_image")
    public ProfileImage profileImage;

    @SerializedName("completed_units")
    public long completedUnits;

    @SerializedName("completed_hours")
    public long completedHours;

    @SerializedName("pending_count")
    public long pendingCount;


    public List<SocialProfile> social_profile;

}
