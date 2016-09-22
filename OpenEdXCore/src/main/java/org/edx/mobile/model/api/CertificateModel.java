package org.edx.mobile.model.api;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CertificateModel implements Serializable {

    @SerializedName("url")
    public String certificateURL;

}
