package org.edx.mobile.extenstion

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

inline fun <reified T : Serializable> Bundle.serializable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getSerializable(key) as? T
    }
}

@Throws(IllegalStateException::class)
inline fun <reified T : Serializable> Bundle?.serializableOrThrow(key: String): T {
    return this?.serializable(key) ?: run {
        throw IllegalStateException("No arguments available")
    }
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelable(key) as? T
    }
}

@Throws(IllegalStateException::class)
inline fun <reified T : Parcelable> Bundle?.parcelableOrThrow(key: String): T {
    return this?.parcelable(key) ?: run {
        throw IllegalStateException("No arguments available")
    }
}
