package org.edx.mobile.util

import org.edx.mobile.R

/**
 * This enum defines the URL type of Agreement
 */
enum class AgreementUrlType {
    EULA, TOS, PRIVACY_POLICY;

    /**
     * @return The string resource's ID if it's a valid enum inside [AgreementUrlType].
     */
    fun getStringResId(): Int {
        return when (this) {
            EULA -> R.string.eula_file_link
            TOS -> R.string.terms_file_link
            PRIVACY_POLICY -> R.string.privacy_file_link
        }
    }
}
