package org.edx.mobile.extenstion

import android.content.Intent
import android.os.Build
import java.io.Serializable


inline fun <reified T : Serializable> Intent.serializable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializableExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getSerializableExtra(key) as? T
    }
}
