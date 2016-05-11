package org.edx.mobile.profiles;


import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class BadgeAssertion {
    private String username;
    private String assertionUrl;
    private @SerializedName("image_url") String imageUrl;
    private Date created;
    private @SerializedName("badge_class") BadgeClass badgeClass;

    public BadgeAssertion() {}
    public BadgeAssertion(String username, String evidence, String imageUrl, Date created, BadgeClass badgeClass) {
        this.username = username;
        this.assertionUrl = evidence;
        this.imageUrl = imageUrl;
        this.created = created;
        this.badgeClass = badgeClass;
    }


    public String getUsername() {
        return username;
    }

    public String getAssertionUrl() {
        return assertionUrl;
    }

    public String getImageUrl() {
        return imageUrl != null ? imageUrl : (badgeClass!= null ? badgeClass.getImageUrl() : null);
    }

    public Date getCreated() {
        return created;
    }

    public BadgeClass getBadgeClass() {
        return badgeClass;
    }
}
