package org.edx.mobile.deeplink

import android.text.TextUtils


/**
 * A PushLink is a DeepLink retrieving through Push Notifications channel.
 */
class PushLink(screenName: String, val title: String?, val body: String,
               map: Map<String, String>) : DeepLink(screenName, map) {

    override fun toString(): String {
        return "PushLink(title=$title, body='$body') ${super.toString()}"
    }

    fun isDeepLink(): Boolean {
        return !TextUtils.isEmpty(screenName)
    }
}
