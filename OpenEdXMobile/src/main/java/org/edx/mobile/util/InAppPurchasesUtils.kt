package org.edx.mobile.util

import org.edx.mobile.R
import org.edx.mobile.exception.ErrorMessage

object InAppPurchasesUtils {

    val postPurchasedRequests =
        listOf(ErrorMessage.EXECUTE_ORDER_CODE, ErrorMessage.COURSE_REFRESH_CODE)

    /**
     * Method to filter the incomplete purchases for the given enrolled audit course and already paid
     * through google play store.
     * */
    fun getInCompletePurchases(
        auditCoursesSku: List<String>,
        purchases: List<Pair<String, String>>
    ): MutableList<Pair<String, String>> {
        return purchases.filter { it.first in auditCoursesSku }.toMutableList()
    }

    /**
     * Method to get the error message resource id based given request type and http error code.
     *
     * @param requestType request type for error message resource id from [org.edx.mobile.exception.ErrorMessage].
     * @param httpErrorCode http error code singled from the BillingClient OR ecommerce end-points.
     *
     * @return error message resource id.
     * */
    fun getErrorMessage(requestType: Int, httpErrorCode: Int) =
        when (httpErrorCode) {
            400 -> when (requestType) {
                ErrorMessage.ADD_TO_BASKET_CODE -> R.string.error_course_not_found
                ErrorMessage.CHECKOUT_CODE -> R.string.error_payment_not_processed
                ErrorMessage.EXECUTE_ORDER_CODE -> R.string.error_course_not_fullfilled
                else -> R.string.general_error_message
            }
            403 -> when (requestType) {
                ErrorMessage.EXECUTE_ORDER_CODE -> R.string.error_course_not_fullfilled
                else -> R.string.error_user_not_authenticated
            }
            406 -> R.string.error_course_already_paid
            else -> when (requestType) {
                ErrorMessage.PAYMENT_SDK_CODE -> R.string.error_payment_not_processed
                ErrorMessage.PRICE_CODE -> R.string.error_price_not_fetched
                ErrorMessage.COURSE_REFRESH_CODE -> R.string.error_course_not_fullfilled
                else -> R.string.general_error_message
            }
        }
}
