package org.edx.mobile.tta.ui.otp;

import android.os.Bundle;

import com.google.android.gms.common.api.Status;

public class SmsResponse {
    private Status status;
    private Bundle data;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Bundle getData() {
        return data;
    }

    public void setData(Bundle data) {
        this.data = data;
    }
}
