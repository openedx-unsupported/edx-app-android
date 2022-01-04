package org.edx.mobile.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.android.billingclient.api.Purchase
import org.edx.mobile.R
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.databinding.DialogUpgradeFeaturesBinding
import org.edx.mobile.inapppurchases.BillingProcessor
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.util.ResourceUtil
import roboguice.fragment.RoboDialogFragment
import javax.inject.Inject

class CourseModalDialogFragment : RoboDialogFragment() {

    private lateinit var binding: DialogUpgradeFeaturesBinding
    private var courseId: String = ""
    private var price: String = ""
    private var isSelfPaced: Boolean = false

    private var billingProcessor: BillingProcessor? = null

    @Inject
    private lateinit var environment: IEdxEnvironment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL,
                R.style.AppTheme_NoActionBar)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_upgrade_features, container,
                false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogSlideUpAndDownAnimation
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { bundle ->
            courseId = bundle.getString(KEY_COURSE_ID) ?: ""
            price = bundle.getString(KEY_COURSE_PRICE) ?: ""
            isSelfPaced = bundle.getBoolean(KEY_IS_SELF_PACED)
            environment.analyticsRegistry.trackValuePropLearnMoreTapped(courseId, null, Analytics.Screens.COURSE_ENROLLMENT)
            environment.analyticsRegistry.trackValuePropModalView(courseId, null, Analytics.Screens.COURSE_ENROLLMENT)
        }

        binding.dialogTitle.text = ResourceUtil.getFormattedString(resources, R.string.course_modal_heading, KEY_COURSE_NAME, arguments?.getString(KEY_COURSE_NAME))
        binding.dialogDismiss.setOnClickListener {
            dialog?.dismiss()
        }

        if (environment.config.isIAPEnabled) {
            binding.layoutUpgradeBtn.root.visibility = View.VISIBLE
            binding.layoutUpgradeBtn.btnUpgrade.setOnClickListener {
                enableUpgradeButton(false)
                purchaseProduct("org.edx.mobile.test_product")
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
                        enableUpgradeButton(true)
                    }

                    override fun onPurchaseComplete(purchase: Purchase) {
                        enableUpgradeButton(true)
                    }
                })
        } else {
            binding.layoutUpgradeBtn.root.visibility = View.GONE
        }
    }

    private fun enableUpgradeButton(enable: Boolean) {
        binding.layoutUpgradeBtn.btnUpgrade.visibility = if (enable) View.VISIBLE else View.GONE
        binding.layoutUpgradeBtn.loadingIndicator.visibility =
            if (!enable) View.VISIBLE else View.GONE
    }

    private fun purchaseProduct(productId: String) {
        activity?.let { billingProcessor?.purchaseItem(it, productId) }
    }

    companion object {
        const val TAG: String = "CourseModalDialogFragment"
        const val KEY_MODAL_PLATFORM = "platform_name"
        const val KEY_COURSE_ID = "course_id"
        const val KEY_COURSE_NAME = "course_name"
        const val KEY_COURSE_PRICE = "course_price"
        const val KEY_IS_SELF_PACED = "is_Self_Paced"

        @JvmStatic
        fun newInstance(platformName: String, courseId: String, courseName: String, price: String, isSelfPaced: Boolean): CourseModalDialogFragment {
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

    override fun onDestroyView() {
        super.onDestroyView()
        billingProcessor?.disconnect()
    }
}
