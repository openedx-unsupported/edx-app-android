package org.edx.mobile.view.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.edx.mobile.R
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.databinding.DialogUpgradeFeaturesBinding
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.inapppurchases.BillingProcessor
import org.edx.mobile.module.analytics.Analytics.Events
import org.edx.mobile.module.analytics.Analytics.Values
import org.edx.mobile.module.analytics.InAppPurchasesAnalytics
import org.edx.mobile.util.*
import org.edx.mobile.viewModel.InAppPurchasesViewModel
import javax.inject.Inject

@AndroidEntryPoint
class CourseModalDialogFragment : DialogFragment() {

    private lateinit var binding: DialogUpgradeFeaturesBinding
    private var screenName: String = ""
    private var courseId: String = ""
    private var courseSku: String? = null
    private var isSelfPaced: Boolean = false

    private var billingProcessor: BillingProcessor? = null

    private val iapViewModel: InAppPurchasesViewModel
            by viewModels(ownerProducer = { requireActivity() })

    @Inject
    lateinit var environment: IEdxEnvironment

    @Inject
    lateinit var iapAnalytics: InAppPurchasesAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.AppTheme_NoActionBar
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogUpgradeFeaturesBinding.inflate(inflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogSlideUpAndDownAnimation
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        if (environment.config.isIAPEnabled) {
            initBillingProcessor()
        }
    }

    private fun initViews() {
        arguments?.let { bundle ->
            screenName = bundle.getString(KEY_SCREEN_NAME, "")
            courseId = bundle.getString(KEY_COURSE_ID, "")
            courseSku = bundle.getString(KEY_COURSE_PRODUCT)
            isSelfPaced = bundle.getBoolean(KEY_IS_SELF_PACED)
            iapAnalytics.initCourseValues(
                courseId = courseId,
                isSelfPaced = isSelfPaced,
                screenName = screenName
            )
            environment.analyticsRegistry.trackValuePropLearnMoreTapped(courseId, screenName)
            environment.analyticsRegistry.trackValuePropModalView(courseId, screenName)
        }

        binding.dialogTitle.text = ResourceUtil.getFormattedString(
            resources,
            R.string.course_modal_heading,
            KEY_COURSE_NAME,
            arguments?.getString(KEY_COURSE_NAME)
        )
        binding.layoutUpgradeBtn.root.setVisibility(environment.config.isIAPEnabled)
        binding.dialogDismiss.setOnClickListener {
            dialog?.dismiss()
        }
    }

