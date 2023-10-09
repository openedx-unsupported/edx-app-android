package org.edx.mobile.view.app_nav

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.R
import org.edx.mobile.base.BaseFragmentActivity
import org.edx.mobile.course.CourseAPI
import org.edx.mobile.databinding.ActivityCourseUnitNavigationBinding
import org.edx.mobile.databinding.LayoutUnitsDropDownBinding
import org.edx.mobile.deeplink.Screen
import org.edx.mobile.event.FileSelectionEvent
import org.edx.mobile.event.FileShareEvent
import org.edx.mobile.event.IAPFlowEvent
import org.edx.mobile.event.LogoutEvent
import org.edx.mobile.event.MyCoursesRefreshEvent
import org.edx.mobile.event.VideoPlaybackEvent
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.extenstion.CollapsingToolbarStatListener
import org.edx.mobile.extenstion.parcelable
import org.edx.mobile.extenstion.serializableOrThrow
import org.edx.mobile.extenstion.setTextWithIcon
import org.edx.mobile.extenstion.setTitleStateListener
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.http.callback.ErrorHandlingCallback
import org.edx.mobile.http.notifications.SnackbarErrorNotification
import org.edx.mobile.interfaces.OnItemClickListener
import org.edx.mobile.interfaces.RefreshListener
import org.edx.mobile.model.api.CourseUpgradeResponse
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.course.BlockType
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.model.course.CourseStatus
import org.edx.mobile.model.course.EnrollmentMode
import org.edx.mobile.model.course.IBlock
import org.edx.mobile.model.course.VideoBlockModel
import org.edx.mobile.model.iap.IAPFlowData
import org.edx.mobile.model.iap.IAPFlowData.IAPAction
import org.edx.mobile.services.CourseManager
import org.edx.mobile.util.AppConstants
import org.edx.mobile.util.BrowserUtil
import org.edx.mobile.util.NetworkUtil
import org.edx.mobile.util.NonNullObserver
import org.edx.mobile.util.UiUtils
import org.edx.mobile.util.VideoUtil
import org.edx.mobile.util.ViewAnimationUtil
import org.edx.mobile.util.images.ShareUtils
import org.edx.mobile.util.observer.Event
import org.edx.mobile.util.observer.EventObserver
import org.edx.mobile.view.CourseUnitFragment
import org.edx.mobile.view.Router
import org.edx.mobile.view.adapters.CourseUnitPagerAdapter
import org.edx.mobile.view.adapters.UnitsDropDownAdapter
import org.edx.mobile.view.custom.DividerItemDecorator
import org.edx.mobile.view.custom.PreLoadingListener
import org.edx.mobile.view.custom.error.EdxErrorState
import org.edx.mobile.view.dialog.CelebratoryModalDialogFragment
import org.edx.mobile.view.dialog.FullscreenLoaderDialogFragment
import org.edx.mobile.view.dialog.FullscreenLoaderDialogFragment.Companion.newInstance
import org.edx.mobile.viewModel.CourseViewModel
import org.edx.mobile.viewModel.InAppPurchasesViewModel
import org.edx.mobile.wrapper.InAppPurchasesDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.EnumSet
import javax.inject.Inject

