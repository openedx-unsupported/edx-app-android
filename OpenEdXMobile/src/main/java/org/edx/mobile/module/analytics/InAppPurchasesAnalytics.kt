package org.edx.mobile.module.analytics

import org.edx.mobile.core.IEdxEnvironment
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppPurchasesAnalytics @Inject constructor(
    var environment: IEdxEnvironment
) {
    private var courseId: String = ""
    private var isSelfPaced: Boolean = false
    private var flowType: String = ""
    private var screenName: String = ""
    private var componentId: String = ""
    private var price: String = ""
    private var elapsedTime: Long = 0
    private var loadPriceTime: Long = 0
    private var paymentTime: Long = 0
    private var unlockContentTime: Long = 0
    private var refreshContentTime: Long = 0
    private var upgradeCourseTime: Long = 0
    private var errorMsg: String = ""
    private var actionTaken: String = ""

    fun initCourseValues(
        courseId: String = "",
        isSelfPaced: Boolean = false,
        flowType: String = "",
        screenName: String = "",
        componentId: String = ""
    ) {
        this.courseId = courseId
        this.isSelfPaced = isSelfPaced
        this.flowType = flowType
        this.screenName = screenName
        this.componentId = componentId
        this.price = ""
        resetAnalytics()
    }

    private fun initUpgradeCourseTime() {
        upgradeCourseTime = getCurrentTime()
    }

    fun initPriceTime() {
        loadPriceTime = getCurrentTime()
    }

    fun initUnlockContentTime() {
        unlockContentTime = getCurrentTime()
    }

    fun initPaymentTime() {
        paymentTime = getCurrentTime()
    }

    fun initRefreshContentTime() {
        refreshContentTime = getCurrentTime()
    }

    fun setPrice(price: String) {
        this.price = price
    }

    fun trackIAPEvent(
        eventName: String,
        errorMsg: String = "",
        actionTaken: String = ""
    ) {
        when (eventName) {
            Analytics.Events.IAP_UPGRADE_NOW_CLICKED -> {
                trackEvent(eventName, Analytics.Values.IAP_UPGRADE_NOW_CLICKED)
                initUpgradeCourseTime()
            }
            Analytics.Events.IAP_RESTORE_PURCHASE_CLICKED -> {
                trackEvent(eventName, Analytics.Values.IAP_RESTORE_PURCHASE_CLICKED)
            }
            Analytics.Events.IAP_UNFULFILLED_PURCHASE_INITIATED -> {
                trackEvent(eventName, Analytics.Values.IAP_UNFULFILLED_PURCHASE_INITIATED, flowType)
                initUpgradeCourseTime()
            }
            Analytics.Events.IAP_COURSE_UPGRADE_SUCCESS -> {
                elapsedTime = getCurrentTime() - upgradeCourseTime
                trackEvent(eventName, Analytics.Values.IAP_COURSE_UPGRADE_SUCCESS, flowType)
                upgradeCourseTime = 0
            }
            Analytics.Events.IAP_UNLOCK_UPGRADED_CONTENT_TIME -> {
                elapsedTime = getCurrentTime() - unlockContentTime
                trackEvent(eventName, Analytics.Values.IAP_UNLOCK_UPGRADED_CONTENT_TIME, flowType)
                unlockContentTime = 0
            }
            Analytics.Events.IAP_UNLOCK_UPGRADED_CONTENT_REFRESH_TIME -> {
                if (refreshContentTime > 0) {
                    elapsedTime = getCurrentTime() - refreshContentTime
                    trackEvent(
                        eventName,
                        Analytics.Values.IAP_UNLOCK_UPGRADED_CONTENT_REFRESH_TIME,
                        flowType
                    )
                    refreshContentTime = 0
                }
            }
            Analytics.Events.IAP_PAYMENT_TIME -> {
                elapsedTime = getCurrentTime() - paymentTime
                trackEvent(eventName, Analytics.Values.IAP_PAYMENT_TIME)
                paymentTime = 0
            }
            Analytics.Events.IAP_LOAD_PRICE_TIME -> {
                elapsedTime = getCurrentTime() - loadPriceTime
                trackEvent(eventName, Analytics.Values.IAP_LOAD_PRICE_TIME)
                loadPriceTime = 0
            }
            Analytics.Events.IAP_PAYMENT_CANCELED -> {
                trackEvent(eventName, Analytics.Values.IAP_PAYMENT_CANCELED)
            }
            Analytics.Events.IAP_PAYMENT_ERROR -> {
                this.errorMsg = errorMsg
                trackEvent(eventName, Analytics.Values.IAP_PAYMENT_ERROR)
            }
            Analytics.Events.IAP_COURSE_UPGRADE_ERROR -> {
                this.errorMsg = errorMsg
                trackEvent(eventName, Analytics.Values.IAP_COURSE_UPGRADE_ERROR, flowType)
            }
            Analytics.Events.IAP_PRICE_LOAD_ERROR -> {
                this.errorMsg = errorMsg
                trackEvent(eventName, Analytics.Values.IAP_PRICE_LOAD_ERROR)
            }
            Analytics.Events.IAP_ERROR_ALERT_ACTION -> {
                this.errorMsg = errorMsg
                this.actionTaken = actionTaken
                trackEvent(eventName, Analytics.Values.IAP_ERROR_ALERT_ACTION, flowType)
            }
            Analytics.Events.IAP_NEW_EXPERIENCE_ALERT_ACTION -> {
                this.actionTaken = actionTaken
                trackEvent(eventName, Analytics.Values.IAP_NEW_EXPERIENCE_ALERT_ACTION, flowType)
            }
            Analytics.Events.IAP_RESTORE_SUCCESS_ALERT_ACTION -> {
                this.actionTaken = actionTaken
                trackEvent(eventName, Analytics.Values.IAP_RESTORE_SUCCESS_ALERT_ACTION)
            }
        }
        resetEventValues()
    }

    private fun resetEventValues() {
        elapsedTime = 0
        errorMsg = ""
        actionTaken = ""
    }

    private fun resetAnalytics() {
        loadPriceTime = 0
        paymentTime = 0
        refreshContentTime = 0
        unlockContentTime = 0
        upgradeCourseTime = 0
        elapsedTime = 0
        errorMsg = ""
        actionTaken = ""
    }

    fun reset() {
        courseId = ""
        isSelfPaced = false
        flowType = ""
        screenName = ""
        componentId = ""
        price = ""
        resetAnalytics()
    }

    private fun trackEvent(eventName: String, biValue: String, flowType: String? = null) =
        environment.analyticsRegistry.trackInAppPurchasesEvent(
            eventName,
            biValue,
            courseId,
            isSelfPaced,
            flowType,
            price,
            componentId,
            elapsedTime,
            errorMsg,
            actionTaken,
            screenName
        )

    companion object {
        fun getCurrentTime() = Calendar.getInstance().timeInMillis
    }
}
