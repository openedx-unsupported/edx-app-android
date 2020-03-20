package org.edx.mobile.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.fragment_payments_info.*
import org.edx.mobile.R
import org.edx.mobile.base.BaseFragment
import org.edx.mobile.databinding.FragmentPaymentsInfoBinding
import org.edx.mobile.model.api.CourseUpgradeResponse
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.util.DateUtil
import org.edx.mobile.util.ResourceUtil
import java.text.SimpleDateFormat
import java.util.*

class PaymentsInfoFragment : BaseFragment() {
    companion object {
        fun newInstance(extras: Bundle): PaymentsInfoFragment {
            val fragment = PaymentsInfoFragment()
            fragment.arguments = extras
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding: FragmentPaymentsInfoBinding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_payments_info, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = view.context
        btn_close.setOnClickListener { activity?.finish() }
        val courseData = arguments?.getSerializable(Router.EXTRA_COURSE_DATA) as EnrolledCoursesResponse

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
        val stringBuilder: StringBuilder = java.lang.StringBuilder()

        // Populate access expires content
        if (!android.text.TextUtils.isEmpty(courseData.auditAccessExpires)) {
            val expiryDate = DateUtil.convertToDate(courseData.auditAccessExpires)
            tv_audit_access_expires_on.text = ResourceUtil.getFormattedString(context.resources, R.string.audit_access_expires_on, "date",
                    dateFormat.format(expiryDate))
            stringBuilder.append(ResourceUtil.getFormattedString(context.resources, R.string.audit_access_expires_details, "date", dateFormat.format(expiryDate)).toString())
        }
        // Populate upgrade deadline content
        if (!android.text.TextUtils.isEmpty(courseData.course.dynamicUpgradeDeadline)) {
            val upgradeBy = DateUtil.convertToDate(courseData.course.dynamicUpgradeDeadline)
            stringBuilder.append("\n\n").append(
                    ResourceUtil.getFormattedString(context.resources, R.string.upgrade_deadline_details, "date", dateFormat.format(upgradeBy)).toString()
            )
        }

        tv_audit_access_expires_details.text = stringBuilder.toString()

        val courseUpgradeData = arguments?.getParcelable(Router.EXTRA_COURSE_UPGRADE_DATA) as CourseUpgradeResponse
        PaymentsBannerFragment.loadPaymentsBannerFragment(R.id.fragment_container, courseData, null,
                courseUpgradeData, false, childFragmentManager, false)
    }
}
