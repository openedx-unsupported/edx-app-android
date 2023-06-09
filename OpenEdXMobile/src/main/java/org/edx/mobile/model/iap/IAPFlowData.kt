package org.edx.mobile.model.iap

import java.io.Serializable

data class IAPFlowData(
    var flowType: IAPFlowType = IAPFlowType.USER_INITIATED,
    var courseId: String = "",
    var isCourseSelfPaced: Boolean = false,
    var productId: String = "",
    var basketId: Long = 0,
    var purchaseToken: String = "",
    var price: Double = 0.0,
    var currencyCode: String = "",
    var screenName: String = "",
    var isVerificationPending: Boolean = false
) : Serializable {
    fun clear() {
        courseId = ""
        isCourseSelfPaced = false
        productId = ""
        basketId = 0
        price = 0.0
        currencyCode = ""
        purchaseToken = ""
        isVerificationPending = false
        screenName = ""
    }

    enum class IAPAction {
        SHOW_FULL_SCREEN_LOADER,
        PURCHASE_FLOW_COMPLETE
    }

    enum class IAPFlowType {
        RESTORE,
        SILENT,
        USER_INITIATED;

        fun value(): String {
            return this.name.lowercase()
        }

        fun isSilentMode() = (this == RESTORE || this == SILENT)
    }
}
