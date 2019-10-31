package org.humana.mobile.model.json;

import com.google.gson.annotations.SerializedName;

public class CreateGroupResponse {
    @SerializedName("id")
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
