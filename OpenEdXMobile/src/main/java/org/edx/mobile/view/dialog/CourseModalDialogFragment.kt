package org.edx.mobile.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import org.edx.mobile.module.analytics.InAppPurchasesAnalytics
import org.edx.mobile.util.AppConstants
import org.edx.mobile.util.InAppPurchasesException
import org.edx.mobile.util.InAppPurchasesUtils
import org.edx.mobile.util.NonNullObserver
import org.edx.mobile.util.ResourceUtil
import org.edx.mobile.viewModel.InAppPurchasesViewModel
import javax.inject.Inject

@AndroidEntryPoint
class CourseModalDialogFragment : DialogFragment() {

    private lateinit var binding: DialogUpgradeFeaturesBinding
    private var screenName: String = ""
    private var courseId: String = ""
    private var courseSku: String? = null
    private var isSelfPaced: Boolean = false
    private var isPurchaseEnabled: Boolean = false

    private var billingProcessor: BillingProcessor? = null

    private val iapViewModel: InAppPurchasesViewModel
            by viewModels(ownerProducer = { requireActivity() })

    @Inject
    lateinit var environment: IEdxEnvironment

    @Inject
    lateinit var iapAnalytics: InAppPurchasesAnalytics

    @Inject
    lateinit var iapUtils: InAppPurchasesUtils

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
        isPurchaseEnabled =
            environment.appFeaturesPrefs.isIAPEnabled(environment.loginPrefs.isOddUserId)
        initViews()
        if (isPurchaseEnabled) {
            initBillingProcessor()
        }
    }

    private fun initViews() {
        arguments?.let { bundle ->
            screenName = bundle.getString(KEY_SCREEN_NAME, "")
            courseId = bundle.getString(KEY_COURSE_ID, "")
            courseSku = bundle.getString(KEY_COURSE_SKU)
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
        binding.layoutUpgradeBtn.root.setVisibility(isPurchaseEnabled)
        binding.dialogDismiss.setOnClickListener {
            dialog?.dismiss()
        }
    }

    private fun initBillingProcessor() {
        initObserver()

        binding.layoutUpgradeBtn.btnUpgrade.setOnClickListener {
            iapAnalytics.trackIAPEvent(eventName = Events.IAP_UPGRADE_NOW_CLICKED)
            iapUtils.showSDNDialog(this) { _, _ ->
                courseSku?.let {
                    iapViewModel.addProductToBasket(it)
                } ?: iapUtils.showUpgradeErrorDialog(this)
            }
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
                    iapUtils.showUpgradeErrorDialog(
                        context = this@CourseModalDialogFragment,
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
                    iapUtils.showUpgradeErrorDialog(
                        context = this@CourseModalDialogFragment,
                        errorResId = R.string.error_price_not_fetched,
                        errorType = ErrorMessage.PRICE_CODE,
                        listener = { _, _ ->
                            initializeProductPrice()
                        })
                }
            }
        } ?: iapUtils.showUpgradeErrorDialog(
            context = this@CourseModalDialogFragment,
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
                    HttpStatus.NOT_ACCEPTABLE -> {
                        iapUtils.showPostUpgradeErrorDialog(
                            context = this@CourseModalDialogFragment,
                            errorResId = errorMsg.errorResId,
                            errorCode = errorMsg.throwable.httpErrorCode,
                            errorMessage = errorMsg.throwable.errorMessage,
                            errorType = errorMsg.errorCode,
                            retryListener = { _, _ ->
                                iapViewModel.upgradeMode =
                                    InAppPurchasesViewModel.UpgradeMode.SILENT
                                iapViewModel.showFullScreenLoader(true)
                                dismiss()
                            },
                            cancelListener = null
                        )
                    }
                    else -> iapUtils.showUpgradeErrorDialog(
                        context = this@CourseModalDialogFragment,
                        errorResId = errorMsg.errorResId,
                        errorCode = errorMsg.throwable.httpErrorCode,
                        errorMessage = errorMsg.throwable.errorMessage,
                        errorType = errorMsg.errorCode
                    )
                }
            } else {
                iapUtils.showUpgradeErrorDialog(
                    context = this@CourseModalDialogFragment,
                    errorResId = errorMsg.errorResId,
                    errorType = errorMsg.errorCode
                )
            }
            iapViewModel.errorMessageShown()
        })
    }

    private fun enableUpgradeButton(enable: Boolean) {
        binding.layoutUpgradeBtn.btnUpgrade.setVisibility(enable)
        binding.layoutUpgradeBtn.loadingIndicator.setVisibility(!enable)
    }

    private fun purchaseProduct(productId: String) {
        activity?.let { context ->
            environment.loginPrefs.userId?.let { userId ->
                billingProcessor?.purchaseItem(context, productId, userId)
            }
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        billingProcessor?.disconnect()
    }

    companion object {
        const val TAG: String = "CourseModalDialogFragment"
        const val KEY_SCREEN_NAME = "screen_name"
        const val KEY_COURSE_ID = "course_id"
        const val KEY_COURSE_SKU = "course_sku"
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
                putString(KEY_COURSE_SKU, courseSku)
                putString(KEY_COURSE_NAME, courseName)
                putBoolean(KEY_IS_SELF_PACED, isSelfPaced)
            }
            frag.arguments = args
            return frag
        }
    }
}
