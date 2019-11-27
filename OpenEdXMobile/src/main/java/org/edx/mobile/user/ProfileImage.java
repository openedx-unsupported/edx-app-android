package org.edx.mobile.user;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProfileImage implements Serializable {
    @SerializedName("image_url_full")
    @Nullable
    private String imageUrlFull;

    @SerializedName("image_url_large")
    @Nullable
    private String imageUrlLarge;

    @SerializedName("image_url_medium")
    @Nullable
    private String imageUrlMedium;

    @SerializedName("image_url_small")
    @Nullable
    private String imageUrlSmall;

    @SerializedName("has_image")
    private boolean hasImage;

    @Nullable
    public String getImageUrlFull() {
        return imageUrlFull;
    }

    public void setImageUrlFull(@Nullable String imageUrlFull) {
        this.imageUrlFull = imageUrlFull;
    }

    @Nullable
    public String getImageUrlLarge() {
        return imageUrlLarge;
    }

    public void setImageUrlLarge(@Nullable String imageUrlLarge) {
        this.imageUrlLarge = imageUrlLarge;
    }

    @Nullable
    public String getImageUrlMedium() {
        return imageUrlMedium;
    }

    public void setImageUrlMedium(@Nullable String imageUrlMedium) {
        this.imageUrlMedium = imageUrlMedium;
    }

    @Nullable
    public String getImageUrlSmall() {
        return imageUrlSmall;
    }

    public void setImageUrlSmall(@Nullable String imageUrlSmall) {
        this.imageUrlSmall = imageUrlSmall;
    }

    public boolean hasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }
}
