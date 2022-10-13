package org.edx.mobile.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.edx.mobile.R
import org.edx.mobile.databinding.FragmentCourseUnitGradeBinding
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.extenstion.isNotVisible
import org.edx.mobile.extenstion.setImageDrawable
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.inapppurchases.BillingProcessor
import org.edx.mobile.inapppurchases.BillingProcessor.BillingFlowListeners
import org.edx.mobile.model.api.AuthorizationDenialReason
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.module.analytics.Analytics.Events
import org.edx.mobile.module.analytics.Analytics.Screens
import org.edx.mobile.module.analytics.InAppPurchasesAnalytics
import org.edx.mobile.util.AppConstants
import org.edx.mobile.util.BrowserUtil
import org.edx.mobile.util.InAppPurchasesException
import org.edx.mobile.util.InAppPurchasesUtils
import org.edx.mobile.util.NonNullObserver
import org.edx.mobile.util.ResourceUtil
import org.edx.mobile.viewModel.InAppPurchasesViewModel
import javax.inject.Inject

@AndroidEntryPoint
class CourseUnitMobileNotSupportedFragment : CourseUnitFragment() {

    private lateinit var binding: FragmentCourseUnitGradeBinding
    private var billingProcessor: BillingProcessor? = null
    private var price: String = ""

    private val iapViewModel: InAppPurchasesViewModel
            by viewModels(ownerProducer = { requireActivity() })

    @Inject
    lateinit var iapAnalytics: InAppPurchasesAnalytics

