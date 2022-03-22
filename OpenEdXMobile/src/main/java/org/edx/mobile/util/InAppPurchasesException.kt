package org.edx.mobile.util

class InAppPurchasesException(
    val errorCode: Int,
    val httpErrorCode: Int,
    val errorMessage: String?
) : Exception(errorMessage)
