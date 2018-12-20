package org.edx.mobile.tta.ui.listing.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

public class Content extends BaseObservable {

    private String name;
    private String image;
    private String category;

    public Content(String name, String image, String category) {
        this.name = name;
        this.image = image;
        this.category = category;
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Bindable
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Bindable
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
