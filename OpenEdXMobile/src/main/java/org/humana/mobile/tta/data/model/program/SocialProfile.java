package org.humana.mobile.tta.data.model.program;

import com.google.gson.annotations.SerializedName;

public class SocialProfile {
    @SerializedName("platform")
    public String platform;

    @SerializedName("social_link")
    public String social_link;


    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getSocial_link() {
        return social_link;
    }

    public void setSocial_link(String social_link) {
        this.social_link = social_link;
    }


}
