package org.humana.mobile.tta.data.model.program;

import com.google.gson.annotations.SerializedName;

public class UnitConfiguration {


    public int getMAX_ALLOWED_ADD_UNIT() {
        return MAX_ALLOWED_ADD_UNIT;
    }

    public void setMAX_ALLOWED_ADD_UNIT(int MAX_ALLOWED_ADD_UNIT) {
        this.MAX_ALLOWED_ADD_UNIT = MAX_ALLOWED_ADD_UNIT;
    }

    @SerializedName("MAX_ALLOWED_ADD_UNIT")
    private int MAX_ALLOWED_ADD_UNIT;


}
