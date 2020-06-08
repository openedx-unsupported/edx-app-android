package org.humana.mobile.tta.data.model.program;

import com.google.gson.annotations.SerializedName;

public class UnitPublish {

    public void setPublish(boolean publish) {
        isPublish = publish;
    }

    @SerializedName("isPublish")
    public boolean isPublish;

    public boolean getIsPublish() {
        return isPublish;
    }



}
