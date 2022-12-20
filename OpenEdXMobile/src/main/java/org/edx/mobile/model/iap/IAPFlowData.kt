package org.edx.mobile.model.iap

import java.io.Serializable

data class IAPFlowData(
    var upgradeMode: UpgradeMode = UpgradeMode.NORMAL,
    var productId: String = "",
    var basketId: Long = 0,
    var purchaseToken: String = "",
    var isVerificationPending: Boolean = false
) : Serializable {
    fun clear() {
        upgradeMode = UpgradeMode.NORMAL
        productId = ""
        basketId = 0
        purchaseToken = ""
        isVerificationPending = false
    }

    enum class UpgradeMode {
        NORMAL,
        SILENT;

        fun isSilentMode() = this == SILENT
    }

    enum class IAPAction {
        SHOW_FULL_SCREEN_LOADER,
        PURCHASE_FLOW_COMPLETE
    }
}
