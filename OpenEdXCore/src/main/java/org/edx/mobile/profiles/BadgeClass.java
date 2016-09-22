package org.edx.mobile.profiles;

import com.google.gson.annotations.SerializedName;

public class BadgeClass {

    private String slug;
    private @SerializedName("issuing_component") String issuingComponent;
    private @SerializedName("display_name") String displayName;
    private String description;
    private @SerializedName("image_url") String imageUrl;
    private @SerializedName("course_id") String courseId;

    public BadgeClass() {}

    public BadgeClass(String slug, String issuingComponent, String displayName, String description, String imageUrl, String courseId) {
        this.slug = slug;
        this.issuingComponent = issuingComponent;
        this.displayName = displayName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.courseId = courseId;
    }

    public String getSlug() {
        return slug;
    }

    public String getIssuingComponent() {
        return issuingComponent;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCourseId() {
        return courseId;
    }
}
