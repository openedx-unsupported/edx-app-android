package org.edx.mobile.util

/**
 *
 * Signals that the user unable to complete the in-app purchases follow it being not parsable or
 * incomplete according to what we expect.
 *
 * @param httpErrorCode stores the error codes can be either [BillingClient][com.android.billingclient.api.BillingClient]
 *                      OR http error codes for ecommerce end-points, and setting it up to `-1`
 *                      cause some at some service return error code `0`.
 * @param errorMessage stores the error messages received from BillingClient & ecommerce end-points.
 * */
class InAppPurchasesException(val httpErrorCode: Int = -1, val errorMessage: String? = null) :
    Exception(errorMessage)