    private fun initBillingProcessor() {
        initObserver()

        binding.layoutUpgradeBtn.btnUpgrade.setOnClickListener {
            iapAnalytics.trackIAPEvent(eventName = Events.IAP_UPGRADE_NOW_CLICKED)
            courseSku?.let {
                iapViewModel.addProductToBasket(it)
            } ?: showUpgradeErrorDialog()
        }
        billingProcessor =
            BillingProcessor(requireContext(), object : BillingProcessor.BillingFlowListeners {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    super.onBillingSetupFinished(billingResult)
                    // Shimmer container taking sometime to get ready and perform the animation, so
                    // by adding the some delay fixed that issue for lower-end devices, and for the
                    // proper animation.
                    binding.layoutUpgradeBtn.shimmerViewContainer.postDelayed({
                        initializeProductPrice()
                    }, 1500)
                    binding.layoutUpgradeBtn.btnUpgrade.isEnabled = false
                }

                override fun onPurchaseCancel(responseCode: Int, message: String) {
                    iapViewModel.endLoading()
                    showUpgradeErrorDialog(
                        errorResId = R.string.error_payment_not_processed,
                        errorCode = responseCode,
                        errorMessage = message,
                        errorType = ErrorMessage.PAYMENT_SDK_CODE
                    )
                }

                override fun onPurchaseComplete(purchase: Purchase) {
                    onProductPurchased(purchase.purchaseToken)
                }
            })
    }

    private fun initializeProductPrice() {
        iapAnalytics.initPriceTime()
        courseSku?.let {
            billingProcessor?.querySyncDetails(
                productId = it
            ) { _, skuDetails ->
                val skuDetail = skuDetails?.get(0)
                if (skuDetail?.sku == it) {
                    binding.layoutUpgradeBtn.btnUpgrade.text =
                        ResourceUtil.getFormattedString(
                            resources,
                            R.string.label_upgrade_course_button,
                            AppConstants.PRICE,
                            skuDetail.price
                        ).toString()
                    // The app get the sku details instantly, so add some wait to perform
                    // animation at least one cycle.
                    binding.layoutUpgradeBtn.shimmerViewContainer.postDelayed({
                        binding.layoutUpgradeBtn.shimmerViewContainer.hideShimmer()
                        binding.layoutUpgradeBtn.btnUpgrade.isEnabled = true
                    }, 500)
                    iapAnalytics.setPrice(skuDetail.price)
                    iapAnalytics.trackIAPEvent(Events.IAP_LOAD_PRICE_TIME)
                } else {
                    showUpgradeErrorDialog(
                        errorResId = R.string.error_price_not_fetched,
                        errorType = ErrorMessage.PRICE_CODE,
                        listener = { _, _ ->
                            initializeProductPrice()
                        })
                }
            }
        } ?: showUpgradeErrorDialog(
            errorResId = R.string.error_price_not_fetched,
            errorType = ErrorMessage.PRICE_CODE,
            listener = { _, _ ->
                initializeProductPrice()
            })
    }

    private fun initObserver() {
        iapViewModel.showLoader.observe(viewLifecycleOwner, NonNullObserver {
            enableUpgradeButton(!it)
        })

        iapViewModel.checkoutResponse.observe(viewLifecycleOwner, NonNullObserver {
            if (it.paymentPageUrl.isNotEmpty()) {
                iapAnalytics.initPaymentTime()
                purchaseProduct(iapViewModel.productId)
            }
        })

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
                        errorMsg.errorResId,
                        errorMsg.throwable.httpErrorCode,
                        errorMsg.throwable.errorMessage,
                        errorMsg.errorCode
                    )
                }
            } else {
                showUpgradeErrorDialog(errorMsg.errorResId, errorType = errorMsg.errorCode)
            }
            iapViewModel.errorMessageShown()
        })
    }

    private fun enableUpgradeButton(enable: Boolean) {
        binding.layoutUpgradeBtn.btnUpgrade.setVisibility(enable)
        binding.layoutUpgradeBtn.loadingIndicator.setVisibility(!enable)
    }

    private fun purchaseProduct(productId: String) {
        activity?.let { billingProcessor?.purchaseItem(it, productId) }
    }

    private fun onProductPurchased(purchaseToken: String) {
        lifecycleScope.launch {
            iapAnalytics.trackIAPEvent(eventName = Events.IAP_PAYMENT_TIME)
            iapAnalytics.initUnlockContentTime()
            iapViewModel.setPurchaseToken(purchaseToken)
            iapViewModel.showFullScreenLoader(true)
            dismiss()
        }
    }

    private fun showUpgradeErrorDialog(
        @StringRes errorResId: Int = R.string.general_error_message,
        errorCode: Int? = null,
        errorMessage: String? = null,
        errorType: Int? = null,
        listener: DialogInterface.OnClickListener? = null
    ) {
        // To restrict showing error dialog on an unattached fragment
        if (!isAdded) return
        val feedbackErrorMessage: String = TextUtils.getFormattedErrorMessage(
            errorCode,
            errorType,
            errorMessage
        ).toString()

        when (errorType) {
            ErrorMessage.PAYMENT_SDK_CODE -> iapAnalytics.trackIAPEvent(
                eventName = Events.IAP_PAYMENT_ERROR,
                errorMsg = feedbackErrorMessage
            )
            ErrorMessage.PRICE_CODE -> iapAnalytics.trackIAPEvent(
                eventName = Events.IAP_PRICE_LOAD_ERROR,
                errorMsg = feedbackErrorMessage
            )
            else -> iapAnalytics.trackIAPEvent(
                eventName = Events.IAP_COURSE_UPGRADE_ERROR,
                errorMsg = feedbackErrorMessage
            )
        }

        AlertDialogFragment.newInstance(
            getString(R.string.title_upgrade_error),
            getString(errorResId),
            getString(if (listener != null) R.string.try_again else R.string.label_close),
            { dialogInterface, i ->
                listener?.onClick(dialogInterface, i).also {
                    iapAnalytics.trackIAPEvent(
                        eventName = Events.IAP_ERROR_ALERT_ACTION,
                        errorMsg = feedbackErrorMessage,
                        actionTaken = Values.ACTION_RELOAD_PRICE
                    )
                } ?: run {
                    iapAnalytics.trackIAPEvent(
                        eventName = Events.IAP_ERROR_ALERT_ACTION,
                        errorMsg = feedbackErrorMessage,
                        actionTaken = Values.ACTION_CLOSE
                    )
                }
            },
            getString(if (listener != null) R.string.label_cancel else R.string.label_get_help),
            { _, _ ->
                listener?.also {
                    iapAnalytics.trackIAPEvent(
                        eventName = Events.IAP_ERROR_ALERT_ACTION,
                        errorMsg = feedbackErrorMessage,
                        actionTaken = Values.ACTION_CLOSE
                    )
                    dismiss()
                } ?: run {
                    environment.router?.showFeedbackScreen(
                        requireActivity(),
                        getString(R.string.email_subject_upgrade_error),
                        feedbackErrorMessage
                    )
                    iapAnalytics.trackIAPEvent(
                        eventName = Events.IAP_ERROR_ALERT_ACTION,
                        errorMsg = feedbackErrorMessage,
                        actionTaken = Values.ACTION_GET_HELP
                    )
                }
            }, false
        ).show(childFragmentManager, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        billingProcessor?.disconnect()
    }

    companion object {
        const val TAG: String = "CourseModalDialogFragment"
        const val KEY_SCREEN_NAME = "screen_name"
        const val KEY_COURSE_ID = "course_id"
        const val KEY_COURSE_PRODUCT = "course_product"
        const val KEY_COURSE_NAME = "course_name"
        const val KEY_IS_SELF_PACED = "is_Self_Paced"

        @JvmStatic
        fun newInstance(
            screenName: String,
            courseId: String,
            courseSku: String?,
            courseName: String,
            isSelfPaced: Boolean
        ): CourseModalDialogFragment {
            val frag = CourseModalDialogFragment()
            val args = Bundle().apply {
                putString(KEY_SCREEN_NAME, screenName)
                putString(KEY_COURSE_ID, courseId)
                putString(KEY_COURSE_PRODUCT, courseSku)
                putString(KEY_COURSE_NAME, courseName)
                putBoolean(KEY_IS_SELF_PACED, isSelfPaced)
            }
            frag.arguments = args
            return frag
        }
    }
}
