package org.edx.mobile.tta.data.model.search;

import androidx.annotation.NonNull;

public class FilterTag {

    private String value;

    private String display_name;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    @NonNull
    @Override
    public String toString() {
        return display_name != null && !display_name.equals("") ? display_name : value;
    }
}
