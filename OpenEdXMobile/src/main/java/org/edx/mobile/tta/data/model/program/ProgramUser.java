package org.edx.mobile.tta.data.model.program;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.user.ProfileImage;

public class ProgramUser {

    private String username;

    private String name;

    @SerializedName("profile_image")
    private ProfileImage profileImage;

    @SerializedName("completed_units")
    private long completedUnits;

    @SerializedName("completed_hours")
    private long completedHours;

    @SerializedName("pending_count")
    private long pendingCount;

}
