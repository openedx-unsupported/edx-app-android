package org.edx.mobile.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.SkuDetails
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.edx.mobile.R
import org.edx.mobile.databinding.FragmentCourseUnitGradeBinding
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.extenstion.isNotVisible
import org.edx.mobile.extenstion.setImageDrawable
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.model.api.AuthorizationDenialReason
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.module.analytics.Analytics
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
import org.edx.mobile.wrapper.InAppPurchasesDialog
import javax.inject.Inject

@AndroidEntryPoint
class CourseUnitMobileNotSupportedFragment : CourseUnitFragment() {

    private lateinit var binding: FragmentCourseUnitGradeBinding
    private val iapViewModel: InAppPurchasesViewModel
            by viewModels(ownerProducer = { requireActivity() })

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
        unit?.let { unit ->
            val isSelfPaced = getBooleanArgument(Router.EXTRA_IS_SELF_PACED, false)
            binding.containerLayoutNotAvailable.setVisibility(false)
            binding.llGradedContentLayout.setVisibility(true)
            var experimentGroup: String? = null
            if (environment.appFeaturesPrefs.isIAPExperimentEnabled()) {
                experimentGroup = if (environment.loginPrefs.isOddUserId) {
                    Analytics.Values.TREATMENT
                } else {
                    Analytics.Values.CONTROL
                }
            }
            environment.analyticsRegistry.trackValuePropMessageViewed(
                unit.courseId,
                Screens.COURSE_UNIT,
                (unit.courseSku.isNullOrEmpty()
                    .not() && environment.appFeaturesPrefs.isIAPEnabled()),
                experimentGroup,
                unit.id
            )
            if (environment.appFeaturesPrefs.isIAPEnabled(environment.loginPrefs.isOddUserId)) {
                iapAnalytics.initCourseValues(
                    courseId = unit.courseId,
                    isSelfPaced = isSelfPaced,
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

    private fun initIAPObserver() {
        iapViewModel.productPrice.observe(viewLifecycleOwner, NonNullObserver { skuDetails ->
            setUpUpgradeButton(skuDetails)
        })
        iapViewModel.showLoader.observe(viewLifecycleOwner, NonNullObserver {
            enableUpgradeButton(!it)
        })

        iapViewModel.refreshCourseData.observe(viewLifecycleOwner) { refreshCourse: Boolean ->
            if (refreshCourse) {
                iapViewModel.refreshCourseData(false)
                unit?.let { updateCourseUnit(it.courseId, it.id) }
            }
        }

        iapViewModel.errorMessage.observe(viewLifecycleOwner, NonNullObserver { errorMessage ->
            // Error message observer should not observe EXECUTE or REFRESH error cases as they
            // will be observed by FullscreenLoaderDialogFragment's observer.
            if (InAppPurchasesUtils.postPurchasedRequests.contains(errorMessage.requestType)) {
                return@NonNullObserver
            }
            handleIAPException(errorMessage)
            iapViewModel.errorMessageShown()
        })

        iapViewModel.productPurchased.observe(viewLifecycleOwner, NonNullObserver {
            lifecycleScope.launch {
                initializeBaseObserver()
                iapViewModel.showFullScreenLoader(true)
            }
        })
    }

    private fun handleIAPException(errorMessage: ErrorMessage) {
        var retryListener: DialogInterface.OnClickListener? = null
        if (HttpStatus.NOT_ACCEPTABLE == (errorMessage.throwable as InAppPurchasesException).httpErrorCode) {
            retryListener = DialogInterface.OnClickListener { _, _ ->
                iapViewModel.upgradeMode =
                    InAppPurchasesViewModel.UpgradeMode.SILENT
                iapViewModel.showFullScreenLoader(true)
            }
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

    private fun setUpUpgradeButton(skuDetail: SkuDetails) {
        price = skuDetail.price
        binding.layoutUpgradeBtn.root.setVisibility(true)

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

        binding.layoutUpgradeBtn.btnUpgrade.setOnClickListener {
            iapAnalytics.trackIAPEvent(Events.IAP_UPGRADE_NOW_CLICKED)
            unit?.courseSku?.let { productId ->
                iapViewModel.addProductToBasket(
                    requireActivity(),
                    environment.loginPrefs.userId,
                    productId
                )
            } ?: iapDialog.showUpgradeErrorDialog(this)
        }
    }

    private fun enableUpgradeButton(enable: Boolean) {
        binding.layoutUpgradeBtn.btnUpgrade.setVisibility(enable)
        binding.layoutUpgradeBtn.loadingIndicator.setVisibility(!enable)
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
