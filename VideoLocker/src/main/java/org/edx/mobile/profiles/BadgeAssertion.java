package org.edx.mobile.profiles;


import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class BadgeAssertion {
    private String username;
    private String evidence;
    private @SerializedName("image_url") String imageUrl;
    private @SerializedName("awarded_on") Date awardedOn;
    private BadgeSpec spec;

    public BadgeAssertion() {}
    public BadgeAssertion(String username, String evidence, String imageUrl, Date awardedOn, BadgeSpec spec) {
        this.username = username;
        this.evidence = evidence;
        this.imageUrl = imageUrl;
        this.awardedOn = awardedOn;
        this.spec = spec;
    }


    public String getUsername() {
        return username;
    }

    public String getEvidence() {
        return evidence;
    }

    public String getImageUrl() {
        return imageUrl != null ? imageUrl : (spec != null ? spec.getImageUrl() : null);
    }

    public Date getAwardedOn() {
        return awardedOn;
    }

    public BadgeSpec getSpec() {
        return spec;
    }
}