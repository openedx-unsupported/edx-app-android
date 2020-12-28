package org.edx.mobile.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import org.edx.mobile.R
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.databinding.DialogUpgradeFeaturesBinding
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.util.ResourceUtil
import roboguice.fragment.RoboDialogFragment
import javax.inject.Inject

class CourseModalDialogFragment : RoboDialogFragment() {

    private lateinit var binding: DialogUpgradeFeaturesBinding
    private var courseId: String = ""

    @Inject
    private lateinit var environment: IEdxEnvironment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL,
                R.style.AppTheme_NoActionBar)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_upgrade_features, container,
                false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { bundle ->
            courseId = bundle.getString(KEY_COURSE_ID) ?: ""
            environment.analyticsRegistry.trackValuePropLearnMoreTapped(courseId, null,
                    Analytics.Screens.COURSE_ENROLLMENT)
            environment.analyticsRegistry.trackValuePropModalView(courseId, null,
                    Analytics.Screens.COURSE_ENROLLMENT)
        }

        binding.dialogTitle.text = getString(R.string.course_dashboard_modal_title)
        binding.supportNonProfit.text = ResourceUtil.getFormattedString(resources, R.string.course_modal_support_non_profit, KEY_MODAL_PLATFORM, arguments?.getString(KEY_MODAL_PLATFORM))
        binding.dialogDismiss.setOnClickListener {
            dialog?.dismiss()
        }
    }

    companion object {
        const val TAG: String = "CourseModalDialogFragment"
        const val KEY_MODAL_PLATFORM = "platform_name"
        const val KEY_COURSE_ID = "course_id"

        @JvmStatic
        fun newInstance(platformName: String, courseId: String): CourseModalDialogFragment {
            val frag = CourseModalDialogFragment()
            val args = Bundle().apply {
                putString(KEY_MODAL_PLATFORM, platformName)
                putString(KEY_COURSE_ID, courseId)
            }
            frag.arguments = args
            return frag
        }
    }
}
