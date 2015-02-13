package org.edx.mobile.module.registration.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rohan on 2/12/15.
 */
public class RegistrationAgreement {

    private @SerializedName("url")     String link;
    private @SerializedName("text")    String text;

    public String getText() {
        return text;
    }

    public String getLink() {
        return link;
    }
}
