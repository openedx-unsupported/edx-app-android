package org.edx.mobile.model;

import com.google.gson.annotations.SerializedName;

public class SubjectModel {
    public enum Type {
        @SerializedName("normal")
        NORMAL,
        @SerializedName("popular")
        POPULAR
    }

    @SerializedName("name")
    public String name;

    @SerializedName("image_name")
    public String imageName;

    @SerializedName("filter")
    public String filter;

    @SerializedName("type")
    public Type type;
}
