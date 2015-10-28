package org.edx.mobile.user;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class FormDescription {
    private
    @SerializedName("fields")
    List<FormField> fields;

    public List<FormField> getFields() {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        return fields;
    }
}
