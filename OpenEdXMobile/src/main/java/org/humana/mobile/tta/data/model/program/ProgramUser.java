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
    public ProfileImageModel profileImage;

    @SerializedName("completed_units")
    public long completedUnits;

    @SerializedName("completed_hours")
    public long completedHours;

    @SerializedName("pending_count")
    public long pendingCount;

    @SerializedName("current_units")
    public long currentUnits;

    @SerializedName("current_hours")
    public long current_hours;

    @SerializedName("current_period_id")
    public long current_period_id;

    @SerializedName("current_period_title")
    public String current_period_title;


    public List<SocialProfile> social_profile;

}
