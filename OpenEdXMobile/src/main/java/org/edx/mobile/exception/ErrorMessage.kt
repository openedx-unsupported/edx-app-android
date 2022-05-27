package org.edx.mobile.exception

import androidx.annotation.StringRes

/**
 * Error handling model class for ViewModel
 * handle exceptions/error based on errorCode
 */
data class ErrorMessage(
    val errorCode: Int,
    val throwable: Throwable,
    @StringRes val errorResId: Int = 0
) {
    companion object {
        // Custom error codes
        const val BANNER_INFO_CODE = 101
        const val COURSE_RESET_DATES_CODE = 102
        const val COURSE_DATES_CODE = 103

        const val ADD_TO_BASKET_CODE = 0x201
        const val CHECKOUT_CODE = 0x202
        const val EXECUTE_ORDER_CODE = 0x203
        const val PAYMENT_SDK_CODE = 0x204
        const val COURSE_REFRESH_CODE = 0x205
        const val PRICE_CODE = 0x206
    }
}
