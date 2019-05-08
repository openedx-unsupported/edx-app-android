package org.edx.mobile.tta.ui.otp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.Status;

import de.greenrobot.event.EventBus;

public class SmsModuleReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("_______LOG_______", "sms received");
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            SmsResponse res = new SmsResponse();
            res.setData(intent.getExtras());
            res.setStatus((Status) intent.getExtras().get(SmsRetriever.EXTRA_STATUS));
            Log.d("_______LOG_______", "sms event posted");
            EventBus.getDefault().post(res);
        }
    }
}
