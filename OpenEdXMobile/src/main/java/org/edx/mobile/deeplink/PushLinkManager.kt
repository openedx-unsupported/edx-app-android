package org.edx.mobile.deeplink

import android.app.Activity
import android.os.Bundle
import com.google.firebase.messaging.RemoteMessage
import de.greenrobot.event.EventBus
import org.edx.mobile.event.PushLinkReceivedEvent
import org.edx.mobile.logger.Logger

object PushLinkManager {
    val logger = Logger(this.javaClass)

    fun checkAndReactIfFCMNotificationReceived(activity: Activity, bundle: Bundle?) {
        val screenName = bundle?.getString(DeepLink.Keys.SCREEN_NAME)
        if (screenName != null && screenName.isNotEmpty()) {
            // Received FCM background notification
            onFCMBackgroundNotificationReceived(activity, screenName, bundle)
        }
    }

    private fun onFCMBackgroundNotificationReceived(activity: Activity, screenName: String, bundle: Bundle) {
        DeepLinkManager.onDeepLinkReceived(activity, DeepLink(screenName, bundle))
    }

    fun onFCMForegroundNotificationReceived(remoteMessage: RemoteMessage?) {
        // Body of message is mandatory for a notification
        remoteMessage?.notification?.body?.run {
            logger.debug("Message Notification Body: " + remoteMessage.notification?.body)
            val screenName = remoteMessage.data[DeepLink.Keys.SCREEN_NAME] ?: ""
            val pushLink = PushLink(screenName, remoteMessage.notification?.title, this, remoteMessage.data)
            // Broadcast the PushLink to all activities and let the foreground activity do the further action
            EventBus.getDefault().post(PushLinkReceivedEvent(pushLink))
        }
    }

    fun onPushLinkActionGranted(activity: Activity, pushLink: PushLink) {
        DeepLinkManager.onDeepLinkReceived(activity, pushLink)
    }
}
