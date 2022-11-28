package org.edx.mobile.view.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.SkuDetails
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.edx.mobile.R
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.databinding.DialogUpgradeFeaturesBinding
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.module.analytics.Analytics.Events
import org.edx.mobile.module.analytics.InAppPurchasesAnalytics
import org.edx.mobile.util.AppConstants
import org.edx.mobile.util.InAppPurchasesException
import org.edx.mobile.util.NonNullObserver
import org.edx.mobile.util.ResourceUtil
import org.edx.mobile.viewModel.InAppPurchasesViewModel
import org.edx.mobile.wrapper.InAppPurchasesDialog
import javax.inject.Inject

@AndroidEntryPoint
class CourseModalDialogFragment : DialogFragment() {

    private lateinit var binding: DialogUpgradeFeaturesBinding

    private val iapViewModel: InAppPurchasesViewModel
            by viewModels(ownerProducer = { requireActivity() })

    @Inject
    lateinit var environment: IEdxEnvironment

    @Inject
    lateinit var iapAnalytics: InAppPurchasesAnalytics

    @Inject
    lateinit var iapDialog: InAppPurchasesDialog

    private var screenName: String = ""
    private var courseId: String = ""
    private var courseSku: String? = null
    private var isSelfPaced: Boolean = false

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
    }

    private fun initViews() {
        arguments?.let { bundle ->
            screenName = bundle.getString(KEY_SCREEN_NAME, "")
            courseId = bundle.getString(KEY_COURSE_ID, "")
            courseSku = bundle.getString(KEY_COURSE_SKU)
            isSelfPaced = bundle.getBoolean(KEY_IS_SELF_PACED)
            trackEvents()
        }

        binding.dialogTitle.text = ResourceUtil.getFormattedString(
            resources,
            R.string.course_modal_heading,
            KEY_COURSE_NAME,
            arguments?.getString(KEY_COURSE_NAME)
        )
        val isPurchaseEnabled =
            environment.appFeaturesPrefs.isIAPEnabled(environment.loginPrefs.isOddUserId)
        binding.layoutUpgradeBtn.root.setVisibility(isPurchaseEnabled)
        binding.dialogDismiss.setOnClickListener {
            dialog?.dismiss()
        }
        if (isPurchaseEnabled) {
            initIAPObservers()
            // Shimmer container taking sometime to get ready and perform the animation, so
            // by adding the some delay fixed that issue for lower-end devices, and for the
            // proper animation.
            binding.layoutUpgradeBtn.shimmerViewContainer.postDelayed({
                iapViewModel.initializeProductPrice(courseSku)
            }, 1500)
            binding.layoutUpgradeBtn.btnUpgrade.isEnabled = false
        }
    }

    private fun trackEvents() {
        iapAnalytics.initCourseValues(
            courseId = courseId,
            isSelfPaced = isSelfPaced,
            screenName = screenName
        )
        environment.analyticsRegistry.trackValuePropLearnMoreTapped(courseId, screenName)
        environment.analyticsRegistry.trackScreenView(
            Events.VALUE_PROP_MODAL_VIEW,
            courseId,
            null,
            mapOf(Pair(KEY_SCREEN_NAME, screenName))
        )
        var experimentGroup: String? = null
        if (environment.appFeaturesPrefs.isIAPExperimentEnabled()) {
            experimentGroup =
                if (environment.loginPrefs.isOddUserId) Analytics.Values.TREATMENT else Analytics.Values.CONTROL
        }
        environment.analyticsRegistry.trackValuePropMessageViewed(
            courseId,
            screenName,
            (courseSku.isNullOrEmpty().not() && environment.appFeaturesPrefs.isIAPEnabled()),
            experimentGroup,
            null
        )
    }

    private fun initIAPObservers() {
        iapViewModel.productPrice.observe(viewLifecycleOwner, NonNullObserver { skuDetails ->
            setUpUpgradeButton(skuDetails)
        })

        iapViewModel.showLoader.observe(viewLifecycleOwner, NonNullObserver {
            enableUpgradeButton(!it)
        })

        iapViewModel.errorMessage.observe(viewLifecycleOwner, NonNullObserver { errorMessage ->
            handleIAPException(errorMessage)
            iapViewModel.errorMessageShown()
        })

        iapViewModel.productPurchased.observe(viewLifecycleOwner, NonNullObserver {
            lifecycleScope.launch {
                iapViewModel.showFullScreenLoader(true)
                dismiss()
            }
        })
    }

    private fun setUpUpgradeButton(skuDetails: SkuDetails) {
        binding.layoutUpgradeBtn.btnUpgrade.text =
            ResourceUtil.getFormattedString(
                resources,
                R.string.label_upgrade_course_button,
                AppConstants.PRICE,
                skuDetails.price
            ).toString()
        // The app get the sku details instantly, so add some wait to perform
        // animation at least one cycle.
        binding.layoutUpgradeBtn.shimmerViewContainer.postDelayed({
            binding.layoutUpgradeBtn.shimmerViewContainer.hideShimmer()
            binding.layoutUpgradeBtn.btnUpgrade.isEnabled = true
        }, 500)

        binding.layoutUpgradeBtn.btnUpgrade.setOnClickListener {
            iapAnalytics.trackIAPEvent(eventName = Events.IAP_UPGRADE_NOW_CLICKED)
            courseSku?.let {
                iapViewModel.addProductToBasket(
                    activity = requireActivity(),
                    userId = environment.loginPrefs.userId,
                    productId = it
                )
            } ?: iapDialog.showUpgradeErrorDialog(this)
        }
    }

    private fun handleIAPException(errorMessage: ErrorMessage) {
        var retryListener: DialogInterface.OnClickListener? = null
        if (HttpStatus.NOT_ACCEPTABLE == (errorMessage.throwable as InAppPurchasesException).httpErrorCode) {
            retryListener = DialogInterface.OnClickListener { _, _ ->
                iapViewModel.upgradeMode = InAppPurchasesViewModel.UpgradeMode.SILENT
                iapViewModel.showFullScreenLoader(true)
                dismiss()
            }
        } else if (errorMessage.canRetry()) {
            retryListener = DialogInterface.OnClickListener { _, _ ->
                when (errorMessage.requestType) {
                    ErrorMessage.PRICE_CODE -> {
                        iapViewModel.initializeProductPrice(courseSku)
                    }
                }
            }
        }
        iapDialog.handleIAPException(
            fragment =
            this@CourseModalDialogFragment,
            errorMessage = errorMessage,
            retryListener = retryListener
        )
    }

    private fun enableUpgradeButton(enable: Boolean) {
        binding.layoutUpgradeBtn.btnUpgrade.setVisibility(enable)
        binding.layoutUpgradeBtn.loadingIndicator.setVisibility(!enable)
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
