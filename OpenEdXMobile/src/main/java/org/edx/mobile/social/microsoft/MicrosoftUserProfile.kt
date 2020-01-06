package org.edx.mobile.social.microsoft

import com.google.gson.annotations.SerializedName

class MicrosoftUserProfile(
        @SerializedName("userPrincipalName")
        var email: String?,
        @SerializedName("displayName")
        var fullName: String?,
        @SerializedName("givenName")
        var firstName: String?,
        @SerializedName("surname")
        var surName: String?) {

    override fun toString(): String {
        return "MicrosoftUserProfile: { 'email': '$email', 'displayName':  '$fullName'," +
                "'firstName': '$firstName', 'surName': '$surName'}"
    }
}
