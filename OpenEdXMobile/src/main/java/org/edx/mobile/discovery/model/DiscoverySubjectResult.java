package org.edx.mobile.discovery.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DiscoverySubjectResult implements Parcelable {
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("subtitle")
    @Expose
    private String subtitle;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBanner_image_url() {
        return banner_image_url;
    }

    public void setBanner_image_url(String banner_image_url) {
        this.banner_image_url = banner_image_url;
    }

    public String getCard_image_url() {
        return card_image_url;
    }

    public void setCard_image_url(String card_image_url) {
        this.card_image_url = card_image_url;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("banner_image_url")
    @Expose
    private String banner_image_url;
    @SerializedName("card_image_url")
    @Expose
    private String card_image_url;
    @SerializedName("slug")
    @Expose
    private String slug;
    @SerializedName("uuid")
    @Expose
    private String uuid;

    public int getCardColorName() {
        return cardColorName;
    }

    public void setCardColorName(int cardColorName) {
        this.cardColorName = cardColorName;
    }

    private int cardColorName;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(cardColorName);
        parcel.writeString(uuid);
        parcel.writeString(slug);
        parcel.writeString(card_image_url);
    }
}