    @Inject
    lateinit var iapUtils: InAppPurchasesUtils

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCourseUnitGradeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (AuthorizationDenialReason.FEATURE_BASED_ENROLLMENTS == unit?.authorizationDenialReason) {
            if (environment.appFeaturesPrefs.isValuePropEnabled()) {
                showGradedContent()
            } else {
                showNotAvailableOnMobile(isLockedContent = true)
            }
        } else {
            showNotAvailableOnMobile(isLockedContent = false)
        }
    }

    private fun showGradedContent() {
        val isSelfPaced = getBooleanArgument(Router.EXTRA_IS_SELF_PACED, false)
        binding.containerLayoutNotAvailable.setVisibility(false)
        binding.llGradedContentLayout.setVisibility(true)
        setUpUpgradeButton(isSelfPaced)

        binding.toggleShow.setOnClickListener {
            val showMore = binding.layoutUpgradeFeature.containerLayout.isNotVisible()
            binding.layoutUpgradeFeature.containerLayout.setVisibility(showMore)
            binding.toggleShow.text = getText(
                if (showMore) R.string.course_modal_graded_assignment_show_less
                else R.string.course_modal_graded_assignment_show_more
            )
            unit?.let {
                environment.analyticsRegistry.trackValuePropShowMoreLessClicked(
                    it.courseId, it.id, price, isSelfPaced, showMore
                )
            }
        }
    }

    private fun showNotAvailableOnMobile(isLockedContent: Boolean) {
        binding.containerLayoutNotAvailable.setVisibility(true)
        binding.llGradedContentLayout.setVisibility(false)
        binding.notAvailableMessage2.setVisibility(!isLockedContent)

        if (isLockedContent) {
            binding.contentErrorIcon.setImageDrawable(R.drawable.ic_lock)
            binding.notAvailableMessage.setText(R.string.not_available_on_mobile)
        } else {
            binding.contentErrorIcon.setImageDrawable(R.drawable.ic_laptop)
            binding.notAvailableMessage.setText(
                if (unit?.isVideoBlock == true) R.string.video_only_on_web_short
                else R.string.assessment_not_available
            )
        }

        binding.viewOnWebButton.setOnClickListener {
            unit?.let {
                BrowserUtil.open(activity, it.webUrl, true)
                environment.analyticsRegistry.trackOpenInBrowser(
                    it.id, it.courseId, it.isMultiDevice, it.blockId
                )
            }
        }
    }

    private fun setUpUpgradeButton(isSelfPaced: Boolean) {
        if (environment.appFeaturesPrefs.isIAPEnabled(environment.loginPrefs.isOddUserId)) {
            unit?.let {
                iapAnalytics.initCourseValues(
                    courseId = it.courseId,
                    isSelfPaced = isSelfPaced,
                    screenName = Screens.COURSE_COMPONENT,
                    componentId = it.id
                )
            }
            initObserver()
            binding.layoutUpgradeBtn.root.setVisibility(true)
            binding.layoutUpgradeBtn.btnUpgrade.setOnClickListener {
                iapAnalytics.trackIAPEvent(Events.IAP_UPGRADE_NOW_CLICKED)
                iapUtils.showSDNDialog(this) { _, _ ->
                    unit?.courseSku?.let { productId ->
                        iapViewModel.addProductToBasket(productId)
                    } ?: iapUtils.showUpgradeErrorDialog(this)
                }
            }

            billingProcessor = BillingProcessor(requireContext(), object : BillingFlowListeners {

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    super.onBillingSetupFinished(billingResult)
                    // Shimmer container taking sometime to get ready and perform the animation, so
                    // by adding the some delay fixed that issue for lower-end devices, and for the
                    // proper animation.
                    binding.layoutUpgradeBtn.shimmerViewContainer.postDelayed({
                        unit?.let { initializeProductPrice() }
                    }, 1500)
                    binding.layoutUpgradeBtn.btnUpgrade.isEnabled = false
                }

                override fun onPurchaseCancel(responseCode: Int, message: String) {
                    iapViewModel.endLoading()
                    iapUtils.showUpgradeErrorDialog(
                        context = this@CourseUnitMobileNotSupportedFragment,
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
        } else {
            binding.layoutUpgradeBtn.root.setVisibility(false)
        }
    }

    private fun initializeProductPrice() {
        iapAnalytics.initPriceTime()
        unit?.courseSku?.let { productId ->
            billingProcessor?.querySyncDetails(
                productId = productId
            ) { _, skuDetails ->
                val skuDetail = skuDetails?.get(0)
                if (skuDetail?.sku == productId) {
                    binding.layoutUpgradeBtn.btnUpgrade.text =
                        ResourceUtil.getFormattedString(
                            resources,
                            R.string.label_upgrade_course_button,
                            AppConstants.PRICE,
                            skuDetail.price
                        ).toString()
                    price = skuDetail.price
                    // The app get the sku details instantly, so add some wait to perform
                    // animation at least one cycle.
                    binding.layoutUpgradeBtn.shimmerViewContainer.postDelayed({
                        binding.layoutUpgradeBtn.shimmerViewContainer.hideShimmer()
                        binding.layoutUpgradeBtn.btnUpgrade.isEnabled = true
                    }, 500)
                    iapAnalytics.setPrice(price)
                    iapAnalytics.trackIAPEvent(Events.IAP_LOAD_PRICE_TIME)
                } else {
                    iapUtils.showUpgradeErrorDialog(
                        context = this,
                        errorResId = R.string.error_price_not_fetched,
                        errorType = ErrorMessage.PRICE_CODE,
                        listener = { _, _ ->
                            unit?.let { initializeProductPrice() }
                        })
                }
            }
        } ?: iapUtils.showUpgradeErrorDialog(
            context = this,
            errorResId = R.string.error_price_not_fetched,
            errorType = ErrorMessage.PRICE_CODE,
            listener = { _, _ ->
                unit?.let { initializeProductPrice() }
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

        iapViewModel.refreshCourseData.observe(viewLifecycleOwner) { refreshCourse: Boolean ->
            if (refreshCourse) {
                iapViewModel.refreshCourseData(false)
                unit?.let { updateCourseUnit(it.courseId, it.id) }
            }
        }

        iapViewModel.errorMessage.observe(viewLifecycleOwner, NonNullObserver { errorMsg ->
            // Error message observer should not observe EXECUTE or REFRESH error cases as they
            // will be observed by FullscreenLoaderDialogFragment's observer.
            if (listOf(ErrorMessage.EXECUTE_ORDER_CODE, ErrorMessage.COURSE_REFRESH_CODE)
                    .contains(errorMsg.errorCode)
            ) return@NonNullObserver

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
                            context = this,
                            errorResId = errorMsg.errorResId,
                            errorCode = errorMsg.throwable.httpErrorCode,
                            errorMessage = errorMsg.throwable.errorMessage,
                            errorType = errorMsg.errorCode,
                            retryListener = { _, _ ->
                                iapViewModel.upgradeMode =
                                    InAppPurchasesViewModel.UpgradeMode.SILENT
                                iapViewModel.showFullScreenLoader(true)
                            },
                            cancelListener = null
                        )
                    }
                    else -> iapUtils.showUpgradeErrorDialog(
                        context = this,
                        errorResId = errorMsg.errorResId,
                        errorCode = errorMsg.throwable.httpErrorCode,
                        errorMessage = errorMsg.throwable.errorMessage,
                        errorType = errorMsg.errorCode
                    )
                }
            } else {
                iapUtils.showUpgradeErrorDialog(
                    context = this, errorResId = errorMsg.errorResId, errorType = errorMsg.errorCode
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
            iapAnalytics.trackIAPEvent(Events.IAP_PAYMENT_TIME)
            iapAnalytics.initUnlockContentTime()
            initializeBaseObserver()
            iapViewModel.setPurchaseToken(purchaseToken)
            iapViewModel.showFullScreenLoader(true)
        }
    }

    override fun onResume() {
        super.onResume()
        unit?.let {
            if (it.authorizationDenialReason == AuthorizationDenialReason.FEATURE_BASED_ENROLLMENTS
                && environment.appFeaturesPrefs.isValuePropEnabled()
            ) {
                environment.analyticsRegistry.trackLockedContentTapped(it.courseId, it.blockId)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        billingProcessor?.disconnect()
    }

    companion object {
        @JvmStatic
        fun newInstance(
            unit: CourseComponent,
            isSelfPaced: Boolean,
        ): CourseUnitMobileNotSupportedFragment {
            val fragment = CourseUnitMobileNotSupportedFragment()
            val args = Bundle()
            args.putSerializable(Router.EXTRA_COURSE_UNIT, unit)
            args.putBoolean(Router.EXTRA_IS_SELF_PACED, isSelfPaced)
            fragment.arguments = args
            return fragment
        }
    }
}
