package org.edx.mobile.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.edx.mobile.R
import org.edx.mobile.databinding.FragmentCourseUnitGradeBinding
import org.edx.mobile.extenstion.isInvisible
import org.edx.mobile.extenstion.visibility
import org.edx.mobile.inapppurchases.BillingProcessor
import org.edx.mobile.inapppurchases.BillingProcessor.BillingFlowListeners
import org.edx.mobile.model.api.AuthorizationDenialReason
import org.edx.mobile.model.course.BlockType
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.util.BrowserUtil
import org.edx.mobile.util.UiUtils.getDrawable
import org.edx.mobile.view.dialog.AlertDialogFragment
import java.util.Locale

class CourseUnitMobileNotSupportedFragment : CourseUnitFragment() {
    private lateinit var binding: FragmentCourseUnitGradeBinding
    private var billingProcessor: BillingProcessor? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCourseUnitGradeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (AuthorizationDenialReason.FEATURE_BASED_ENROLLMENTS == unit?.authorizationDenialReason) {
            if (environment.remoteFeaturePrefs.isValuePropEnabled()) {
                showGradedContent()
            } else {
                showNotAvailableOnMobile()
            }
        } else {
            showOnlyAvailableOnWeb()
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

    private fun showGradedContent() {
        val isSelfPaced = getBooleanArgument(Router.EXTRA_IS_SELF_PACED, false)
        val price = getStringArgument(Router.EXTRA_PRICE)
        binding.containerLayoutNotAvailable.visibility = View.GONE
        binding.llGradedContentLayout.visibility = View.VISIBLE
        setUpUpgradeButton(isSelfPaced, price)

        binding.toggleShow.setOnClickListener {
            val showMore = binding.layoutUpgradeFeature.containerLayout.isInvisible()
            binding.layoutUpgradeFeature.containerLayout.visibility(showMore)
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

    private fun showNotAvailableOnMobile() {
        binding.containerLayoutNotAvailable.visibility = View.VISIBLE
        binding.llGradedContentLayout.visibility = View.GONE
        binding.contentErrorIcon.setImageDrawable(getDrawable(requireContext(), R.drawable.ic_lock))
        binding.notAvailableMessage.setText(R.string.not_available_on_mobile)
        binding.notAvailableMessage2.visibility = View.GONE
    }

    private fun showOnlyAvailableOnWeb() {
        binding.containerLayoutNotAvailable.visibility = View.VISIBLE
        binding.llGradedContentLayout.visibility = View.GONE
        binding.contentErrorIcon.setImageDrawable(
            getDrawable(requireContext(), R.drawable.ic_laptop)
        )
        binding.notAvailableMessage.setText(
            if (unit?.type === BlockType.VIDEO) R.string.video_only_on_web_short
            else R.string.assessment_not_available
        )
        binding.notAvailableMessage2.visibility = View.VISIBLE
    }

    private fun setUpUpgradeButton(isSelfPaced: Boolean, price: String) {
        if (environment.config.isIAPEnabled) {
            binding.layoutUpgradeBtn.root.visibility = View.VISIBLE
            binding.layoutUpgradeBtn.btnUpgrade.setOnClickListener {
                enableUpgradeButton(false)
                purchaseProduct("org.edx.mobile.test_product")
                unit?.let {
                    environment.analyticsRegistry.trackUpgradeNowClicked(
                        it.courseId, price, it.id, isSelfPaced
                    )
                }
            }

            billingProcessor = BillingProcessor(requireContext(), object : BillingFlowListeners {
                override fun onPurchaseCancel() {
                    enableUpgradeButton(true)
                }

                override fun onPurchaseComplete(purchase: Purchase) {
                    CoroutineScope(Dispatchers.Main).launch {
                        enableUpgradeButton(true)
                    }
                    AlertDialogFragment.newInstance(
                        getString(R.string.title_upgrade_complete),
                        getString(R.string.upgrade_success_message),
                        getString(R.string.label_continue).toUpperCase(Locale.ROOT),
                        null,
                        null,
                        null
                    ).show(childFragmentManager, null)
                }
            })
        } else {
            binding.layoutUpgradeBtn.root.visibility = View.GONE
        }
    }

    private fun enableUpgradeButton(enable: Boolean) {
        binding.layoutUpgradeBtn.btnUpgrade.visibility(enable)
        binding.layoutUpgradeBtn.loadingIndicator.visibility(!enable)
    }

    private fun purchaseProduct(productId: String) {
        activity?.let { billingProcessor?.purchaseItem(it, productId) }
    }

    override fun onResume() {
        super.onResume()
        unit?.let {
            if (it.authorizationDenialReason == AuthorizationDenialReason.FEATURE_BASED_ENROLLMENTS
                && environment.remoteFeaturePrefs.isValuePropEnabled()
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
            price: String
        ): CourseUnitMobileNotSupportedFragment {
            val fragment = CourseUnitMobileNotSupportedFragment()
            val args = Bundle()
            args.putSerializable(Router.EXTRA_COURSE_UNIT, unit)
            args.putBoolean(Router.EXTRA_IS_SELF_PACED, isSelfPaced)
            args.putString(Router.EXTRA_PRICE, price)
            fragment.arguments = args
            return fragment
        }
    }
}
