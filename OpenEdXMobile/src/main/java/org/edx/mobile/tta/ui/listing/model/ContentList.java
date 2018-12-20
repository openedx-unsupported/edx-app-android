package org.edx.mobile.tta.ui.listing.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import org.edx.mobile.BR;

public class ContentList extends BaseObservable {

    private String title;

    public ContentList(String title) {
        this.title = title;
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }
}
