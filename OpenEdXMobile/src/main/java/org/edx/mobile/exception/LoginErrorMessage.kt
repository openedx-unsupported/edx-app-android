package org.edx.mobile.exception

import java.util.Locale

data class LoginErrorMessage(
    val messageLine1: String,
    val messageLine2: String,
) {
    override fun toString(): String {
        return "$messageLine1. $messageLine2".format(Locale.getDefault())
    }
}
