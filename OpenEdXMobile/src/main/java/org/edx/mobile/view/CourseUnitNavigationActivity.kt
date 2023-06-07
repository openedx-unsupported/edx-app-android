package org.edx.mobile.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.R
import org.edx.mobile.base.BaseFragmentActivity
import org.edx.mobile.course.CourseAPI
import org.edx.mobile.databinding.ActivityCourseUnitNavigationBinding
import org.edx.mobile.event.CourseUpgradedEvent
import org.edx.mobile.event.FileSelectionEvent
import org.edx.mobile.event.FileShareEvent
import org.edx.mobile.event.IAPFlowEvent
import org.edx.mobile.event.LogoutEvent
import org.edx.mobile.event.MyCoursesRefreshEvent
import org.edx.mobile.event.VideoPlaybackEvent
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.extenstion.parcelable
import org.edx.mobile.extenstion.serializable
import org.edx.mobile.http.callback.ErrorHandlingCallback
import org.edx.mobile.http.notifications.FullScreenErrorNotification
import org.edx.mobile.http.notifications.SnackbarErrorNotification
import org.edx.mobile.interfaces.RefreshListener
import org.edx.mobile.model.api.CourseUpgradeResponse
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.course.BlockType
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.model.course.CourseStatus
import org.edx.mobile.model.course.VideoBlockModel
import org.edx.mobile.model.iap.IAPFlowData
import org.edx.mobile.model.iap.IAPFlowData.IAPAction
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.services.CourseManager
import org.edx.mobile.util.AppConstants
import org.edx.mobile.util.NetworkUtil
import org.edx.mobile.util.NonNullObserver
import org.edx.mobile.util.UiUtils.enforceSingleScrollDirection
import org.edx.mobile.util.VideoUtil
import org.edx.mobile.util.images.ShareUtils
import org.edx.mobile.util.images.ShareUtils.ShareType
import org.edx.mobile.util.observer.Event
import org.edx.mobile.util.observer.EventObserver
import org.edx.mobile.view.CourseUnitFragment.HasComponent
import org.edx.mobile.view.adapters.CourseUnitPagerAdapter
import org.edx.mobile.view.common.MessageType
import org.edx.mobile.view.common.TaskProcessCallback
import org.edx.mobile.view.custom.PreLoadingListener
import org.edx.mobile.view.dialog.CelebratoryModalDialogFragment
import org.edx.mobile.view.dialog.CelebratoryModalDialogFragment.CelebratoryModelCallback
import org.edx.mobile.view.dialog.CelebratoryModalDialogFragment.Companion.newInstance
import org.edx.mobile.view.dialog.FullscreenLoaderDialogFragment
import org.edx.mobile.viewModel.CourseViewModel
import org.edx.mobile.viewModel.InAppPurchasesViewModel
import org.edx.mobile.wrapper.InAppPurchasesDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.EnumSet
import javax.inject.Inject