@AndroidEntryPoint
class CourseUnitNavigationActivity : BaseFragmentActivity(), CourseUnitFragment.HasComponent,
    PreLoadingListener, RefreshListener {

    private lateinit var binding: ActivityCourseUnitNavigationBinding

    @Inject
    lateinit var courseManager: CourseManager

    @Inject
    lateinit var courseApi: CourseAPI

    @Inject
    lateinit var iapDialogs: InAppPurchasesDialog

    private val iapViewModel: InAppPurchasesViewModel by viewModels()

    private lateinit var courseData: EnrolledCoursesResponse
    private var courseComponentId: String? = null
    private var subsection: CourseComponent? = null
    private var resumedComponent: CourseComponent? = null
    private var courseUpgradeData: CourseUpgradeResponse? = null

    private lateinit var pagerAdapter: CourseUnitPagerAdapter
    private var isVideoMode = false
    private var viewPagerState = PreLoadingListener.State.DEFAULT
    private var isFirstSection = false
    private var refreshCourse = false

    private var fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            var files: Array<Uri>? = null
            val resultData = result.data
            if (result.resultCode == RESULT_OK && resultData != null) {
                val dataString = resultData.dataString
                val clipData = resultData.clipData
                if (clipData != null) {
                    files = emptyArray()
                    for (i in 0 until clipData.itemCount) {
                        val item = clipData.getItemAt(i)
                        files[i] = item.uri
                    }
                }
                // Executed when user select only one file.
                if (dataString != null) {
                    files = arrayOf(Uri.parse(dataString))
                }
                if (files != null) {
                    EventBus.getDefault().post(FileShareEvent(files))
                }
            }
        }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // Add result data into the intent to trigger the signal that `courseData` is updated after
            // the course was purchased from a locked component screen.
            if (refreshCourse) {
                val resultData = Intent()
                resultData.putExtra(AppConstants.COURSE_UPGRADED, true)
                setResult(RESULT_OK, resultData)
                refreshCourse = false
            }
            finish()
        }
    }

    private val courseCelebrationStatus: Unit
        get() {
            val courseStatusCall = courseApi.getCourseStatus(courseData.courseId)
            courseStatusCall.enqueue(object :
                ErrorHandlingCallback<CourseStatus>(this, null, null) {
                override fun onResponse(responseBody: CourseStatus) {
                    isFirstSection = responseBody.celebrationStatus.firstSection
                }
            })
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCourseUnitNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        restore(savedInstanceState ?: intent?.getBundleExtra(Router.EXTRA_BUNDLE))
        isVideoMode = intent?.extras?.getBoolean(Router.EXTRA_IS_VIDEOS_MODE, false) as Boolean
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        initViews()
    }

    private fun initViews() {
        if (courseComponentId == null && environment.loginPrefs.isUserLoggedIn.not()) {
            EventBus.getDefault().post(LogoutEvent())
            return
        }
        onLoadData()

        if (subsection?.children.isNullOrEmpty()) {
            showComponentNotSupportError()
            return
        }

        setupToolbar()
        initAdapter()
        // Enforce to intercept single scrolling direction
        UiUtils.enforceSingleScrollDirection(binding.pager2)
        if (!isVideoMode) {
            courseCelebrationStatus
        }
        binding.pager2.setVisibility(true)
        binding.stateLayout.root.setVisibility(false)

        binding.gotoPrev.setOnClickListener { navigatePreviousComponent() }
        binding.gotoNext.setOnClickListener { navigateNextComponent() }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(Router.EXTRA_COURSE_DATA, courseData)
        outState.putParcelable(Router.EXTRA_COURSE_UPGRADE_DATA, courseUpgradeData)
        outState.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restore(savedInstanceState)
        onLoadData()
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
            val component = courseManager.getComponentById(courseData.courseId, componentId)
            if (component?.isContainer == false) { // if the component is the leaf
                resumedComponent = component
                // CourseComponent#getAncestor(1) returns the current Subsection of a component
                component.getAncestor(1)
            } else {
                component
            }
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
        subsection?.let { subsection ->
            val unitList = subsection.getChildren(isVideoMode)
            val hasMultipleChildren = unitList.isNullOrEmpty().not() && unitList.size > 1
            subsection.firstIncompleteComponent?.let {
                setToolbarTitle(it, hasMultipleChildren)
                if (hasMultipleChildren) {
                    setupUnitsDropDown(unitList, it)
                }
                updateCompletionProgressBar(0, it.getChildren(isVideoMode).size)
            }

            ViewAnimationUtil.startAlphaAnimation(
                binding.collapsedToolbarTitle,
                View.INVISIBLE
            )
            setupToolbarListeners()
        }
    }

    private fun showComponentNotSupportError() {
        subsection?.let { subsection ->
            binding.apply {
                courseUnitNavBar.setVisibility(false)
                expandedToolbarLayout.setVisibility(false)
                val primaryColor = ContextCompat.getColor(
                    this@CourseUnitNavigationActivity,
                    R.color.primaryBaseColor
                )
                ivCollapsedBack.setColorFilter(primaryColor)
                ivCollapsedBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
                collapsedToolbarTitle.text = subsection.displayName
                collapsedToolbarTitle.setTextColor(primaryColor)
                collapsedToolbarLayout.setBackgroundResource(R.color.white)
                containerLayoutNotAvailable.root.setVisibility(true)
                containerLayoutNotAvailable.notAvailableMessage.setText(R.string.assessment_not_available)
                containerLayoutNotAvailable.notAvailableMessage2.setVisibility(false)
                containerLayoutNotAvailable.viewOnWebButton.setOnClickListener {
                    environment.analyticsRegistry.trackSubsectionViewOnWebTapped(
                        subsection.courseId,
                        subsection.blockId,
                        subsection.specialExamInfo != null
                    )
                    BrowserUtil.open(this@CourseUnitNavigationActivity, subsection.webUrl, false)
                }
            }
        }
    }

    private fun updateCompletionProgressBar(currentPosition: Int, size: Int) {
        binding.spbUnits.setDivisions(size)
        binding.spbUnits.setDividerEnabled(true)
        binding.spbUnits.setEnabledDivisions(listOf(currentPosition))
    }

    private fun setupToolbarListeners() {
        binding.appbar.setTitleStateListener(
            binding.collapsingToolbarLayout,
            object : CollapsingToolbarStatListener {
                override fun onExpanded() {
                    ViewAnimationUtil.startAlphaAnimation(
                        binding.collapsedToolbarTitle,
                        View.INVISIBLE
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
                        View.INVISIBLE
                    )
                }

            })
        binding.ivCollapsedBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setToolbarTitle(courseUnit: CourseComponent, hasMoreThenOneChild: Boolean) {
        // Sub section title
        binding.courseSubSectionTitle.text = courseUnit.parent.displayName
        var unitTitle = courseUnit.displayName
        if (hasMoreThenOneChild) {
            unitTitle += "  " + AppConstants.ICON_PLACEHOLDER
        }
        binding.collapsedToolbarTitle.text = courseUnit.displayName
        // first incomplete unit title
        val dropdownIcon =
            UiUtils.getDrawable(this, R.drawable.ic_tip_down, 0, R.color.neutralWhiteT)
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
                UnitsDropDownAdapter(
                    courseData.isUpgradeable,
                    units,
                    object : OnItemClickListener<IBlock> {
                        override fun onItemClick(item: IBlock) {
                            setToolbarTitle(item as CourseComponent, true)
                            var index = 0
                            item.firstIncompleteComponent?.let { component ->
                                index = (binding.pager2.adapter as CourseUnitPagerAdapter)
                                    .getComponentIndex(component)
                            }
                            binding.pager2.currentItem = index
                            popupWindow.dismiss()
                        }
                    })
            popupViewBinding.rvUnits.apply {
                this.adapter = adapter
                this.addItemDecoration(
                    DividerItemDecorator(
                        UiUtils.getDrawable(
                            applicationContext,
                            R.drawable.list_item_divider
                        )
                    )
                )
            }
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
            logger.warn("selected section is null?")
            return  //should not happen
        }
        val componentList = getComponentList()
        pagerAdapter =
            CourseUnitPagerAdapter(
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
                updateUIForOrientation()
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        val selectedComponent: CourseComponent? = if (resumedComponent != null) {
            resumedComponent
        } else {
            if (isVideoMode) {
                subsection?.firstIncompleteVideoComponent?.firstIncompleteVideoComponent
            } else {
                subsection?.firstIncompleteComponent?.firstIncompleteComponent
            }
        }
        val firstIncompleteComponentIndex = componentList.indexOf(selectedComponent)
        binding.pager2.setCurrentItem(firstIncompleteComponentIndex, false)
    }

    private fun updateCurrentComponent(currentComponent: CourseComponent) {
        // CourseComponent#getAncestor(1) returns the current Subsection of a component
        val currentSubSection = currentComponent.getAncestor(1)
        val currentUnit = currentComponent.parent
        setToolbarTitle(currentUnit, currentSubSection.getChildren(isVideoMode).size > 1)
        if (subsection?.id.equals(currentSubSection.id).not()) {
            subsection = currentSubSection
        }
        subsection?.getChildren(isVideoMode)?.let {
            if (it.size > 1) {
                setupUnitsDropDown(it, currentUnit)
            }
        }
        val unitComponents = currentUnit.getChildren(isVideoMode)
        val currentComponentIndex = unitComponents.indexOf(currentComponent)
        updateCompletionProgressBar(currentComponentIndex, unitComponents.size)
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
         */
        if (!isVideoMode && isFirstSection && currentBlockSection != nextBlockSection) {
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
        refreshCourse = true
        courseData.mode = EnrollmentMode.VERIFIED.toString()
        updateCourseStructure(courseId, componentId)
    }

    override fun initializeIAPObserver() {
        // The shared observer is used to monitor and handle any errors that may occur during the
        // ‘refreshCourseData’ method, which is called as part of the refresh flow. This observer
        // invokes the ‘updateCourseStructure’ method.
        iapViewModel.errorMessage.observe(
            this,
            NonNullObserver { errorMessageEvent: Event<ErrorMessage> ->
                if (errorMessageEvent.peekContent().requestType == ErrorMessage.COURSE_REFRESH_CODE) {
                    val errorMessage = errorMessageEvent.getContentIfNotConsumed()
                    val fullScreenLoader = FullscreenLoaderDialogFragment.getRetainedInstance(
                        supportFragmentManager
                    )
                    if (fullScreenLoader == null || errorMessage == null) {
                        return@NonNullObserver
                    }
                    iapDialogs.handleIAPException(fullScreenLoader, errorMessage, { _, _ ->
                        updateCourseStructure(
                            courseData.courseId, courseComponentId
                        )
                    }) { _, _ ->
                        iapViewModel.iapFlowData.clear()
                        fullScreenLoader.dismiss()
                    }
                }
            })
    }

    /**
     * Method to force update the course structure from server.
     */
    private fun updateCourseStructure(courseId: String?, componentId: String?) {
        if (!environment.loginPrefs.isUserLoggedIn) {
            EventBus.getDefault().post(LogoutEvent())
            return
        }
        val courseViewModel = ViewModelProvider(this)[CourseViewModel::class.java]

        courseViewModel.courseComponent.observe(
            this,
            EventObserver { courseComponent: CourseComponent ->
                // Check if the Course structure is updated from a specific component
                // so need to set the courseComponentId to that specific component
                // as after update app needs to show the updated content for that component.
                courseComponentId = componentId ?: courseComponent.id
                invalidateOptionsMenu()
                onLoadData()
                initAdapter()

                val fullScreenLoader =
                    FullscreenLoaderDialogFragment.getRetainedInstance(supportFragmentManager)
                if (fullScreenLoader != null && fullScreenLoader.isResumed) {
                    SnackbarErrorNotification(binding.pager2).showUpgradeSuccessSnackbar(
                        R.string.purchase_success_message
                    )
                    fullScreenLoader.closeLoader(null)
                }
            })

        courseViewModel.handleError.observe(this) { throwable: Throwable ->
            binding.stateLayout.root.setVisibility(true)
            binding.pager2.setVisibility(false)
            binding.stateLayout.state.setState(
                EdxErrorState.State.NETWORK,
                Screen.COURSE_COMPONENT
            )
            binding.stateLayout.state.setActionListener {
                if (NetworkUtil.isConnected(this@CourseUnitNavigationActivity)) {
                    onRefresh()
                }
            }
            onCourseRefreshError(throwable)
        }

        courseId?.let {
            courseViewModel.getCourseData(
                courseId = courseId,
                courseComponentId = null,
                showProgress = false,
                swipeRefresh = false,
                coursesRequestType = CourseViewModel.CoursesRequestType.LIVE
            )
        }
    }

    private fun onCourseRefreshError(error: Throwable) {
        val fullScreenLoader =
            FullscreenLoaderDialogFragment.getRetainedInstance(supportFragmentManager)
        if (fullScreenLoader?.isResumed == true) {
            iapViewModel.dispatchError(ErrorMessage.COURSE_REFRESH_CODE, null, error)
        }
    }

    override fun setLoadingState(newState: PreLoadingListener.State) {
        viewPagerState = newState
    }

    override fun isMainUnitLoaded(): Boolean {
        return viewPagerState == PreLoadingListener.State.MAIN_UNIT_LOADED
    }

    private fun updateUIForOrientation() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
            getCurrentComponent()?.isVideoBlock == true
        ) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, window.decorView).apply {
                hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            }
            val layoutParams = binding.appbar.layoutParams
            layoutParams.height = 0
            binding.appbar.layoutParams = layoutParams
            binding.courseUnitNavBar.setVisibility(false)
        } else {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowInsetsControllerCompat(window, window.decorView)
                .show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            val layoutParams = binding.appbar.layoutParams
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            binding.appbar.layoutParams = layoutParams
            binding.courseUnitNavBar.setVisibility(subsection?.children.isNullOrEmpty().not())
        }
    }

    private fun getCurrentComponent(): CourseComponent? {
        if (binding.pager2.adapter is CourseUnitPagerAdapter) {
            return (binding.pager2.adapter as CourseUnitPagerAdapter).getComponent(binding.pager2.currentItem)
        }
        return null
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
                        this@CourseUnitNavigationActivity,
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
                        ErrorHandlingCallback<Void?>(this@CourseUnitNavigationActivity) {
                        override fun onResponse(responseBody: Void?) {
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

    override fun onRefresh() {
        initViews()
    }

    override fun showGoogleCastButton(): Boolean {
        val component = getCurrentComponent()
        return if (config.isChromeCastEnabled && component is VideoBlockModel) {
            // Showing casting button only for native video block
            // Currently casting for youtube video isn't available
            VideoUtil.isCourseUnitVideo(environment, component)
        } else super.showGoogleCastButton()
    }

    private fun showFullscreenLoader(iapFlowData: IAPFlowData) {
        // To proceed with the same instance of dialog fragment in case of orientation change
        var fullScreenLoader =
            FullscreenLoaderDialogFragment.getRetainedInstance(supportFragmentManager)
        if (fullScreenLoader == null) {
            fullScreenLoader = newInstance(iapFlowData)
        }
        fullScreenLoader.show(supportFragmentManager, FullscreenLoaderDialogFragment.TAG)
    }

    @Subscribe
    fun onEventMainThread(event: IAPFlowEvent) {
        if (!isInForeground) {
            return
        }
        when (event.flowAction) {
            IAPAction.SHOW_FULL_SCREEN_LOADER -> event.iapFlowData?.let { showFullscreenLoader(it) }
            IAPAction.PURCHASE_FLOW_COMPLETE -> EventBus.getDefault().post(MyCoursesRefreshEvent())
        }
    }

    @Subscribe
    fun onEvent(event: FileSelectionEvent) {
        fileChooserLauncher.launch(event.intent)
    }
}
