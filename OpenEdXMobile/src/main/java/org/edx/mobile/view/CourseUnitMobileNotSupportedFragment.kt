package org.edx.mobile.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.R
import org.edx.mobile.databinding.FragmentCourseUnitGradeBinding
import org.edx.mobile.event.IAPFlowEvent
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.extenstion.isNotVisible
import org.edx.mobile.extenstion.setImageDrawable
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.inapppurchases.getPriceAmount
import org.edx.mobile.model.api.AuthorizationDenialReason
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.model.iap.IAPFlowData
import org.edx.mobile.model.iap.IAPFlowData.IAPAction
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.module.analytics.Analytics.Events
import org.edx.mobile.module.analytics.Analytics.Screens
import org.edx.mobile.module.analytics.InAppPurchasesAnalytics
import org.edx.mobile.util.AppConstants
import org.edx.mobile.util.BrowserUtil
import org.edx.mobile.util.InAppPurchasesException
import org.edx.mobile.util.ResourceUtil
import org.edx.mobile.util.observer.EventObserver
import org.edx.mobile.viewModel.InAppPurchasesViewModel
import org.edx.mobile.wrapper.InAppPurchasesDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

@AndroidEntryPoint
class CourseUnitMobileNotSupportedFragment : CourseUnitFragment() {

    private lateinit var binding: FragmentCourseUnitGradeBinding

    private val iapViewModel: InAppPurchasesViewModel by viewModels()

    @Inject
    lateinit var iapAnalytics: InAppPurchasesAnalytics

    @Inject
    lateinit var iapDialog: InAppPurchasesDialog

