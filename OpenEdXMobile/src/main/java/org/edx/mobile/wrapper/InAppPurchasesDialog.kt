package org.edx.mobile.wrapper

import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import org.edx.mobile.R
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.module.analytics.InAppPurchasesAnalytics
import org.edx.mobile.util.InAppPurchasesException
import org.edx.mobile.util.InAppPurchasesUtils
import org.edx.mobile.util.TextUtils
import org.edx.mobile.view.dialog.AlertDialogFragment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppPurchasesDialog @Inject constructor(
    var environment: IEdxEnvironment,
    var iapAnalytics: InAppPurchasesAnalytics
) {

    fun handleIAPException(
        fragment: Fragment,
        errorMessage: ErrorMessage,
        retryListener: DialogInterface.OnClickListener?,
        cancelListener: DialogInterface.OnClickListener? = null
    ) {
        errorMessage.throwable as InAppPurchasesException
        when (errorMessage.throwable.httpErrorCode) {
            HttpStatus.UNAUTHORIZED -> {
                environment.router?.forceLogout(
                    fragment.requireContext(),
                    environment.analyticsRegistry,
                    environment.notificationDelegate
                )
                return
            }
            else -> {
                cancelListener?.let {
                    showPostUpgradeErrorDialog(
                        context = fragment,
                        errorMessage = errorMessage,
                        retryListener = retryListener,
                        cancelListener = cancelListener
                    )
                } ?: showUpgradeErrorDialog(
                    context = fragment,
                    errorMessage = errorMessage,
                    retryListener = retryListener
                )
            }
        }
    }

    /**
     * Shows Alert Dialog for all error cases that can occur before a successful course purchase i.e
     * SKU/Product ID not available (Enrollment-API), while fetching the price from Play Console
     * (Payment SDK), Adding course in purchase basket/cart (Basket-API), during basket checkout
     * (Checkout-API) or while purchasing a course (Payment SDK).
     *
     * All Alerts (except Price Fetching): Two buttons; Cancel and Get help, without any listener
     * Fetching Price Alert: Two buttons; Try again & Cancel, with a listener for Try again
     *
     * Events are also tracked on interaction with the alert.
     *
     * @param context Fragment context for resolving message strings and displaying the dialog
     * @param errorMessage API error response for feedback message
     * @param retryListener Retry listener to fetch the course price again
     */
    fun showUpgradeErrorDialog(
        context: Fragment,
        errorMessage: ErrorMessage = ErrorMessage(0, InAppPurchasesException()),
        retryListener: DialogInterface.OnClickListener? = null,
    ) {
        // To restrict showing error dialog on an unattached fragment
        if (!context.isAdded) return
        errorMessage.throwable as InAppPurchasesException

        val feedbackErrorMessage: String = TextUtils.getFormattedErrorMessage(
            errorMessage.requestType,
            errorMessage.getHttpErrorCode(),
            errorMessage.getErrorMessage()
        ).toString()

        when (errorMessage.requestType) {
            ErrorMessage.PAYMENT_SDK_CODE -> iapAnalytics.trackIAPEvent(
                eventName = Analytics.Events.IAP_PAYMENT_ERROR,
                errorMsg = feedbackErrorMessage
            )
            ErrorMessage.PRICE_CODE -> iapAnalytics.trackIAPEvent(
                eventName = Analytics.Events.IAP_PRICE_LOAD_ERROR,
                errorMsg = feedbackErrorMessage
            )
            else -> iapAnalytics.trackIAPEvent(
                eventName = Analytics.Events.IAP_COURSE_UPGRADE_ERROR,
                errorMsg = feedbackErrorMessage
            )
        }
        @StringRes val errorResId =
            InAppPurchasesUtils.getErrorMessage(
                errorMessage.requestType,
                errorMessage.getHttpErrorCode()
            )

        var actionTaken: String = Analytics.Values.ACTION_CLOSE
        @StringRes var positiveBtnResId = R.string.label_close
        if (retryListener != null) {
            when (errorMessage.getHttpErrorCode()) {
                HttpStatus.NOT_ACCEPTABLE -> {
                    positiveBtnResId = R.string.label_refresh_now
                    actionTaken = Analytics.Values.ACTION_REFRESH
                }
                else -> {
                    positiveBtnResId = R.string.try_again
                    actionTaken = Analytics.Values.ACTION_RELOAD_PRICE
                }
            }
        }

        AlertDialogFragment.newInstance(
            context.getString(R.string.title_upgrade_error),
            context.getString(errorResId),
            context.getString(positiveBtnResId),
            { dialog, which ->
                retryListener?.onClick(dialog, which).also {
                    iapAnalytics.trackIAPEvent(
                        eventName = Analytics.Events.IAP_ERROR_ALERT_ACTION,
                        errorMsg = feedbackErrorMessage,
                        actionTaken = actionTaken
                    )
                } ?: run { trackAlertCloseEvent(feedbackErrorMessage) }
            },
            context.getString(if (retryListener != null) R.string.label_cancel else R.string.label_get_help),
            { _, _ ->
                if (retryListener != null) {
                    trackAlertCloseEvent(feedbackErrorMessage)
                    if (context is DialogFragment) context.dismiss()
                } else {
                    showFeedbackScreen(context, feedbackErrorMessage)
                }
            }, false
        ).show(context.childFragmentManager, null)
    }

    /**
     * Shows Alert Dialog for all error cases that can occur after a successful course purchase i.e
     * Course already purchased (Basket-API) or unable to verify the purchase on backend
     * (Execute-API).
     *
     * Course Already Purchased : Three buttons; Refresh Now, Get help and Cancel, with two
     * listeners. One for refresh and the other one for Get help and Cancel
     * Unable to Verify: Three buttons; Refresh to retry, Get help and Cancel, with two listeners.
     * One for Refresh to retry and the other one for Get Help and Cancel
     *
     * Events are also tracked on interaction with the alert.
     *
     * @param context Fragment context for resolving message strings and displaying the dialog
     * @param errorMessage API error response for feedback message
     * @param retryListener Retry listener to fetch the upgraded course again or to verify the
     * purchase
     * @param cancelListener Cancel listener to reset the purchase flow
     */
    private fun showPostUpgradeErrorDialog(
        context: Fragment,
        errorMessage: ErrorMessage = ErrorMessage(0, InAppPurchasesException()),
        retryListener: DialogInterface.OnClickListener? = null,
        cancelListener: DialogInterface.OnClickListener? = null
    ) {

        val feedbackErrorMessage: String = TextUtils.getFormattedErrorMessage(
            errorMessage.requestType,
            errorMessage.getHttpErrorCode(),
            errorMessage.getErrorMessage()
        ).toString()

        iapAnalytics.trackIAPEvent(
            eventName = Analytics.Events.IAP_COURSE_UPGRADE_ERROR,
            errorMsg = feedbackErrorMessage
        )
        AlertDialogFragment.newInstance(
            context.getString(R.string.title_upgrade_error),
            context.getString(R.string.error_course_not_fullfilled),
            context.getString(
                if (HttpStatus.NOT_ACCEPTABLE == errorMessage.getHttpErrorCode()) R.string.label_refresh_now
                else R.string.label_refresh_to_retry
            ),
            { dialog, which ->
                retryListener?.onClick(dialog, which).also {
                    if (HttpStatus.NOT_ACCEPTABLE == errorMessage.getHttpErrorCode()) {
                        // Add Analytics for refresh course on unfulfilled payments
                    } else {
                        iapAnalytics.initRefreshContentTime()
                        iapAnalytics.trackIAPEvent(
                            eventName = Analytics.Events.IAP_ERROR_ALERT_ACTION,
                            errorMsg = feedbackErrorMessage,
                            actionTaken = Analytics.Values.ACTION_REFRESH
                        )
                    }
                }
            },
            context.getString(R.string.label_get_help),
            { dialog, which ->
                cancelListener?.onClick(dialog, which).also {
                    showFeedbackScreen(context, feedbackErrorMessage)
                }
            },
            context.getString(R.string.label_cancel),
            { dialog, which ->
                cancelListener?.onClick(dialog, which).also {
                    trackAlertCloseEvent(feedbackErrorMessage)
                }
            }, false
        ).show(context.childFragmentManager, null)
    }

    private fun trackAlertCloseEvent(feedbackErrorMessage: String) {
        iapAnalytics.trackIAPEvent(
            eventName = Analytics.Events.IAP_ERROR_ALERT_ACTION,
            errorMsg = feedbackErrorMessage,
            actionTaken = Analytics.Values.ACTION_CLOSE
        )
    }

    private fun showFeedbackScreen(context: Fragment, feedbackErrorMessage: String) {
        environment.router?.showFeedbackScreen(
            context.requireActivity(),
            context.getString(R.string.email_subject_upgrade_error),
            feedbackErrorMessage
        )
        iapAnalytics.trackIAPEvent(
            eventName = Analytics.Events.IAP_ERROR_ALERT_ACTION,
            errorMsg = feedbackErrorMessage,
            actionTaken = Analytics.Values.ACTION_GET_HELP
        )
    }

    fun showNewExperienceAlertDialog(
        fragment: Fragment,
        onPositiveClick: DialogInterface.OnClickListener,
        onNegativeClick: DialogInterface.OnClickListener
    ) {
        AlertDialogFragment.newInstance(
            fragment.getString(R.string.silent_course_upgrade_success_title),
            fragment.getString(R.string.silent_course_upgrade_success_message),
            fragment.getString(R.string.label_refresh_now),
            onPositiveClick,
            fragment.getString(R.string.label_continue_without_update),
            onNegativeClick,
            false
        ).show(fragment.childFragmentManager, null)
    }

    /**
     * Method used to display the dialog after completing the Un fulfilled purchases flow.
     *
     * @param fragment context of the screen dialog is display for
     * */
    fun showNoUnFulfilledPurchasesDialog(fragment: Fragment) {
        AlertDialogFragment.newInstance(
            fragment.getString(R.string.title_purchases_restored),
            fragment.getString(R.string.message_purchases_restored),
            fragment.getString(R.string.label_close),
            null,
            fragment.getString(R.string.label_get_help),
        ) { _, _ ->
            environment.router?.showFeedbackScreen(
                fragment.requireActivity(),
                fragment.getString(R.string.email_subject_upgrade_error)
            )
        }.show(fragment.childFragmentManager, null)
    }
}
