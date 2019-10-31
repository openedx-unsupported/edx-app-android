package org.humana.mobile.tta.ui.otp;

import org.humana.mobile.util.BrowserUtil;

public class OTP_helper {

    public boolean isValidSender(String from) {
        boolean isValid = false;

        if (BrowserUtil.getConfig().get_TTA_OTP_SENDER_ADDRESS() != null &&
                BrowserUtil.getConfig().get_TTA_OTP_SENDER_ADDRESS().equals(""))
        {
            isValid=false;
        }
        else if (!from.equals(BrowserUtil.getConfig().get_TTA_OTP_SENDER_ADDRESS()) || !from.equals("DM-NOTICE"))
        {
            return true;
        }
        return true;
    }

    public String getOTPFromMesssageBody(String message_body) {
        if (message_body == null && message_body.equals(""))
            return null;

        String[] breaked_message = message_body.split(" ");
        String otp = breaked_message[breaked_message.length - 1];

        return otp;
    }

}
