package org.edx.mobile.social

enum class SocialSourceType(
    private val values: List<String>
) {
    UNKNOWN(listOf("unknown")),
    FACEBOOK(listOf("facebook")),
    GOOGLE(listOf("google-oauth2", "google")),
    MICROSOFT(listOf("azuread-oauth2", "azuread"));

    companion object {
        fun fromString(source: String?): SocialSourceType {
            return when (source?.lowercase()) {
                in FACEBOOK.values -> FACEBOOK
                in GOOGLE.values -> GOOGLE
                in MICROSOFT.values -> MICROSOFT
                else -> UNKNOWN
            }
        }
    }
}
