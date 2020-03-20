package org.edx.mobile.view

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.inject.Inject
import kotlinx.android.synthetic.main.fragment_payments_banner.*
import org.edx.mobile.R
import org.edx.mobile.base.BaseFragment
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.databinding.FragmentPaymentsBannerBinding
import org.edx.mobile.model.api.CourseUpgradeResponse
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.course.CourseComponent

class PaymentsBannerFragment : BaseFragment() {
    @Inject
    var environment: IEdxEnvironment? = null

    companion object {
        private const val EXTRA_SHOW_INFO_BUTTON = "show_info_button"
        private fun newInstance(courseData: EnrolledCoursesResponse,
                                courseUnit: CourseComponent?,
                                courseUpgradeData: CourseUpgradeResponse,
                                showInfoButton: Boolean): Fragment {
            val fragment = PaymentsBannerFragment()
            val bundle = Bundle()
            bundle.putSerializable(Router.EXTRA_COURSE_DATA, courseData)
            bundle.putSerializable(Router.EXTRA_COURSE_UNIT, courseUnit)
            bundle.putParcelable(Router.EXTRA_COURSE_UPGRADE_DATA, courseUpgradeData)
            bundle.putBoolean(EXTRA_SHOW_INFO_BUTTON, showInfoButton)
            fragment.arguments = bundle
            return fragment
        }

        fun loadPaymentsBannerFragment(containerId: Int,
                                       courseData: EnrolledCoursesResponse,
                                       courseUnit: CourseComponent?,
                                       courseUpgradeData: CourseUpgradeResponse, showInfoButton: Boolean,
                                       childFragmentManager: FragmentManager, animate: Boolean) {
            val frag: Fragment? = childFragmentManager.findFragmentByTag("payment_banner_frag")
            if (frag != null) {
                // Payment banner already exists
                return
            }
            val fragment: Fragment = newInstance(courseData, courseUnit, courseUpgradeData, showInfoButton)
            // This activity will only ever hold this lone fragment, so we
            // can afford to retain the instance during activity recreation
            fragment.retainInstance = true
            val fragmentTransaction: FragmentTransaction = childFragmentManager.beginTransaction()
            if (animate) {
                fragmentTransaction.setCustomAnimations(R.anim.slide_up, android.R.anim.fade_out)
            }
            fragmentTransaction.replace(containerId, fragment, "payment_banner_frag")
            fragmentTransaction.disallowAddToBackStack()
            fragmentTransaction.commitAllowingStateLoss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding: FragmentPaymentsBannerBinding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_payments_banner, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateCourseUpgradeBanner(view.context)
    }

    private fun populateCourseUpgradeBanner(context: Context) {
        val courseUpgradeData: CourseUpgradeResponse =
                arguments?.getParcelable(Router.EXTRA_COURSE_UPGRADE_DATA) as CourseUpgradeResponse
        val courseData: EnrolledCoursesResponse =
                arguments?.getSerializable(Router.EXTRA_COURSE_DATA) as EnrolledCoursesResponse
        val showInfoButton: Boolean = arguments?.getBoolean(EXTRA_SHOW_INFO_BUTTON) ?: false
        upgrade_to_verified_footer.visibility = View.VISIBLE
        if (showInfoButton) {
            info.visibility = View.VISIBLE
            info.setOnClickListener {
                environment?.router?.showPaymentsInfoActivity(context, courseData, courseUpgradeData)
            }
        } else {
            info.visibility = View.GONE
        }
        if (!TextUtils.isEmpty(courseUpgradeData.price)) {
            tv_upgrade_price.text = courseUpgradeData.price
        } else {
            tv_upgrade_price.visibility = View.GONE
        }

        val courseUnit: CourseComponent? = arguments?.getSerializable(Router.EXTRA_COURSE_UNIT) as CourseComponent?

        courseUpgradeData.basketUrl?.let { basketUrl ->
            ll_upgrade_button.setOnClickListener {
                environment?.router?.showCourseUpgradeWebViewActivity(
                        context, basketUrl, courseData, courseUnit)
            }
        }
    }
}
