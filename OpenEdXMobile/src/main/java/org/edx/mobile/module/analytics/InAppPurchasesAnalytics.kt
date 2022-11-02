package org.edx.mobile.module.analytics

import org.edx.mobile.core.IEdxEnvironment
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppPurchasesAnalytics @Inject constructor(
    var environment: IEdxEnvironment
) {
    private var courseId: String = ""
    private var isSelfPaced: Boolean = false
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
        courseId: String,
        isSelfPaced: Boolean,
        screenName: String,
        componentId: String = ""
    ) {
        this.courseId = courseId
        this.isSelfPaced = isSelfPaced
        this.screenName = screenName
        this.componentId = componentId
        this.price = ""
        resetAnalytics()
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
                upgradeCourseTime = getCurrentTime()
            }
            Analytics.Events.IAP_COURSE_UPGRADE_SUCCESS -> {
                elapsedTime = getCurrentTime() - upgradeCourseTime
                trackEvent(eventName, Analytics.Values.IAP_COURSE_UPGRADE_SUCCESS)
                upgradeCourseTime = 0
            }
            Analytics.Events.IAP_UNLOCK_UPGRADED_CONTENT_TIME -> {
                elapsedTime = getCurrentTime() - unlockContentTime
                trackEvent(eventName, Analytics.Values.IAP_UNLOCK_UPGRADED_CONTENT_TIME)
                unlockContentTime = 0
            }
            Analytics.Events.IAP_UNLOCK_UPGRADED_CONTENT_REFRESH_TIME -> {
                if (refreshContentTime > 0) {
                    elapsedTime = getCurrentTime() - refreshContentTime
                    trackEvent(eventName, Analytics.Values.IAP_UNLOCK_UPGRADED_CONTENT_REFRESH_TIME)
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
            Analytics.Events.IAP_PAYMENT_ERROR -> {
                this.errorMsg = errorMsg
                trackEvent(eventName, Analytics.Values.IAP_PAYMENT_ERROR)
            }
            Analytics.Events.IAP_COURSE_UPGRADE_ERROR -> {
                this.errorMsg = errorMsg
                trackEvent(eventName, Analytics.Values.IAP_COURSE_UPGRADE_ERROR)
            }
            Analytics.Events.IAP_PRICE_LOAD_ERROR -> {
                this.errorMsg = errorMsg
                trackEvent(eventName, Analytics.Values.IAP_PRICE_LOAD_ERROR)
            }
            Analytics.Events.IAP_ERROR_ALERT_ACTION -> {
                this.errorMsg = errorMsg
                this.actionTaken = actionTaken
                trackEvent(eventName, Analytics.Values.IAP_ERROR_ALERT_ACTION)
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

    private fun trackEvent(eventName: String, biValue: String) =
        environment.analyticsRegistry.trackInAppPurchasesEvent(
            eventName,
            biValue,
            courseId,
            isSelfPaced,
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
