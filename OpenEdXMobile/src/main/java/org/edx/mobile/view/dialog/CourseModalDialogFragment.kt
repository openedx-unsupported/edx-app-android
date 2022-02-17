package org.edx.mobile.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.launch
import org.edx.mobile.R
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.databinding.DialogUpgradeFeaturesBinding
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.inapppurchases.BillingProcessor
import org.edx.mobile.inapppurchases.ProductManager
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.util.NonNullObserver
import org.edx.mobile.util.ResourceUtil
import org.edx.mobile.viewModel.InAppPurchasesViewModel
import org.edx.mobile.viewModel.ViewModelFactory
import roboguice.fragment.RoboDialogFragment
import javax.inject.Inject

class CourseModalDialogFragment : RoboDialogFragment() {

    private lateinit var binding: DialogUpgradeFeaturesBinding
    private var courseId: String = ""
    private var price: String = ""
    private var isSelfPaced: Boolean = false

    private var billingProcessor: BillingProcessor? = null
    private lateinit var iapViewModel: InAppPurchasesViewModel

    @Inject
    private lateinit var environment: IEdxEnvironment

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
            courseId = bundle.getString(KEY_COURSE_ID) ?: ""
            price = bundle.getString(KEY_COURSE_PRICE) ?: ""
            isSelfPaced = bundle.getBoolean(KEY_IS_SELF_PACED)
            environment.analyticsRegistry.trackValuePropLearnMoreTapped(
                courseId, null,
                Analytics.Screens.COURSE_ENROLLMENT
            )
            environment.analyticsRegistry.trackValuePropModalView(
                courseId, null,
                Analytics.Screens.COURSE_ENROLLMENT
            )
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
        iapViewModel = ViewModelProvider(
            this,
            ViewModelFactory()
        ).get(InAppPurchasesViewModel::class.java)
        initObserver()

        binding.layoutUpgradeBtn.btnUpgrade.setOnClickListener {
            ProductManager.getProductByCourseId(courseId)?.let {
                iapViewModel.addProductToBasket(it)
            } ?: showUpgradeErrorDialog()
            environment.analyticsRegistry.trackUpgradeNowClicked(
                courseId,
                price,
                null,
                isSelfPaced
            )
        }
        billingProcessor =
            BillingProcessor(requireContext(), object : BillingProcessor.BillingFlowListeners {
                override fun onPurchaseCancel() {
                    iapViewModel.endLoading()
                    showUpgradeErrorDialog()
                }

                override fun onPurchaseComplete(purchase: Purchase) {
                    onProductPurchased(purchase.purchaseToken)
                }
            })
    }

    private fun initObserver() {
        iapViewModel.showLoader.observe(viewLifecycleOwner, NonNullObserver {
            enableUpgradeButton(!it)
        })

        iapViewModel.checkoutResponse.observe(viewLifecycleOwner, NonNullObserver {
            if (it.paymentPageUrl.isNotEmpty())
                purchaseProduct(iapViewModel.getProductId())
        })

        iapViewModel.executeOrderResponse.observe(viewLifecycleOwner, NonNullObserver {
            showUpgradeCompleteDialog()
        })

        iapViewModel.errorMessage.observe(viewLifecycleOwner, NonNullObserver { errorMsg ->
            if (errorMsg.throwable is HttpStatusException) {
                when (errorMsg.throwable.statusCode) {
                    HttpStatus.UNAUTHORIZED,
                    HttpStatus.FORBIDDEN -> {
                        environment.router?.forceLogout(
                            requireContext(),
                            environment.analyticsRegistry,
                            environment.notificationDelegate
                        )
                        return@NonNullObserver
                    }
                    else -> showUpgradeErrorDialog()
                }
            } else {
                showUpgradeErrorDialog()
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
            executeOrder(purchaseToken)
        }
    }

    private fun executeOrder(purchaseToken: String) {
        iapViewModel.executeOrder(purchaseToken = purchaseToken)
    }

    private fun showUpgradeErrorDialog() {
        AlertDialogFragment.newInstance(
            getString(R.string.title_upgrade_error),
            getString(R.string.upgrade_error_message),
            getString(R.string.label_close),
            null,
            getString(R.string.label_get_help)
        ) { _, _ ->
            environment.router?.showFeedbackScreen(
                requireActivity(),
                getString(R.string.email_subject_upgrade_error)
            )
        }.show(childFragmentManager, null)
    }

    private fun showUpgradeCompleteDialog() {
        AlertDialogFragment.newInstance(
            getString(R.string.title_upgrade_complete),
            getString(R.string.upgrade_success_message),
            getString(R.string.label_continue),
            null,
            null,
            null
        ).show(childFragmentManager, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        billingProcessor?.disconnect()
    }

    companion object {
        const val TAG: String = "CourseModalDialogFragment"
        const val KEY_MODAL_PLATFORM = "platform_name"
        const val KEY_COURSE_ID = "course_id"
        const val KEY_COURSE_NAME = "course_name"
        const val KEY_COURSE_PRICE = "course_price"
        const val KEY_IS_SELF_PACED = "is_Self_Paced"

        @JvmStatic
        fun newInstance(
            platformName: String,
            courseId: String,
            courseName: String,
            price: String,
            isSelfPaced: Boolean
        ): CourseModalDialogFragment {
            val frag = CourseModalDialogFragment()
            val args = Bundle().apply {
                putString(KEY_MODAL_PLATFORM, platformName)
                putString(KEY_COURSE_ID, courseId)
                putString(KEY_COURSE_NAME, courseName)
                putString(KEY_COURSE_PRICE, price)
                putBoolean(KEY_IS_SELF_PACED, isSelfPaced)
            }
            frag.arguments = args
            return frag
        }
    }
}