@AndroidEntryPoint
class CourseUnitNavigationActivity : BaseFragmentActivity(), HasComponent, PreLoadingListener,
    TaskProcessCallback, RefreshListener {

    @Inject
    lateinit var courseApi: CourseAPI

    @Inject
    lateinit var iapDialogs: InAppPurchasesDialog

    @Inject
    lateinit var courseManager: CourseManager

    private val iapViewModel: InAppPurchasesViewModel by viewModels()

    private lateinit var binding: ActivityCourseUnitNavigationBinding
    private lateinit var pagerAdapter: CourseUnitPagerAdapter

    override var component: CourseComponent? = null
    private var courseData: EnrolledCoursesResponse? = null
    private var courseUpgradeData: CourseUpgradeResponse? = null
    private var courseComponentId: String? = null
    private val unitList: MutableList<CourseComponent> = ArrayList()
    private var errorNotification: FullScreenErrorNotification? = null

    private var viewPagerState = PreLoadingListener.State.DEFAULT
    private var isFirstSection = false
    private var isVideoMode = false
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCourseUnitNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        errorNotification = FullScreenErrorNotification(binding.contentArea)

        var bundle = savedInstanceState
        if (bundle == null) {
            if (intent != null) bundle = intent.getBundleExtra(Router.EXTRA_BUNDLE)
        }
        restore(bundle)
        super.setToolbarAsActionBar()

        initAdapter()
        // Enforce to intercept single scrolling direction
        enforceSingleScrollDirection(binding.pager2)
        binding.courseUnitNavBar.visibility = View.VISIBLE
        binding.gotoPrev.setOnClickListener { navigatePreviousComponent() }
        binding.gotoNext.setOnClickListener { navigateNextComponent() }
        if (intent != null) {
            isVideoMode = intent.extras?.getBoolean(Router.EXTRA_IS_VIDEOS_MODE, false) as Boolean
        }
        if (!isVideoMode) {
            courseCelebrationStatus
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
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

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // If the data is available then trigger the callback
        // after basic initialization
        if (courseComponentId != null && environment.loginPrefs.isUserLoggedIn) {
            onLoadData()
        }
    }

    private fun initAdapter() {
        pagerAdapter = CourseUnitPagerAdapter(
            this, environment,
            unitList, courseData, courseUpgradeData, this
        )
        binding.pager2.adapter = pagerAdapter
        binding.pager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                invalidateOptionsMenu()
            }

            override fun onPageSelected(position: Int) {
                if (pagerAdapter.getUnit(position).isMultiDevice) {
                    // Disable ViewPager2 scrolling to enable horizontal scrolling to for the WebView (Specific HTML Components).
                    val horizontalBlocks = listOf(
                        BlockType.DRAG_AND_DROP_V2,
                        BlockType.LTI_CONSUMER,
                        BlockType.WORD_CLOUD
                    )
                    binding.pager2.isUserInputEnabled = !horizontalBlocks
                        .contains(pagerAdapter.getUnit(position).type)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    tryToUpdateForEndOfSequential()
                }
            }
        })
    }

    private val courseCelebrationStatus: Unit
        get() {
            courseData?.courseId?.let { courseId ->
                val courseStatusCall = courseApi.getCourseStatus(courseId)
                courseStatusCall.enqueue(object :
                    ErrorHandlingCallback<CourseStatus?>(this, null, null) {
                    override fun onResponse(responseBody: CourseStatus) {
                        isFirstSection = responseBody.celebrationStatus.firstSection
                    }
                })
            }
        }

    private fun showCelebrationModal(reCreate: Boolean) {
        val celebrationDialog = newInstance(object : CelebratoryModelCallback {
            override fun onKeepGoing() {
                EventBus.getDefault().postSticky(VideoPlaybackEvent(false))
            }

            override fun onCelebrationShare(anchor: View) {
                courseData?.courseId?.let { courseId ->
                    ShareUtils.showCelebrationShareMenu(
                        this@CourseUnitNavigationActivity,
                        anchor,
                        courseData
                    ) { shareType: ShareType ->
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
                    courseData?.courseId?.let { courseId ->
                        courseApi.updateCourseCelebration(courseId).enqueue(object :
                            ErrorHandlingCallback<Void?>(this@CourseUnitNavigationActivity) {
                            override fun onResponse(responseBody: Void) {
                                isFirstSection = false
                            }
                        })
                        environment.analyticsRegistry.trackCourseSectionCelebration(courseId)
                    }
                }
            }
        })
        celebrationDialog.isCancelable = false
        celebrationDialog.show(supportFragmentManager, CelebratoryModalDialogFragment.TAG)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        /*
         * If the youtube player is not in a proper state then it throws the IllegalStateException.
         * To avoid the crash and continue the flow we are catching the exception.
         *
         * It may occur when the edX app was in background and user kills the on-device YouTube app.
         */
        try {
            super.onSaveInstanceState(outState)
            outState.putSerializable(Router.EXTRA_COURSE_DATA, courseData)
            outState.putParcelable(Router.EXTRA_COURSE_UPGRADE_DATA, courseUpgradeData)
            outState.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId)
        } catch (e: IllegalStateException) {
            logger.error(e)
        }
    }

    private fun restore(bundle: Bundle?) {
        bundle?.let {
            courseData =
                bundle.serializable(Router.EXTRA_COURSE_DATA) as EnrolledCoursesResponse?
            courseUpgradeData = bundle.parcelable(Router.EXTRA_COURSE_UPGRADE_DATA)
            courseComponentId = bundle.getString(Router.EXTRA_COURSE_COMPONENT_ID)
        }
        if (courseComponentId == null) {
            updateCourseStructure(courseData?.course?.id, null)
        }
    }

    override fun onOffline() {
        hideLoadingProgress()
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

    override fun navigateNextComponent() {
        if (pagerAdapter.itemCount == 0) {
            return
        }
        val index = binding.pager2.currentItem
        if (index < pagerAdapter.itemCount - 1) {
            binding.pager2.currentItem = index + 1
        }
        // CourseComponent#getAncestor(2) returns the section of a component
        val currentBlockSection = component?.getAncestor(2)
        val nextBlockSection = pagerAdapter.getUnit(binding.pager2.currentItem).getAncestor(2)
        /*
         * Show celebratory modal when:
         * 1. We haven't arrived at component navigation from the Videos tab.
         * 2. The current section is the first section being completed (not necessarily the actual first section of the course).
         * 3. Section of the current and next components are different.
         */if (!isVideoMode && isFirstSection && currentBlockSection != nextBlockSection) {
            showCelebrationModal(false)
        }
    }

    override fun refreshCourseData(courseId: String, componentId: String) {
        refreshCourse = true
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
                            courseData?.courseId, courseComponentId
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
            })
        courseViewModel.handleError.observe(this) { throwable: Throwable ->
            errorNotification?.showError(
                this@CourseUnitNavigationActivity,
                throwable,
                R.string.lbl_reload
            ) {
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

    @Suppress("DEPRECATION")
    private fun onLoadData() {
        component =
            courseData?.courseId?.let { courseId ->
                courseComponentId?.let { componentId ->
                    courseManager.getComponentById(courseId, componentId)
                }
            }
        updateDataModel()
        val fullScreenLoader =
            FullscreenLoaderDialogFragment.getRetainedInstance(supportFragmentManager)
        if (fullScreenLoader?.isResumed == true) {
            SnackbarErrorNotification(binding.pager2)
                .showUpgradeSuccessSnackbar(R.string.purchase_success_message)
            fullScreenLoader.closeLoader(null)
        }
    }

    private fun onCourseRefreshError(error: Throwable) {
        val fullScreenLoader =
            FullscreenLoaderDialogFragment.getRetainedInstance(supportFragmentManager)
        if (fullScreenLoader?.isResumed == true) {
            iapViewModel.dispatchError(ErrorMessage.COURSE_REFRESH_CODE, null, error)
        }
    }

    private fun setCurrentUnit(component: CourseComponent) {
        this.component = component
        if (this.component == null) {
            return
        }
        courseComponentId = component.id
        environment.database.updateAccess(null, component.id, true)
        updateUIForOrientation()
        val resultData = Intent()
        resultData.putExtra(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId)
        setResult(RESULT_OK, resultData)
        environment.analyticsRegistry.trackScreenView(
            Analytics.Screens.UNIT_DETAIL, courseData?.course?.id, component.internalName
        )
        environment.analyticsRegistry.trackCourseComponentViewed(
            component.id,
            courseData?.course?.id,
            component.blockId
        )
    }

    private fun tryToUpdateForEndOfSequential() {
        if (pagerAdapter.itemCount == 0) {
            return
        }
        val curIndex = binding.pager2.currentItem
        setCurrentUnit(pagerAdapter.getUnit(curIndex))
        binding.gotoPrev.isEnabled = curIndex > 0
        binding.gotoNext.isEnabled = curIndex < pagerAdapter.itemCount - 1
        binding.courseUnitNavBar.requestLayout()
        title = component?.displayName
        val currentSubsectionId = component?.parent?.id
        if (curIndex + 1 <= pagerAdapter.itemCount - 1) {
            val nextUnitSubsectionId = unitList[curIndex + 1].parent.id
            if (currentSubsectionId.equals(nextUnitSubsectionId, ignoreCase = true)) {
                binding.nextUnitTitle.visibility = View.GONE
            } else {
                binding.nextUnitTitle.text = unitList[curIndex + 1].parent.displayName
                binding.nextUnitTitle.visibility = View.VISIBLE
            }
        } else {
            // we have reached the end and next button is disabled
            binding.nextUnitTitle.visibility = View.GONE
        }
        if (curIndex - 1 >= 0) {
            val prevUnitSubsectionId = unitList[curIndex - 1].parent.id
            if (currentSubsectionId.equals(prevUnitSubsectionId, ignoreCase = true)) {
                binding.prevUnitTitle.visibility = View.GONE
            } else {
                binding.prevUnitTitle.text = unitList[curIndex - 1].parent.displayName
                binding.prevUnitTitle.visibility = View.VISIBLE
            }
        } else {
            // we have reached the start and previous button is disabled
            binding.prevUnitTitle.visibility = View.GONE
        }
        if (binding.gotoPrev.isEnabled) {
            binding.gotoPrev.typeface =
                ResourcesCompat.getFont(this, R.font.inter_semi_bold)
        } else {
            binding.gotoPrev.typeface = ResourcesCompat.getFont(this, R.font.inter_regular)
        }
        if (binding.gotoNext.isEnabled) {
            binding.gotoNext.typeface =
                ResourcesCompat.getFont(this, R.font.inter_semi_bold)
        } else {
            binding.gotoNext.typeface = ResourcesCompat.getFont(this, R.font.inter_regular)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateDataModel() {
        unitList.clear()
        if (component?.root == null) {
            logger.warn("selectedUnit is null?")
            return  //should not happen
        }

        //if we want to navigate through all unit of within the parent node,
        //we should use courseComponent instead.   Requirement maybe changed?
        // unitList.addAll( courseComponent.getChildLeafs() );
        var leaves: List<CourseComponent>? = ArrayList()
        if (isVideoMode) {
            leaves = component?.root?.getVideos(false)
        } else {
            component?.root?.fetchAllLeafComponents(leaves, EnumSet.allOf(BlockType::class.java))
        }
        leaves?.let { unitList.addAll(it) }
        val index = unitList.indexOf(component)
        if (refreshCourse) {
            initAdapter()
        }
        pagerAdapter.notifyDataSetChanged()
        if (index >= 0) {
            binding.pager2.setCurrentItem(index, false)
            tryToUpdateForEndOfSequential()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateUIForOrientation()
        if (component != null) {
            environment.analyticsRegistry.trackCourseComponentViewed(
                component?.id,
                courseData?.courseId, component?.blockId
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Manually handle backPress button on toolbar
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * This function shows the loading progress wheel
     * Show progress wheel while loading the web page
     */
    private fun showLoadingProgress() {
        binding.loadingIndicator.loadingIndicator.visibility = View.VISIBLE
    }

    /**
     * This function hides the loading progress wheel
     * Hide progress wheel after the web page completes loading
     */
    private fun hideLoadingProgress() {
        binding.loadingIndicator.loadingIndicator.visibility = View.GONE
    }

    /**
     * implements TaskProcessCallback
     */
    override fun startProcess() {
        showLoadingProgress()
    }

    /**
     * implements TaskProcessCallback
     */
    override fun finishProcess() {
        hideLoadingProgress()
    }

    override fun onMessage(messageType: MessageType, message: String) {
        showErrorMessage("", message)
    }

    @Suppress("DEPRECATION")
    private val isOnCourseOutline: Boolean
        get() {
            courseComponentId?.let { courseComponentId ->
                courseData?.courseId?.let { courseId ->
                    courseManager.getComponentById(courseId, courseComponentId)
                }?.let { outlineComponent ->
                    val outlinePath = outlineComponent.path
                    val outlinePathSize = outlinePath.path.size
                    return outlinePathSize <= 1
                }
                return false
            } ?: run {
                return true
            }
        }

    override fun onRefresh() {
        errorNotification?.hideError()
        if (!environment.loginPrefs.isUserLoggedIn) {
            EventBus.getDefault().post(LogoutEvent())
            return
        }
        if (isOnCourseOutline) {
            intent?.let {
                restore(intent.getBundleExtra(Router.EXTRA_BUNDLE))
            }
        } else {
            onLoadData()
        }
    }

    private fun updateUIForOrientation() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
            VideoUtil.isCourseUnitVideo(environment, component)
        ) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, window.decorView).apply {
                hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            }
            setActionBarVisible(false)
            binding.courseUnitNavBar.visibility = View.GONE
        } else {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowInsetsControllerCompat(window, window.decorView)
                .show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            setActionBarVisible(true)
            binding.courseUnitNavBar.visibility = View.VISIBLE
        }
    }

    override fun setLoadingState(newState: PreLoadingListener.State) {
        viewPagerState = newState
    }

    override fun isMainUnitLoaded(): Boolean {
        return viewPagerState == PreLoadingListener.State.MAIN_UNIT_LOADED
    }

    override fun showGoogleCastButton(): Boolean {
        return if (component != null && component is VideoBlockModel) {
            // Showing casting button only for native video block
            // Currently casting for youtube video isn't available
            VideoUtil.isCourseUnitVideo(environment, component)
        } else super.showGoogleCastButton()
    }

    private fun showFullscreenLoader(iapFlowData: IAPFlowData?) {
        iapFlowData?.let {
            // To proceed with the same instance of dialog fragment in case of orientation change
            var fullScreenLoader =
                FullscreenLoaderDialogFragment.getRetainedInstance(supportFragmentManager)
            if (fullScreenLoader == null) {
                fullScreenLoader = FullscreenLoaderDialogFragment.newInstance(iapFlowData)
            }
            fullScreenLoader.show(supportFragmentManager, FullscreenLoaderDialogFragment.TAG)
        }
    }

    @Subscribe
    fun onEventMainThread(event: IAPFlowEvent) {
        if (!isInForeground) {
            return
        }
        when (event.flowAction) {
            IAPAction.SHOW_FULL_SCREEN_LOADER -> {
                showFullscreenLoader(event.iapFlowData)
            }

            IAPAction.PURCHASE_FLOW_COMPLETE -> {
                EventBus.getDefault().post(MyCoursesRefreshEvent())
            }
        }
    }

    @Subscribe
    fun onEvent(event: FileSelectionEvent) {
        fileChooserLauncher.launch(event.intent)
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onEvent(event: CourseUpgradedEvent?) {
        finish()
    }
}
