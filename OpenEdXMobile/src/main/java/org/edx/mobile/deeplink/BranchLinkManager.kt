package org.edx.mobile.deeplink

import android.app.Activity
import org.edx.mobile.logger.Logger
import org.json.JSONObject

object BranchLinkManager {
    val logger: Logger = Logger(this.javaClass)
    const val KEY_CLICKED_BRANCH_LINK = "+clicked_branch_link"

    fun checkAndReactIfReceivedLink(activity: Activity, paramsJson: JSONObject) {
        logger.debug("DeepLink received. JSON Details:\n$paramsJson")
        val screenName = paramsJson.optString(DeepLink.Keys.SCREEN_NAME)
        if (screenName != null && screenName.isNotEmpty()) {
            DeepLinkManager.onDeepLinkReceived(activity, DeepLink(screenName, paramsJson))
        }
    }
}
