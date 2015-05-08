package org.edx.mobile.module.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.edx.mobile.util.Config;

/**
 * we can not put ParseBroadcastReceiver directly into manifest file as
 * it will cause random crash when the parse notification is disabled.
 */
public class EdxParseBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ( Config.getInstance().isNotificationEnabled() ) {
            Config.ParseNotificationConfig parseNotificationConfig =
                Config.getInstance().getParseNotificationConfig();
            if (parseNotificationConfig.isEnabled()) {
                new com.parse.ParseBroadcastReceiver().onReceive(context, intent);
            }
        }
    }
}