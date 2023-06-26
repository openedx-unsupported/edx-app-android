package org.edx.mobile.exception

import java.util.Locale

class LoginErrorMessage(val messageLine1: String, val messageLine2: String) {

    override fun toString(): String {
        return String.format(Locale.getDefault(), "%s. %s", messageLine1, messageLine2)
    }
}
