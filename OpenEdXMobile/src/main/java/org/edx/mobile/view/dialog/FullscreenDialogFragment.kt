package org.edx.mobile.view.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.R
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.databinding.DialogFullscreenLoaderBinding
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.module.analytics.InAppPurchasesAnalytics
import org.edx.mobile.util.InAppPurchasesException
import org.edx.mobile.util.NonNullObserver
import org.edx.mobile.util.TextUtils
import org.edx.mobile.viewModel.InAppPurchasesViewModel
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class FullscreenLoaderDialogFragment : DialogFragment() {

    @Inject
    lateinit var environment: IEdxEnvironment

    @Inject
    lateinit var iapAnalytics: InAppPurchasesAnalytics

    private lateinit var binding: DialogFullscreenLoaderBinding

    private val iapViewModel: InAppPurchasesViewModel
            by viewModels(ownerProducer = { requireActivity() })

    private var loaderStartTime: Long = 0
    private val LOADER_START_TIME = "LOADER_START_TIME"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.AppTheme_NoActionBar
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogFullscreenLoaderBinding.inflate(inflater)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, args: Bundle?) {
        super.onViewCreated(view, args)
        loaderStartTime = args?.getLong(LOADER_START_TIME, Calendar.getInstance().timeInMillis)
            ?: Calendar.getInstance().timeInMillis
        intiViews()
        initObservers()
        if (iapViewModel.isVerificationPending) {
            iapViewModel.executeOrder()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(LOADER_START_TIME, loaderStartTime)
    }

    private fun intiViews() {
        binding.materialTextView.setText(getTitle(), TextView.BufferType.SPANNABLE)
    }

    private fun initObservers() {
        iapViewModel.errorMessage.observe(viewLifecycleOwner, NonNullObserver { errorMsg ->
            if (errorMsg.throwable is InAppPurchasesException) {
                when (errorMsg.throwable.httpErrorCode) {
                    HttpStatus.UNAUTHORIZED -> {
                        environment.router?.forceLogout(
                            requireContext(),
                            environment.analyticsRegistry,
                            environment.notificationDelegate
                        )
                        return@NonNullObserver
                    }
                    else -> showUpgradeErrorDialog(
                        errorCode = errorMsg.throwable.httpErrorCode,
                        errorMessage = errorMsg.throwable.errorMessage,
                        errorType = errorMsg.errorCode,
                        retryListener = { _, _ -> iapViewModel.executeOrder() }
                    )
                }
            } else {
                showUpgradeErrorDialog(
                    errorType = errorMsg.errorCode,
                    retryListener = { _, _ ->
                        if (errorMsg.errorCode == ErrorMessage.EXECUTE_ORDER_CODE)
                            iapViewModel.executeOrder()
                        else
                            iapViewModel.refreshCourseData(true)
                    }
                )
            }
            iapViewModel.errorMessageShown()
        })
    }

    private fun showUpgradeErrorDialog(
        errorCode: Int? = null,
        errorMessage: String? = null,
        errorType: Int? = null,
        retryListener: DialogInterface.OnClickListener? = null
    ) {

        val feedbackErrorMessage: String = TextUtils.getFormattedErrorMessage(
            errorCode,
            errorType,
            errorMessage
        ).toString()

        iapAnalytics.trackIAPEvent(
            eventName = Analytics.Events.IAP_COURSE_UPGRADE_ERROR,
            errorMsg = feedbackErrorMessage
        )
        AlertDialogFragment.newInstance(
            getString(R.string.title_upgrade_error),
            getString(R.string.error_course_not_fullfilled),
            getString(R.string.label_refresh_to_retry),
            retryListener?.also {
                iapAnalytics.initRefreshContentTime()
                iapAnalytics.trackIAPEvent(
                    eventName = Analytics.Events.IAP_ERROR_ALERT_ACTION,
                    errorMsg = feedbackErrorMessage,
                    actionTaken = Analytics.Values.ACTION_REFRESH
                )
            },
            getString(R.string.label_get_help),
            { _, _ ->
                environment.router?.showFeedbackScreen(
                    requireActivity(),
                    getString(R.string.email_subject_upgrade_error),
                    feedbackErrorMessage
                )
                iapAnalytics.trackIAPEvent(
                    eventName = Analytics.Events.IAP_ERROR_ALERT_ACTION,
                    errorMsg = feedbackErrorMessage,
                    actionTaken = Analytics.Values.ACTION_GET_HELP
                )
                resetPurchase()
            },
            getString(R.string.label_cancel),
            { _, _ ->
                iapAnalytics.trackIAPEvent(
                    eventName = Analytics.Events.IAP_ERROR_ALERT_ACTION,
                    errorMsg = feedbackErrorMessage,
                    actionTaken = Analytics.Values.ACTION_CLOSE
                )
                resetPurchase()
            }, false
        ).show(childFragmentManager, null)
    }

    private fun getTitle(): SpannableStringBuilder {
        val unlocking = getString(R.string.fullscreen_loader_unlocking)
        val fullAccess = getString(R.string.fullscreen_loader_full_access)
        val toYourCourse = getString(R.string.fullscreen_loader_to_your_course)

        val spannable =
            SpannableStringBuilder(String.format("%s\n%s\n%s", unlocking, fullAccess, toYourCourse))

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.accentAColor)),
            unlocking.length,
            unlocking.length + fullAccess.length + 1,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        return spannable
    }

    /**
     * Method to get the remaining visible time for the loader
     * As per requirements the loader needs to be visible for at least 3 seconds
     */
    fun getRemainingVisibleTime(): Long {
        val totalVisibleTime = Calendar.getInstance().timeInMillis - loaderStartTime
        return if (totalVisibleTime < MINIMUM_DISPLAY_DELAY)
            MINIMUM_DISPLAY_DELAY - totalVisibleTime
        else
            0
    }

    private fun resetPurchase() {
        iapViewModel.resetPurchase(false)
        dismiss()
    }

    companion object {
        const val TAG = "FULLSCREEN_LOADER"
        const val MINIMUM_DISPLAY_DELAY: Long = 3_000

        @JvmStatic
        fun newInstance(): FullscreenLoaderDialogFragment {
            return FullscreenLoaderDialogFragment()
        }
    }
}
