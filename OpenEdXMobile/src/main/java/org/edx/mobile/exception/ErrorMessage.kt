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
    }
}
