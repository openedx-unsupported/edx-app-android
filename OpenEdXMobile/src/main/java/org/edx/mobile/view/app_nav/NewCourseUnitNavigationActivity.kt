package org.edx.mobile.view.app_nav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.R
import org.edx.mobile.base.BaseFragmentActivity
import org.edx.mobile.databinding.ActivityCourseUnitNavigationNewBinding
import org.edx.mobile.databinding.LayoutUnitsDropDownBinding
import org.edx.mobile.extenstion.CollapsingToolbarStatListener
import org.edx.mobile.extenstion.setTextWithIcon
import org.edx.mobile.extenstion.setTitleStateListener
import org.edx.mobile.model.api.CourseUpgradeResponse
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.model.course.IBlock
import org.edx.mobile.services.CourseManager
import org.edx.mobile.util.AppConstants
import org.edx.mobile.util.UiUtils
import org.edx.mobile.util.ViewAnimationUtil
import org.edx.mobile.util.images.ImageUtils
import org.edx.mobile.view.Router
import org.edx.mobile.view.adapters.UnitsDropDownAdapter
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

    private fun setupToolbar() {
        component?.let { subSection ->
            // Sub section title
            binding.courseSubSectionTitle.text = subSection.displayName

            val hasMultipleChildren =
                subSection.children.isNullOrEmpty().not() && subSection.children.size > 1
            subSection.firstIncompleteComponent?.let {
                setUnitTitle(it, hasMultipleChildren)
                if (hasMultipleChildren) {
                    setupUnitsDropDown(subSection.children, it)
                }
            }

            ViewAnimationUtil.startAlphaAnimation(
                binding.collapsedToolbarTitle,
                View.INVISIBLE
            )
            setupToolbarListeners()
        }
    }

    private fun setupToolbarListeners() {
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

    private fun setUnitTitle(firstIncompleteUnit: CourseComponent, hasMoreThenOneChild: Boolean) {
        val dropdownIcon = ImageUtils.rotateVectorDrawable(this, R.drawable.ic_play_arrow, 90f)
        var unitTitle = firstIncompleteUnit.displayName
        if (hasMoreThenOneChild) {
            unitTitle += "  " + AppConstants.ICON_PLACEHOLDER
        }
        binding.collapsedToolbarTitle.text = firstIncompleteUnit.displayName
        // first incomplete unit title
        binding.courseUnitTitle.setTextWithIcon(
            unitTitle,
            dropdownIcon,
            AppConstants.ICON_PLACEHOLDER
        )
    }

    private fun setupUnitsDropDown(units: MutableList<IBlock>, selectedUnits: CourseComponent) {
        binding.courseUnitTitle.setOnClickListener {
            val popupViewBinding =
                LayoutUnitsDropDownBinding.inflate(LayoutInflater.from(it.context), null, false)
            val popupWindow = PopupWindow()
            popupWindow.contentView = popupViewBinding.root
            popupWindow.width = binding.flUnitsDropDown.width
            popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT
            popupWindow.isOutsideTouchable = true
            popupWindow.setBackgroundDrawable(getDrawable(R.drawable.white_roundedbg))

            val adapter =
                UnitsDropDownAdapter(courseData, units, object : UnitsDropDownAdapter.OnItemSelect {
                    override fun onUnitSelect(unit: IBlock) {
                        setUnitTitle(unit as CourseComponent, true)
                        popupWindow.dismiss()
                    }
                })
            popupViewBinding.rvUnits.adapter = adapter

            popupWindow.elevation =
                UiUtils.dpToPx(this, resources.getDimension(R.dimen.edx_quarter_margin))
            // Show the popup below the anchor view with the specified offsets in pixels
            popupWindow.showAsDropDown(
                binding.flUnitsDropDown, 0,
                UiUtils.dpToPx(this, resources.getDimension(R.dimen.edx_x_quarter_margin)).toInt()
            )
        }
    }
}
