package org.edx.mobile.extenstion

import android.util.Base64

fun Long.encodeToString(): String {
    return Base64.encodeToString(this.toString().toByteArray(), Base64.DEFAULT)
}

fun String.decodeToLong(): Long? {
    return try {
        Base64.decode(this, Base64.DEFAULT).toString(Charsets.UTF_8).toLong()
    } catch (ex: Exception) {
        null
    }
}
