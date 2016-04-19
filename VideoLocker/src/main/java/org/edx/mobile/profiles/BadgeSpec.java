package org.edx.mobile.profiles;

import com.google.gson.annotations.SerializedName;

public class BadgeSpec {

    private String slug;
    private @SerializedName("issuing_component") String issuingComponent;
    private String name;
    private String description;
    private @SerializedName("image_url") String imageUrl;
    private @SerializedName("course_id") String courseId;

    public BadgeSpec() {}

    public BadgeSpec(String slug, String issuingComponent, String name, String description, String imageUrl, String courseId) {
        this.slug = slug;
        this.issuingComponent = issuingComponent;
        this.name = name;
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

    public String getName() {
        return name;
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
