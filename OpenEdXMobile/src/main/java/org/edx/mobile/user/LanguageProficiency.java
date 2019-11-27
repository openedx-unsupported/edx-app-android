package org.edx.mobile.user;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class LanguageProficiency {
    @NonNull
    @SerializedName("code")
    String code;

    public LanguageProficiency() {
    }

    public LanguageProficiency(@NonNull String code) {
        this.code = code;
    }

    @NonNull
    public String getCode() {
        return code;
    }

    public void setCode(@NonNull String code) {
        this.code = code;
    }
}
