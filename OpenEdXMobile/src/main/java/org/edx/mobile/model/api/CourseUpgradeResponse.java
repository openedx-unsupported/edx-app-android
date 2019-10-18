package org.edx.mobile.model.api;

import com.google.gson.annotations.SerializedName;

public class CourseUpgradeResponse {

    @SerializedName("show_upsell")
    private boolean showUpsell;

    @SerializedName("price")
    private String price;

    @SerializedName("basket_url")
    private String basketUrl;

    public boolean showUpsell() {
        return showUpsell;
    }

    public String getPrice() {
        return price;
    }

    public String getBasketUrl() {
        return basketUrl;
    }
}
