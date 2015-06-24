package org.edx.mobile.core;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.edx.mobile.module.notification.DummyNotificationDelegate;
import org.edx.mobile.module.notification.NotificationDelegate;
import org.edx.mobile.module.notification.ParseNotificationDelegate;
import org.edx.mobile.util.Config;

/**
 * Created by hanning on 6/22/15.
 */
public class NotificationProvider  implements Provider<NotificationDelegate> {

    @Inject
    Config config;

    @Override
    public NotificationDelegate get() {
        if ( config.isNotificationEnabled() ) {
            Config.ParseNotificationConfig parseNotificationConfig =
                config.getParseNotificationConfig();
            if (parseNotificationConfig.isEnabled()) {
                return new ParseNotificationDelegate();
            }
            else {
                return new DummyNotificationDelegate();
            }
        }
        else {
            return new DummyNotificationDelegate();
        }
    }
}
