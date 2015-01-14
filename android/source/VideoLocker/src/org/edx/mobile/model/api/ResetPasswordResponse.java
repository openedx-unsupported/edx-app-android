package org.edx.mobile.model.api;

import android.text.Html;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ResetPasswordResponse implements Serializable {

    public String value;
    public boolean success;

    public boolean isSuccess() {
        return success;
    }

    public String getPrimaryReason() {
        if (value == null)
            return null;

        return Html.fromHtml(value).toString();
    }
}
