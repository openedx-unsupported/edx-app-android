package org.edx.mobile.view.app_nav

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.R
import org.edx.mobile.base.BaseFragmentActivity
import org.edx.mobile.course.CourseAPI
import org.edx.mobile.databinding.ActivityCourseUnitNavigationNewBinding
import org.edx.mobile.databinding.LayoutUnitsDropDownBinding
import org.edx.mobile.event.VideoPlaybackEvent
import org.edx.mobile.extenstion.CollapsingToolbarStatListener
import org.edx.mobile.extenstion.parcelable
import org.edx.mobile.extenstion.serializableOrThrow
import org.edx.mobile.extenstion.setTextWithIcon
import org.edx.mobile.extenstion.setTitleStateListener
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.http.callback.ErrorHandlingCallback
import org.edx.mobile.model.api.CourseUpgradeResponse
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.course.BlockType
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.model.course.CourseStatus
import org.edx.mobile.model.course.IBlock
import org.edx.mobile.services.CourseManager
import org.edx.mobile.util.AppConstants
import org.edx.mobile.util.UiUtils
import org.edx.mobile.util.VideoUtil
import org.edx.mobile.util.ViewAnimationUtil
import org.edx.mobile.util.images.ImageUtils
import org.edx.mobile.util.images.ShareUtils
import org.edx.mobile.view.CourseUnitFragment
import org.edx.mobile.view.Router
import org.edx.mobile.view.adapters.NewCourseUnitPagerAdapter
import org.edx.mobile.view.adapters.UnitsDropDownAdapter
import org.edx.mobile.view.custom.PreLoadingListener
import org.edx.mobile.view.dialog.CelebratoryModalDialogFragment
import org.greenrobot.eventbus.EventBus
import java.util.EnumSet
import javax.inject.Inject

