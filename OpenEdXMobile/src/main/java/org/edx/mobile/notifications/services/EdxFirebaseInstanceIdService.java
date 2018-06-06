package org.edx.mobile.notifications.services;

import com.google.firebase.messaging.FirebaseMessagingService;

import org.edx.mobile.logger.Logger;

public class EdxFirebaseInstanceIdService extends FirebaseMessagingService {
    protected static final Logger logger = new Logger(EdxFirebaseInstanceIdService.class.getName());

    @Override
    public void onNewToken(String s) {
        logger.debug("Refreshed FCM token: " + s);
        super.onNewToken(s);
    }

}
