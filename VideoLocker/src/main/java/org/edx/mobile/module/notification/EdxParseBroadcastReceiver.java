package org.edx.mobile.module.notification;

import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;

import org.edx.mobile.util.Config;

import roboguice.receiver.RoboBroadcastReceiver;

/**
 * we can not put ParseBroadcastReceiver directly into manifest file as
 * it will cause random crash when the parse notification is disabled.
 */
public class EdxParseBroadcastReceiver extends RoboBroadcastReceiver {
    @Inject
    Config config;

    @Override
    protected void handleReceive(final Context context, Intent intent){
        if( config == null )
            return; //FIXME
        if ( config.isNotificationEnabled() ) {
            Config.ParseNotificationConfig parseNotificationConfig =
                config.getParseNotificationConfig();
            if (parseNotificationConfig.isEnabled()) {
                new com.parse.ParseBroadcastReceiver().onReceive(context, intent);
            }
        }
    }
}