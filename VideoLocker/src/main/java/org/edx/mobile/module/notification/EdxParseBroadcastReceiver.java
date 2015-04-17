package org.edx.mobile.module.notification;

import com.parse.ParseBroadcastReceiver;

import org.edx.mobile.util.Config;

/**
 * We need to stop events when Parse is disabled.
 */
public class EdxParseBroadcastReceiver extends ParseBroadcastReceiver {
    public void onReceive(android.content.Context context, android.content.Intent intent) {
        if ( Config.getInstance().isNotificationEnabled() ) {
            Config.ParseNotificationConfig parseNotificationConfig =
                    Config.getInstance().getParseNotificationConfig();
            if (parseNotificationConfig.isEnabled()) {
                super.onReceive(context, intent);
            }
        }
    }
}
