package org.edx.mobile.model.api

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * [AppConfig] handles the app's behavior based on multiple flags. The [AppConfig] is passed as part
 * of a network request to keep it readily updated.
 */
data class AppConfig(

    @SerializedName("value_prop_enabled")
    val isValuePropEnabled: Boolean = false

) : Serializable
