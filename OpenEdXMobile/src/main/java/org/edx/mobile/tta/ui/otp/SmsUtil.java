package org.edx.mobile.tta.ui.otp;

import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;

public class SmsUtil {
    public static String getOtp(Bundle sms_bndle) {
        String message =(String) sms_bndle . get (SmsRetriever.EXTRA_SMS_MESSAGE);
        if (message == null || message.equals(""))
            return new String();

        String[] otp_arr = message.split(" ");
        if(otp_arr.length<3)
            return "";
        else {
            return otp_arr[otp_arr.length - 2];
        }
    }
}
