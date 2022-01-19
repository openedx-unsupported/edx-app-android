package org.edx.mobile.exception

/**
 * Error handling model class for ViewModel
 * handle exceptions/error based on errorCode
 */
data class ErrorMessage(
    val errorCode: Int,
    val throwable: Throwable
) {
    companion object {
        // Custom error codes
        const val BANNER_INFO_CODE = 101
        const val COURSE_RESET_DATES_CODE = 102
        const val COURSE_DATES_CODE = 103

        const val ADD_TO_BASKET_CODE = 0x201
        const val CHECKOUT_CODE = 0x202
        const val EXECUTE_ORDER_CODE = 0x203
    }
}
