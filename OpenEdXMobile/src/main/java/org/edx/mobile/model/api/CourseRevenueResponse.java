package org.edx.mobile.model.api;

import com.google.gson.annotations.SerializedName;

public class CourseRevenueResponse {

    @SerializedName("show_upsell")
    private boolean showUpsell;

    @SerializedName("price")
    private String price;

    @SerializedName("basket_url")
    private String basketUrl;

    public boolean isShowUpsell() {
        return showUpsell;
    }

    public String getPrice() {
        return price;
    }

    public String getBasketUrl() {
        return basketUrl;
    }
}
