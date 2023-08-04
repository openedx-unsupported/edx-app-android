package org.edx.mobile.social

/**
 * Enumerates sources of social authentication.
 *
 * This enum provides predefined sources to categorize and manage social authentication
 * providers. Each constant includes associated identifiers used by authentication
 * providers.
 */
enum class SocialAuthSource(
    private val values: List<String>
) {
    UNKNOWN(listOf("unknown")),
    FACEBOOK(listOf("facebook")),
    GOOGLE(listOf("google-oauth2", "google")),
    MICROSOFT(listOf("azuread-oauth2", "azuread"));

    companion object {
        fun fromString(source: String?): SocialAuthSource {
            return when (source?.lowercase()) {
                in FACEBOOK.values -> FACEBOOK
                in GOOGLE.values -> GOOGLE
                in MICROSOFT.values -> MICROSOFT
                else -> UNKNOWN
            }
        }
    }
}