    private var price: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCourseUnitGradeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isUpgradeable = getBooleanArgument(Router.EXTRA_IS_UPGRADEABLE, true)
        if (AuthorizationDenialReason.FEATURE_BASED_ENROLLMENTS == unit?.authorizationDenialReason) {
            if (environment.featuresPrefs.isValuePropEnabled && isUpgradeable) {
                showGradedContent()
            } else {
                showNotAvailableOnMobile(isLockedContent = true)
            }
        } else {
            showNotAvailableOnMobile(isLockedContent = false)
        }
    }

    private fun showGradedContent() {
        unit?.let { unit ->
            val isSelfPaced = getBooleanArgument(Router.EXTRA_IS_SELF_PACED, false)
            val isPurchaseEnabled = unit.courseSku.isNullOrEmpty().not() &&
                    environment.featuresPrefs.isIAPEnabledForUser(environment.loginPrefs.isOddUserId)

            binding.containerLayoutNotAvailable.root.setVisibility(false)
            binding.llGradedContentLayout.setVisibility(true)
            var experimentGroup: String? = null
            if (environment.featuresPrefs.isIAPExperimentEnabled) {
                experimentGroup = if (environment.loginPrefs.isOddUserId) {
                    Analytics.Values.TREATMENT
                } else {
                    Analytics.Values.CONTROL
                }
            }
            environment.analyticsRegistry.trackLockedContentTapped(unit.courseId, unit.blockId)
            environment.analyticsRegistry.trackValuePropMessageViewed(
                unit.courseId,
                Screens.COURSE_UNIT,
                (unit.courseSku.isNullOrEmpty().not() && environment.featuresPrefs.isIAPEnabled),
                experimentGroup,
                unit.id
            )
            if (isPurchaseEnabled) {
                iapAnalytics.initCourseValues(
                    courseId = unit.courseId,
                    isSelfPaced = isSelfPaced,
                    flowType = IAPFlowData.IAPFlowType.USER_INITIATED.value(),
                    screenName = Screens.COURSE_COMPONENT,
                    componentId = unit.id
                )
                initIAPObserver()
                // Shimmer container taking sometime to get ready and perform the animation, so
                // by adding the some delay fixed that issue for lower-end devices, and for the
                // proper animation.
                binding.layoutUpgradeBtn.shimmerViewContainer.postDelayed({
                    iapViewModel.initializeProductPrice(unit.courseSku)
                }, 1500)
                binding.layoutUpgradeBtn.btnUpgrade.isEnabled = false
            } else {
                binding.layoutUpgradeBtn.root.setVisibility(false)
            }

            binding.toggleShow.setOnClickListener {
                val showMore = binding.layoutUpgradeFeature.containerLayout.isNotVisible()
                binding.layoutUpgradeFeature.containerLayout.setVisibility(showMore)
                binding.toggleShow.text = getText(
                    if (showMore) {
                        R.string.course_modal_graded_assignment_show_less
                    } else {
                        R.string.course_modal_graded_assignment_show_more
                    }
                )
                environment.analyticsRegistry.trackValuePropShowMoreLessClicked(
                    unit.courseId, unit.id, price, isSelfPaced, showMore
                )
            }
        }
    }

    private fun showNotAvailableOnMobile(isLockedContent: Boolean) {
        binding.llGradedContentLayout.setVisibility(false)
        binding.containerLayoutNotAvailable.apply {
            root.setVisibility(true)
            notAvailableMessage2.setVisibility(!isLockedContent)

            if (isLockedContent) {
                contentErrorIcon.setImageDrawable(R.drawable.ic_lock)
                notAvailableMessage.setText(R.string.not_available_on_mobile)
            } else {
                contentErrorIcon.setImageDrawable(R.drawable.ic_laptop)
                notAvailableMessage.setText(
                    if (unit?.isVideoBlock == true) R.string.video_only_on_web_short
                    else R.string.assessment_not_available
                )
            }

            viewOnWebButton.setOnClickListener {
                unit?.let {
                    BrowserUtil.open(activity, it.webUrl, true)
                    environment.analyticsRegistry.trackOpenInBrowser(
                        it.id, it.courseId, it.isMultiDevice, it.blockId
                    )
                }
            }
        }
    }

    private fun initIAPObserver() {
        initializeBaseObserver()

        iapViewModel.productPrice.observe(viewLifecycleOwner, EventObserver {
            setUpUpgradeButton(it)
        })

        iapViewModel.launchPurchaseFlow.observe(viewLifecycleOwner, EventObserver {
            iapViewModel.purchaseItem(
                requireActivity(),
                environment.loginPrefs.userId,
                unit?.courseSku
            )
        })

        iapViewModel.productPurchased.observe(viewLifecycleOwner, EventObserver {
            EventBus.getDefault().post(IAPFlowEvent(IAPAction.SHOW_FULL_SCREEN_LOADER, it))
        })

        iapViewModel.showLoader.observe(viewLifecycleOwner, EventObserver {
            enableUpgradeButton(!it)
        })

        iapViewModel.errorMessage.observe(viewLifecycleOwner, EventObserver { errorMessage ->
            handleIAPException(errorMessage)
        })
    }

    private fun handleIAPException(errorMessage: ErrorMessage) {
        var retryListener: DialogInterface.OnClickListener? = null
        if (HttpStatus.NOT_ACCEPTABLE == (errorMessage.throwable as InAppPurchasesException).httpErrorCode) {
            retryListener = DialogInterface.OnClickListener { _, _ ->
                EventBus.getDefault().post(
                    IAPFlowEvent(IAPAction.SHOW_FULL_SCREEN_LOADER, iapViewModel.iapFlowData)
                )
            }
        } else if (BillingClient.BillingResponseCode.USER_CANCELED == errorMessage.getHttpErrorCode()) {
            iapAnalytics.trackIAPEvent(eventName = Events.IAP_PAYMENT_CANCELED)
            return
        } else if (errorMessage.canRetry()) {
            retryListener = DialogInterface.OnClickListener { _, _ ->
                when (errorMessage.requestType) {
                    ErrorMessage.PRICE_CODE -> {
                        iapViewModel.initializeProductPrice(unit?.courseSku)
                    }
                }
            }
        }
        iapDialog.handleIAPException(
            fragment = this@CourseUnitMobileNotSupportedFragment,
            errorMessage = errorMessage,
            retryListener = retryListener
        )
    }

    private fun setUpUpgradeButton(productDetails: ProductDetails.OneTimePurchaseOfferDetails) {
        price = productDetails.formattedPrice
        binding.layoutUpgradeBtn.root.setVisibility(true)

        binding.layoutUpgradeBtn.btnUpgrade.text =
            ResourceUtil.getFormattedString(
                resources,
                R.string.label_upgrade_course_button,
                AppConstants.PRICE,
                price
            ).toString()
        // The app get the product details instantly, so add some wait to perform
        // animation at least one cycle.
        binding.layoutUpgradeBtn.shimmerViewContainer.postDelayed({
            binding.layoutUpgradeBtn.shimmerViewContainer.hideShimmer()
            binding.layoutUpgradeBtn.btnUpgrade.isEnabled = true
        }, 500)

        binding.layoutUpgradeBtn.btnUpgrade.setOnClickListener {
            iapAnalytics.trackIAPEvent(Events.IAP_UPGRADE_NOW_CLICKED)
            unit?.courseSku?.let { productId ->
                iapViewModel.startPurchaseFlow(
                    productId,
                    productDetails.getPriceAmount(),
                    productDetails.priceCurrencyCode,
                )
            } ?: iapDialog.showPreUpgradeErrorDialog(this)
        }
    }

    private fun enableUpgradeButton(enable: Boolean) {
        binding.layoutUpgradeBtn.btnUpgrade.setVisibility(enable)
        binding.layoutUpgradeBtn.loadingIndicator.setVisibility(!enable)
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        EventBus.getDefault().unregister(this)
        super.onPause()
    }

    @Subscribe
    fun onEventMainThread(event: IAPFlowEvent) {
        if (isResumed && event.flowAction == IAPAction.PURCHASE_FLOW_COMPLETE) {
            unit?.let { updateCourseUnit(it.courseId, it.id) }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(
            unit: CourseComponent,
            courseData: EnrolledCoursesResponse,
        ): CourseUnitMobileNotSupportedFragment {
            val fragment = CourseUnitMobileNotSupportedFragment()
            val args = Bundle()
            args.putSerializable(Router.EXTRA_COURSE_UNIT, unit)
            args.putBoolean(Router.EXTRA_IS_SELF_PACED, courseData.course.isSelfPaced)
            args.putBoolean(Router.EXTRA_IS_UPGRADEABLE, courseData.isUpgradeable)
            fragment.arguments = args
            return fragment
        }
    }
}
