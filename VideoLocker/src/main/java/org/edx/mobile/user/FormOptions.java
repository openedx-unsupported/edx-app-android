package org.edx.mobile.user;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.module.registration.model.RegistrationOption;

import java.util.List;

public class FormOptions {

    @SerializedName("reference")
    private String reference;

    @SerializedName("values")
    private List<RegistrationOption> values;

    @SerializedName("range_min")
    private Integer rangeMin;

    @SerializedName("range_max")
    private Integer rangeMax;

    @SerializedName("allows_none")
    private boolean allowsNone = true;

    public String getReference() {
        return reference;
    }

    public List<RegistrationOption> getValues() {
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
