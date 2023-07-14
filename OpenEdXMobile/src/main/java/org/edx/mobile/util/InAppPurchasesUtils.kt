package org.edx.mobile.util

import com.android.billingclient.api.Purchase
import org.edx.mobile.R
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.model.iap.IAPFlowData

object InAppPurchasesUtils {

    /**
     * Method to filter the incomplete purchases for the given enrolled audit course and already paid
     * through google play store.
     * */
    fun getInCompletePurchases(
        auditCourses: List<IAPFlowData>,
        purchases: List<Purchase>,
        flowType: IAPFlowData.IAPFlowType,
        screenName: String
    ): MutableList<IAPFlowData> {
        purchases.forEach { purchase ->
            auditCourses.find { course ->
                purchase.products.first().equals(course.productId)
            }?.apply {
                this.purchaseToken = purchase.purchaseToken
                this.flowType = flowType
                this.screenName = screenName
            }
        }
        return auditCourses.filter { it.purchaseToken.isNotBlank() }.toMutableList()
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
            HttpStatus.BAD_REQUEST -> when (requestType) {
                ErrorMessage.ADD_TO_BASKET_CODE -> R.string.error_course_not_found
                ErrorMessage.CHECKOUT_CODE -> R.string.error_payment_not_processed
                ErrorMessage.EXECUTE_ORDER_CODE -> R.string.error_course_not_fullfilled
                else -> R.string.general_error_message
            }

            HttpStatus.FORBIDDEN -> when (requestType) {
                ErrorMessage.EXECUTE_ORDER_CODE -> R.string.error_course_not_fullfilled
                else -> R.string.error_user_not_authenticated
            }

            HttpStatus.NOT_ACCEPTABLE -> R.string.error_course_already_paid
            HttpStatus.CONFLICT -> when (requestType) {
                ErrorMessage.EXECUTE_ORDER_CODE -> R.string.error_course_paid_and_verified
                else -> R.string.general_error_message
            }

            else -> when (requestType) {
                ErrorMessage.PAYMENT_SDK_CODE -> R.string.error_payment_not_processed
                ErrorMessage.PRICE_CODE -> R.string.error_price_not_fetched
                ErrorMessage.COURSE_REFRESH_CODE -> R.string.error_course_not_fullfilled
                else -> R.string.general_error_message
            }
        }
}
