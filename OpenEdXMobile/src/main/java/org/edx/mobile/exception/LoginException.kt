package org.edx.mobile.exception

data class LoginException(
    val loginErrorMessage: LoginErrorMessage
) : Exception()
