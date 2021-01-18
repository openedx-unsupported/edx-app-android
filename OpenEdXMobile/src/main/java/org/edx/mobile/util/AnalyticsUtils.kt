package org.edx.mobile.util

import com.segment.analytics.Properties
import org.edx.mobile.util.images.ShareUtils.ShareType

class AnalyticsUtils {
    companion object {
        /**
         * Resolves and returns the string alternative of the given share type.
         *
         * @param shareType The share type.
         * @return The string alternative of the given share type.
         */
        fun getShareTypeValue(shareType: ShareType): String {
            return when (shareType) {
                ShareType.FACEBOOK -> "facebook"
                ShareType.TWITTER -> "twitter"
                else -> "other"
            }
        }

        /**
         * Method to remove the un-supported characters by the Firebase Analytics from the
         * given string.
         */
        fun removeUnSupportedCharacters(value: String): String {
            return value.replace(":".toRegex(), "_")
                    .replace("-".toRegex(), "_")
                    .replace(" ".toRegex(), "_")
                    .replace("__".toRegex(), "_")
        }

        /**
         * Method used to format the Analytics data as per firebase recommendations
         * Ref: https://stackoverflow.com/questions/44421234/firebase-analytics-custom-list-of-values
         */
        fun formatFirebaseAnalyticsData(`object`: Any): Properties {
            val properties = `object` as Properties
            val newProperties = Properties()
            for ((key, value) in properties) {
                var entryValueString = value.toString()
                if (entryValueString.length > 100) {
                    // Truncate to first 100 characters
                    entryValueString = entryValueString.trim { it <= ' ' }.substring(0, 100)
                }
                newProperties[removeUnSupportedCharacters(key)] = entryValueString
            }
            return newProperties
        }
    }
}
