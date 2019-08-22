package org.edx.mobile.tta.data.model.program;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.user.ProfileImage;

public class ProgramUser {

    public String username;

    public String name;

    @SerializedName("profile_image")
    public ProfileImage profileImage;

    @SerializedName("completed_units")
    public long completedUnits;

    @SerializedName("completed_hours")
    public long completedHours;

    @SerializedName("pending_count")
    public long pendingCount;

}
