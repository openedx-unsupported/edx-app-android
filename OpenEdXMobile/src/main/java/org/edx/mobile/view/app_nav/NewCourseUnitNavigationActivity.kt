package org.edx.mobile.view.app_nav

import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.R
import org.edx.mobile.base.BaseFragmentActivity
import org.edx.mobile.databinding.ActivityCourseUnitNavigationNewBinding
import org.edx.mobile.extenstion.CollapsingToolbarStatListener
import org.edx.mobile.extenstion.setTextWithIcon
import org.edx.mobile.extenstion.setTitleStateListener
import org.edx.mobile.model.api.CourseUpgradeResponse
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.services.CourseManager
import org.edx.mobile.util.AppConstants
import org.edx.mobile.util.ViewAnimationUtil
import org.edx.mobile.util.images.ImageUtils
import org.edx.mobile.view.Router
import javax.inject.Inject

@AndroidEntryPoint
class NewCourseUnitNavigationActivity : BaseFragmentActivity() {

    private lateinit var binding: ActivityCourseUnitNavigationNewBinding

    @Inject
    lateinit var courseManager: CourseManager

    private var courseData: EnrolledCoursesResponse? = null
    private var component: CourseComponent? = null
    private var courseUpgradeData: CourseUpgradeResponse? = null
    private var courseComponentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCourseUnitNavigationNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var bundle = savedInstanceState
        if (bundle == null) {
            if (intent != null) bundle = intent.getBundleExtra(Router.EXTRA_BUNDLE)
        }
        restore(bundle)
        // If the data is available then trigger the callback
        // after basic initialization
        if (courseComponentId != null && environment.loginPrefs.isUserLoggedIn) {
            onLoadData()
            setupToolbar()
        }

    }

    private fun setupToolbar() {
        // Sub section title
        binding.courseSubSectionTitle.text = component?.displayName
        val dropdownIcon = ImageUtils.rotateVectorDrawable(this, R.drawable.ic_play_arrow, 90f)
        val firstIncompleteUnit = component?.firstIncompleteComponent
        val unitTitle = firstIncompleteUnit?.displayName + "  " + AppConstants.ICON_PLACEHOLDER
        binding.collapsedToolbarTitle.text = firstIncompleteUnit?.displayName
        ViewAnimationUtil.startAlphaAnimation(
            binding.collapsedToolbarTitle,
            View.INVISIBLE
        )
        // first incomplete unit title
        binding.courseUnitTitle.setTextWithIcon(
            unitTitle,
            dropdownIcon,
            AppConstants.ICON_PLACEHOLDER
        )
        binding.appbar.setTitleStateListener(
            binding.collapsingToolbarLayout,
            object : CollapsingToolbarStatListener {
                override fun onExpanded() {
                    ViewAnimationUtil.startAlphaAnimation(
                        binding.collapsedToolbarTitle,
                        View.GONE
                    )
                    ViewAnimationUtil.startAlphaAnimation(
                        binding.expandedToolbarLayout,
                        View.VISIBLE
                    )
                }

                override fun onCollapsed() {
                    ViewAnimationUtil.startAlphaAnimation(
                        binding.collapsedToolbarTitle,
                        View.VISIBLE
                    )
                    ViewAnimationUtil.startAlphaAnimation(
                        binding.expandedToolbarLayout,
                        View.GONE
                    )
                }

            })
        binding.ivCollapsedBack.setOnClickListener { finish() }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restore(savedInstanceState)
    }

    @Suppress("DEPRECATION")
    private fun onLoadData() {
        component =
            courseData?.courseId?.let { courseId ->
                courseComponentId?.let { componentId ->
                    courseManager.getComponentById(courseId, componentId)
                }
            }
    }

    private fun restore(bundle: Bundle?) {
        bundle?.let {
            courseData =
                bundle.getSerializable(Router.EXTRA_COURSE_DATA) as EnrolledCoursesResponse?
            courseUpgradeData = bundle.getParcelable(Router.EXTRA_COURSE_UPGRADE_DATA)
            courseComponentId = bundle.getString(Router.EXTRA_COURSE_COMPONENT_ID)
        }
    }
}
