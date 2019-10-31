package org.humana.mobile.user;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProfileImage implements Serializable {
    @SerializedName("full")
    @Nullable
    private String imageUrlFull;

    @SerializedName("large")
    @Nullable
    private String imageUrlLarge;

    @SerializedName("medium")
    @Nullable
    private String imageUrlMedium;

    @SerializedName("small")
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
