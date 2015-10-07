package org.edx.mobile.user;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.module.registration.model.RegistrationOption;

import java.io.Serializable;
import java.util.List;

public class FormOptions implements Serializable {

    @SerializedName("reference")
    private String reference;

    @SerializedName("values")
    private List<FormOption> values;

    @SerializedName("range_min")
    private Integer rangeMin;

    @SerializedName("range_max")
    private Integer rangeMax;

    @SerializedName("allows_none")
    private boolean allowsNone = true;

    public String getReference() {
        return reference;
    }

    public List<FormOption> getValues() {
        return values;
    }

    public Integer getRangeMin() {
        return rangeMin;
    }

    public Integer getRangeMax() {
        return rangeMax;
    }

    public boolean isAllowsNone() {
        return allowsNone;
    }
}