@AndroidEntryPoint
class NewCourseUnitNavigationActivity : BaseFragmentActivity(), CourseUnitFragment.HasComponent,
    PreLoadingListener {

    private lateinit var binding: ActivityCourseUnitNavigationNewBinding

    @Inject
    lateinit var courseManager: CourseManager

    @Inject
    lateinit var courseApi: CourseAPI

    private lateinit var courseData: EnrolledCoursesResponse
    private var courseComponentId: String? = null
    private var subsection: CourseComponent? = null
    private var courseUpgradeData: CourseUpgradeResponse? = null

    private lateinit var pagerAdapter: NewCourseUnitPagerAdapter
    private var isVideoMode = false
    private var viewPagerState = PreLoadingListener.State.DEFAULT
    private var isFirstSection = false

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

        if (intent != null) {
            isVideoMode = intent.extras?.getBoolean(Router.EXTRA_IS_VIDEOS_MODE, false) as Boolean
        }
        initAdapter()
        // Enforce to intercept single scrolling direction
        UiUtils.enforceSingleScrollDirection(binding.pager2)
        if (!isVideoMode) {
            courseCelebrationStatus
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restore(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()
        updateUIForOrientation()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateUIForOrientation()
        if (subsection != null) {
            environment.analyticsRegistry.trackCourseComponentViewed(
                subsection?.id,
                courseData.courseId, subsection?.blockId
            )
        }
        // Remove the celebration modal on configuration change before create a new one for landscape mode.
        val celebrationModal =
            supportFragmentManager.findFragmentByTag(CelebratoryModalDialogFragment.TAG)
        if (celebrationModal != null) {
            supportFragmentManager.beginTransaction().remove(celebrationModal).commit()
            showCelebrationModal(true)
        }
    }

    @Suppress("DEPRECATION")
    private fun onLoadData() {
        subsection = courseComponentId?.let { componentId ->
            courseManager.getComponentById(courseData.courseId, componentId)
        }
    }

    private fun restore(bundle: Bundle?) {
        bundle?.let {
            courseData =
                bundle.serializableOrThrow(Router.EXTRA_COURSE_DATA) as EnrolledCoursesResponse
            courseUpgradeData = bundle.parcelable(Router.EXTRA_COURSE_UPGRADE_DATA)
            courseComponentId = bundle.getString(Router.EXTRA_COURSE_COMPONENT_ID)
            isVideoMode = bundle.getBoolean(Router.EXTRA_IS_VIDEOS_MODE, false)
        }
    }

    private fun setupToolbar() {
        subsection?.let { subSection ->
            val hasMultipleChildren =
                subSection.children.isNullOrEmpty().not() && subSection.children.size > 1
            subSection.firstIncompleteComponent?.let {
                setToolbarTitle(it, hasMultipleChildren)
                if (hasMultipleChildren) {
                    setupUnitsDropDown(subSection.children, it)
                }
                updateCompletionProgressBar(0, it.children.size)
            }

            ViewAnimationUtil.startAlphaAnimation(
                binding.collapsedToolbarTitle,
                View.INVISIBLE
            )
            setupToolbarListeners()
        }
    }

    private fun updateCompletionProgressBar(currentPosition: Int, size: Int) {
        binding.spbUnits.setDivisions(size)
        binding.spbUnits.setDividerEnabled(true)
        binding.spbUnits.setEnabledDivisions((0..currentPosition).toList())
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

    private fun setToolbarTitle(courseUnit: CourseComponent, hasMoreThenOneChild: Boolean) {
        // Sub section title
        binding.courseSubSectionTitle.text = courseUnit.parent.displayName

        val dropdownIcon = ImageUtils.rotateVectorDrawable(this, R.drawable.ic_play_arrow, 90f)
        var unitTitle = courseUnit.displayName
        if (hasMoreThenOneChild) {
            unitTitle += "  " + AppConstants.ICON_PLACEHOLDER
        }
        binding.collapsedToolbarTitle.text = courseUnit.displayName
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
            popupWindow.setBackgroundDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.white_roundedbg
                )
            )

            val adapter =
                UnitsDropDownAdapter(courseData, units, object : UnitsDropDownAdapter.OnItemSelect {
                    override fun onUnitSelect(unit: IBlock) {
                        setToolbarTitle(unit as CourseComponent, true)
                        var index = 0
                        unit.firstIncompleteComponent?.let { component ->
                            index = (binding.pager2.adapter as NewCourseUnitPagerAdapter)
                                .getComponentIndex(component)
                        }
                        binding.pager2.setCurrentItem(index, false)
                        popupWindow.dismiss()
                    }
                })
            popupViewBinding.rvUnits.adapter = adapter
            val index = adapter.getUnitIndex(selectedUnits)
            adapter.setSelection(index)

            popupWindow.elevation =
                UiUtils.dpToPx(this, resources.getDimension(R.dimen.edx_quarter_margin))
            // Show the popup below the anchor view with the specified offsets in pixels
            popupWindow.showAsDropDown(
                binding.flUnitsDropDown, 0,
                UiUtils.dpToPx(this, resources.getDimension(R.dimen.edx_x_quarter_margin)).toInt()
            )
        }
    }

    private fun initAdapter() {
        if (subsection?.root == null) {
            logger.warn("selectedUnit is null?")
            return  //should not happen
        }
        val componentList = getComponentList()
        pagerAdapter =
            NewCourseUnitPagerAdapter(
                this,
                environment,
                componentList,
                courseData,
                courseUpgradeData,
                this
            )
        binding.pager2.adapter = pagerAdapter
        binding.pager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                invalidateOptionsMenu()
            }

            override fun onPageSelected(position: Int) {
                val currentComponent = pagerAdapter.getComponent(position)
                if (currentComponent.isMultiDevice) {
                    // Disable ViewPager2 scrolling to enable horizontal scrolling to for the
                    // WebView (Specific HTML Components).
                    val horizontalBlocks = listOf(
                        BlockType.DRAG_AND_DROP_V2,
                        BlockType.LTI_CONSUMER,
                        BlockType.WORD_CLOUD
                    )
                    binding.pager2.isUserInputEnabled =
                        !horizontalBlocks.contains(currentComponent.type)
                }
                updateCurrentComponent(currentComponent)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        val firstIncompleteComponentIndex =
            componentList.indexOf(subsection?.firstIncompleteComponent?.firstIncompleteComponent)
        binding.pager2.setCurrentItem(firstIncompleteComponentIndex, false)
    }

    private fun updateCurrentComponent(currentComponent: CourseComponent) {
        // CourseComponent#getAncestor(1) returns the current Subsection of a component
        val currentSubSection = currentComponent.getAncestor(1)
        val currentUnit = currentComponent.parent
        setToolbarTitle(currentUnit, currentSubSection.children.size > 1)
        if (subsection?.id.equals(currentSubSection.id).not()) {
            subsection = currentSubSection
        }
        subsection?.children?.let { setupUnitsDropDown(it, currentUnit) }
        val currentComponentIndex = currentUnit.children.indexOf(currentComponent)
        updateCompletionProgressBar(currentComponentIndex, currentUnit.children.size)
    }

    private fun getComponentList(): MutableList<CourseComponent> {
        val leaves: MutableList<CourseComponent> = ArrayList()
        if (isVideoMode) {
            subsection?.root?.getVideos(false)?.let {
                leaves.addAll(it)
            }
        } else {
            subsection?.root?.fetchAllLeafComponents(leaves, EnumSet.allOf(BlockType::class.java))
        }
        return leaves
    }

    override fun navigateNextComponent() {
        if (pagerAdapter.itemCount == 0) {
            return
        }
        val index = binding.pager2.currentItem
        if (index < pagerAdapter.itemCount - 1) {
            binding.pager2.currentItem = index + 1
        }
        // CourseComponent#getAncestor(2) returns the section of a component
        val currentBlockSection = subsection?.getAncestor(2)
        val nextBlockSection = pagerAdapter.getComponent(binding.pager2.currentItem).getAncestor(2)
        /*
         * Show celebratory modal when:
         * 1. We haven't arrived at component navigation from the Videos tab.
         * 2. The current section is the first section being completed (not necessarily the actual first section of the course).
         * 3. Section of the current and next components are different.
         */if (!isVideoMode && isFirstSection && currentBlockSection != nextBlockSection) {
            showCelebrationModal(false)
        }
    }

    override fun navigatePreviousComponent() {
        if (pagerAdapter.itemCount == 0) {
            return
        }
        val index = binding.pager2.currentItem
        if (index > 0) {
            binding.pager2.currentItem = index - 1
        }
    }

    override fun refreshCourseData(courseId: String, componentId: String) {

    }

    override fun initializeIAPObserver() {
    }

    override fun setLoadingState(newState: PreLoadingListener.State) {
        viewPagerState = newState
    }

    override fun isMainUnitLoaded(): Boolean {
        return viewPagerState == PreLoadingListener.State.MAIN_UNIT_LOADED
    }

    private fun updateUIForOrientation() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
            VideoUtil.isCourseUnitVideo(environment, getCurrentComponent())
        ) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, window.decorView).apply {
                hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            }
            binding.appbar.setVisibility(false)
        } else {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowInsetsControllerCompat(window, window.decorView)
                .show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            binding.appbar.setVisibility(true)
        }
    }

    private fun getCurrentComponent(): CourseComponent {
        return (binding.pager2.adapter as NewCourseUnitPagerAdapter).getComponent(binding.pager2.currentItem)
    }

    private val courseCelebrationStatus: Unit
        get() {
            val courseStatusCall = courseApi.getCourseStatus(courseData.courseId)
            courseStatusCall.enqueue(object :
                ErrorHandlingCallback<CourseStatus?>(this, null, null) {
                override fun onResponse(responseBody: CourseStatus) {
                    isFirstSection = responseBody.celebrationStatus.firstSection
                }
            })
        }

    private fun showCelebrationModal(reCreate: Boolean) {
        val celebrationDialog = CelebratoryModalDialogFragment.newInstance(object :
            CelebratoryModalDialogFragment.CelebratoryModelCallback {
            override fun onKeepGoing() {
                EventBus.getDefault().postSticky(VideoPlaybackEvent(false))
            }

            override fun onCelebrationShare(anchor: View) {
                courseData.courseId.let { courseId ->
                    ShareUtils.showCelebrationShareMenu(
                        this@NewCourseUnitNavigationActivity,
                        anchor,
                        courseData
                    ) { shareType: ShareUtils.ShareType ->
                        environment.analyticsRegistry.trackCourseCelebrationShareClicked(
                            courseId,
                            shareType.utmParamKey
                        )
                    }
                }

            }

            override fun celebratoryModalViewed() {
                EventBus.getDefault().postSticky(VideoPlaybackEvent(true))
                if (!reCreate) {
                    courseApi.updateCourseCelebration(courseData.courseId).enqueue(object :
                        ErrorHandlingCallback<Void?>(this@NewCourseUnitNavigationActivity) {
                        override fun onResponse(responseBody: Void) {
                            isFirstSection = false
                        }
                    })
                    environment.analyticsRegistry.trackCourseSectionCelebration(courseData.courseId)
                }
            }
        })
        celebrationDialog.isCancelable = false
        celebrationDialog.show(supportFragmentManager, CelebratoryModalDialogFragment.TAG)
    }
}
