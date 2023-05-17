package org.edx.mobile.view

import android.content.DialogInterface
import android.os.Bundle
import android.provider.CalendarContract
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.android.billingclient.api.SkuDetails
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.R
import org.edx.mobile.base.BaseFragment
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.course.CourseAPI
import org.edx.mobile.course.CourseAPI.GetCourseByIdCallback
import org.edx.mobile.databinding.FragmentCourseTabsDashboardBinding
import org.edx.mobile.databinding.FragmentDashboardErrorLayoutBinding
import org.edx.mobile.deeplink.Screen
import org.edx.mobile.deeplink.ScreenDef
import org.edx.mobile.event.IAPFlowEvent
import org.edx.mobile.event.MainDashboardRefreshEvent
import org.edx.mobile.event.MoveToDiscoveryTabEvent
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.http.notifications.SnackbarErrorNotification
import org.edx.mobile.logger.Logger
import org.edx.mobile.model.CourseDatesCalendarSync
import org.edx.mobile.model.FragmentItemModel
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.course.CourseBannerType.RESET_DATES
import org.edx.mobile.model.course.EnrollmentMode
import org.edx.mobile.model.iap.IAPFlowData
import org.edx.mobile.model.iap.IAPFlowData.IAPFlowType
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.module.analytics.AnalyticsRegistry
import org.edx.mobile.module.analytics.InAppPurchasesAnalytics
import org.edx.mobile.util.AppConstants
import org.edx.mobile.util.CalendarUtils
import org.edx.mobile.util.CalendarUtils.createOrUpdateCalendar
import org.edx.mobile.util.CalendarUtils.deleteCalendar
import org.edx.mobile.util.CalendarUtils.isCalendarOutOfDate
import org.edx.mobile.util.ConfigUtil.Companion.checkCalendarSyncEnabled
import org.edx.mobile.util.ConfigUtil.OnCalendarSyncListener
import org.edx.mobile.util.CourseDateUtil
import org.edx.mobile.util.DateUtil
import org.edx.mobile.util.InAppPurchasesException
import org.edx.mobile.util.NonNullObserver
import org.edx.mobile.util.ResourceUtil
import org.edx.mobile.util.TextUtils.setIconifiedText
import org.edx.mobile.util.UiUtils.enforceSingleScrollDirection
import org.edx.mobile.util.UiUtils.restartFragment
import org.edx.mobile.util.ViewAnimationUtil
import org.edx.mobile.util.images.CourseCardUtils
import org.edx.mobile.util.images.ShareUtils
import org.edx.mobile.util.observer.EventObserver
import org.edx.mobile.view.adapters.FragmentItemPagerAdapter
import org.edx.mobile.view.custom.error.EdxCourseAccessErrorState.State
import org.edx.mobile.view.custom.error.EdxErrorState
import org.edx.mobile.view.dialog.AlertDialogFragment
import org.edx.mobile.view.dialog.CourseModalDialogFragment
import org.edx.mobile.view.dialog.CourseModalDialogFragment.Companion.newInstance
import org.edx.mobile.view.dialog.FullscreenLoaderDialogFragment
import org.edx.mobile.view.dialog.FullscreenLoaderDialogFragment.Companion.getRetainedInstance
import org.edx.mobile.view.dialog.FullscreenLoaderDialogFragment.Companion.newInstance
import org.edx.mobile.view.dialog.FullscreenLoaderDialogFragment.FullScreenDismissListener
import org.edx.mobile.viewModel.CourseDateViewModel
import org.edx.mobile.viewModel.InAppPurchasesViewModel
import org.edx.mobile.wrapper.InAppPurchasesDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class CourseTabsDashboardFragment : BaseFragment() {
    private val logger = Logger(javaClass.name)

    @Inject
    lateinit var environment: IEdxEnvironment

    @Inject
    lateinit var analyticsRegistry: AnalyticsRegistry

    @Inject
    lateinit var courseApi: CourseAPI

    @Inject
    lateinit var iapAnalytics: InAppPurchasesAnalytics

    @Inject
    lateinit var iapDialog: InAppPurchasesDialog

    private lateinit var binding: FragmentCourseTabsDashboardBinding

    private lateinit var courseData: EnrolledCoursesResponse

    private val iapViewModel: InAppPurchasesViewModel by viewModels()

    private val courseDateViewModel: CourseDateViewModel by viewModels()

    private val loaderDialog: AlertDialogFragment by lazy {
        AlertDialogFragment.newInstance(
            R.string.title_syncing_calendar, R.layout.alert_dialog_progress
        )
    }

    private val accountName: String by lazy {
        CalendarUtils.getUserAccountForSync(environment)
    }

    private val calendarTitle: String by lazy {
        CalendarUtils.getCourseCalendarTitle(environment, courseData.course.name)
    }

    private var isCollapsedTitleVisible = false

    private var isExpandedTitleVisible = true

    private val fullscreenLoader: FullscreenLoaderDialogFragment?
        get() = getRetainedInstance(childFragmentManager)

    private val courseTabItems: List<FragmentItemModel>
        get() {
            val screenName = arguments?.getString(Router.EXTRA_SCREEN_NAME)
            val items = mutableListOf<FragmentItemModel>()

            items.add(createCourseOutlineItem(screenName))
            if (environment.config.isCourseVideosEnabled) {
                items.add(createVideosItem())
            }
            if (environment.config.isDiscussionsEnabled &&
                courseData.course.discussionUrl?.isNotEmpty() == true
            ) {
                items.add(createDiscussionsItem())
            }
            if (environment.config.isCourseDatesEnabled) {
                items.add(createDatesItem())
            }
            items.add(createHandoutsItem())
            items.add(createAnnouncementsItem())

            return items
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (arguments?.getSerializable(Router.EXTRA_COURSE_DATA) == null) {
            return if (arguments?.getBoolean(ARG_COURSE_NOT_FOUND) == true) {
                // The case where we have invalid course data
                binding.loadingError.apply {
                    root.setVisibility(true)
                    dismiss.setVisibility(true)
                    dismiss.setOnClickListener(onCloseClick())
                    state.setState(EdxErrorState.State.LOAD_ERROR, Screen.COURSE_DASHBOARD)
                    state.setActionListener(onCloseClick())
                }
                binding.root
            } else {
                // The case where we need to fetch course's data based on its courseId
                fetchCourseById()
                FrameLayout(requireActivity()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    addView(inflater.inflate(R.layout.loading_indicator, container, false).apply {
                        setVisibility(true)
                    })
                }
            }
        }

        binding = FragmentCourseTabsDashboardBinding.inflate(inflater, container, false)
        courseData = arguments?.getSerializable(Router.EXTRA_COURSE_DATA) as EnrolledCoursesResponse

        setHasOptionsMenu(courseData.course.coursewareAccess.hasAccess())

        if (!courseData.course.coursewareAccess.hasAccess()) {
            setupToolbar(false)
            if (courseData.isAuditAccessExpired) {
                if (courseData.isUpgradeable && environment.appFeaturesPrefs.isValuePropEnabled()) {
                    setupIAPLayout()
                } else {
                    binding.accessError.apply {
                        setVisibility(true)
                        setState(State.AUDIT_ACCESS_EXPIRED, null)
                        setPrimaryButtonListener(onFindCourseClick())
                    }
                }
            } else if (!courseData.course.isStarted) {
                binding.accessError.apply {
                    setVisibility(true)
                    setState(
                        State.NOT_STARTED,
                        DateUtil.formatCourseNotStartedDate(courseData.course.start)
                    )
                    setPrimaryButtonListener(onFindCourseClick())
                }
            } else {
                //Todo Remove when Next Session Enrollment feature is added
                val errorLayoutBinding =
                    FragmentDashboardErrorLayoutBinding.inflate(inflater, container, false)
                errorLayoutBinding.errorMsg.setText(R.string.course_not_started)
                return errorLayoutBinding.root
            }
        } else {
            setupToolbar(true)
            setViewPager()
            initCourseDateObserver()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTabSelection(requireArguments())

        environment.analyticsRegistry.trackScreenView(
            Analytics.Screens.COURSE_DASHBOARD, courseData.course.id, null
        )
    }

    private fun setupIAPLayout() {
        val isPurchaseEnabled = environment.appFeaturesPrefs.isIAPEnabled(
            environment.loginPrefs.isOddUserId
        )
        binding.accessError.apply {
            setVisibility(true)
            setState(State.IS_UPGRADEABLE, null)
            if (isPurchaseEnabled) {
                initIAPObservers()
                // Shimmer container taking sometime to get ready and perform the animation, so
                // by adding the some delay fixed that issue for lower-end devices, and for the
                // proper animation.
                postDelayed(
                    { iapViewModel.initializeProductPrice(courseData.courseSku) }, 1500
                )
                setSecondaryButtonListener(onFindCourseClick())
            } else {
                replacePrimaryWithSecondaryButton(R.string.label_find_a_course)
                setPrimaryButtonListener(onFindCourseClick())
            }
        }
    }

    private fun initIAPObservers() {
        iapViewModel.productPrice.observe(viewLifecycleOwner, EventObserver {
            setUpUpgradeButton(it)
        })

        iapViewModel.launchPurchaseFlow.observe(viewLifecycleOwner, EventObserver {
            iapViewModel.purchaseItem(
                requireActivity(), environment.loginPrefs.userId,
                courseData.courseSku
            )
        })

        iapViewModel.showLoader.observe(viewLifecycleOwner, EventObserver {
            binding.accessError.enableUpgradeButton(it)
        })

        iapViewModel.errorMessage.observe(viewLifecycleOwner, EventObserver {
            handleIAPException(it)
        })

        iapViewModel.productPurchased.observe(viewLifecycleOwner, EventObserver {
            showFullscreenLoader(it)
        })
    }

    private fun setUpUpgradeButton(skuDetails: SkuDetails) {
        // The app get the sku details instantly, so add some wait to perform
        // animation at least one cycle.
        binding.accessError.apply {
            setPrimaryButtonText(
                ResourceUtil.getFormattedString(
                    resources,
                    R.string.label_upgrade_course_button,
                    AppConstants.PRICE,
                    skuDetails.price
                ).toString()
            )
            setPrimaryButtonListener {
                iapAnalytics.trackIAPEvent(Analytics.Events.IAP_UPGRADE_NOW_CLICKED, "", "")
                iapViewModel.startPurchaseFlow(skuDetails.sku)
            }
        }
    }

    private fun handleIAPException(errorMessage: ErrorMessage) {
        var retryListener: DialogInterface.OnClickListener? = null

        if (HttpStatus.NOT_ACCEPTABLE == (errorMessage.throwable as InAppPurchasesException).httpErrorCode) {
            retryListener = DialogInterface.OnClickListener { _, _ ->
                // already purchased course.
                iapViewModel.iapFlowData.isVerificationPending = false
                iapViewModel.iapFlowData.flowType = IAPFlowType.SILENT
                showFullscreenLoader(iapViewModel.iapFlowData)
            }
        } else if (errorMessage.canRetry()) {
            retryListener = DialogInterface.OnClickListener { _: DialogInterface?, _: Int ->
                if (errorMessage.requestType == ErrorMessage.PRICE_CODE) {
                    iapViewModel.initializeProductPrice(courseData.courseSku)
                }
            }
        }
        iapDialog.handleIAPException(this, errorMessage, retryListener, null)
    }

    private fun showFullscreenLoader(iapFlowData: IAPFlowData) {
        // To proceed with the same instance of dialog fragment in case of orientation change
        val fullscreenLoader = fullscreenLoader ?: newInstance(iapFlowData)
        fullscreenLoader.show(childFragmentManager, FullscreenLoaderDialogFragment.TAG)
    }

    private fun onCloseClick() = View.OnClickListener { requireActivity().finish() }

    private fun onFindCourseClick() = View.OnClickListener {
        environment.analyticsRegistry.trackUserFindsCourses()
        EventBus.getDefault().post(MoveToDiscoveryTabEvent(Screen.DISCOVERY))
        requireActivity().finish()
    }

    private fun initCourseDateObserver() {
        courseDateViewModel.courseDates.observe(viewLifecycleOwner, EventObserver {
            if (it.courseDateBlocks != null) {
                it.organiseCourseDates()
                val outdatedCalenderId =
                    isCalendarOutOfDate(
                        requireContext(),
                        accountName,
                        calendarTitle,
                        it.courseDateBlocks
                    )
                if (outdatedCalenderId != -1L) {
                    showCalendarOutOfDateDialog(outdatedCalenderId)
                }
            }
        })

        courseDateViewModel.bannerInfo.observe(viewLifecycleOwner, NonNullObserver {
            if (!it.hasEnded && it.datesBannerInfo.getCourseBannerType() == RESET_DATES) {
                CourseDateUtil.setupCourseDatesBanner(
                    view = binding.toolbar.datesBanner.root,
                    courseId = courseData.courseId,
                    enrollmentMode = courseData.mode,
                    isSelfPaced = courseData.course.isSelfPaced,
                    screenName = Analytics.Screens.COURSE_DASHBOARD,
                    analyticsRegistry = environment.analyticsRegistry,
                    courseBannerInfoModel = it
                ) {
                    courseDateViewModel.resetCourseDatesBanner(courseData.courseId)
                }
            } else {
                binding.toolbar.datesBanner.root.setVisibility(false)
            }
        })

        courseDateViewModel.resetCourseDates.observe(viewLifecycleOwner, NonNullObserver {
            if (!CalendarUtils.isCalendarExists(contextOrThrow, accountName, calendarTitle)) {
                showShiftDateSnackBar(true)
                binding.toolbar.datesBanner.root.setVisibility(false)
            }
        })

        courseDateViewModel.syncLoader.observe(viewLifecycleOwner, EventObserver {
            if (it) {
                loaderDialog.isCancelable = false
                loaderDialog.showNow(childFragmentManager, null)
            } else {
                loaderDialog.dismiss()
                showCalendarUpdatedSnackbar()
                trackCalendarEvent(
                    Analytics.Events.CALENDAR_UPDATE_SUCCESS,
                    Analytics.Values.CALENDAR_UPDATE_SUCCESS
                )
            }
        })

        courseDateViewModel.errorMessage.observe(viewLifecycleOwner, NonNullObserver {
            if (it.throwable is HttpStatusException &&
                it.throwable.statusCode == HttpStatus.UNAUTHORIZED
            ) {
                environment.router.forceLogout(
                    contextOrThrow,
                    environment.analyticsRegistry,
                    environment.notificationDelegate
                )
            } else {
                when (it.requestType) {
                    ErrorMessage.BANNER_INFO_CODE -> {
                        binding.toolbar.datesBanner.root.setVisibility(false)
                    }
                    ErrorMessage.COURSE_RESET_DATES_CODE -> {
                        showShiftDateSnackBar(false)
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
        courseDateViewModel.fetchCourseDates(courseData.courseId, true)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
        // TODO: block of code can be removed once `fetchCourseById` retrofit call replaced with MVVM approach.
        fullscreenLoader?.closeTimer()
    }

    /**
     * Method to handle the tab-selection of the ViewPager based on screen name [Screen]
     * which may be sent through a deep link.
     *
     * @param bundle arguments
     */
    private fun handleTabSelection(bundle: Bundle?) {
        bundle?.let {
            @ScreenDef val screenName = bundle.getString(Router.EXTRA_SCREEN_NAME)
            if (screenName != null && !bundle.getBoolean(Router.EXTRA_SCREEN_SELECTED, false)) {
                for (index in courseTabItems.indices) {
                    val item = courseTabItems[index]
                    if (shouldSelectFragment(item, screenName)) {
                        binding.pager.currentItem = index
                        break
                    }
                }
                // Setting `EXTRA_SCREEN_SELECTED` to true, so that upon recreation of the fragment the tab defined in
                // the deep link is not auto-selected again.
                bundle.putBoolean(Router.EXTRA_SCREEN_SELECTED, true)
            }
        }
    }

    /**
     * Determines if a tab fragment needs to be selected based on screen name.
     *
     * @param item       [FragmentItemModel] assigned to a tab.
     * @param screenName screen name param coming from [org.edx.mobile.deeplink.DeepLinkManager]
     * @return `true` if the specified tab fragment needs to be selected, `false` otherwise
     */
    private fun shouldSelectFragment(
        item: FragmentItemModel,
        @ScreenDef screenName: String
    ): Boolean {
        return (screenName == Screen.COURSE_VIDEOS && item.title == getString(R.string.videos_title)) ||
                (screenName == Screen.COURSE_DISCUSSION && item.title == getString(R.string.discussion_title)) ||
                (screenName == Screen.DISCUSSION_POST && item.title == getString(R.string.discussion_title)) ||
                (screenName == Screen.DISCUSSION_TOPIC && item.title == getString(R.string.discussion_title)) ||
                (screenName == Screen.COURSE_DATES && item.title == getString(R.string.label_dates)) ||
                (screenName == Screen.COURSE_HANDOUT && item.title == getString(R.string.handouts_title)) ||
                (screenName == Screen.COURSE_ANNOUNCEMENT && item.title == getString(R.string.announcement_title))
    }

    private fun setViewPager() {
        binding.pager.setVisibility(true)
        enforceSingleScrollDirection(binding.pager)

        binding.toolbar.tabs.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.pager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        binding.pager.adapter = FragmentItemPagerAdapter(requireActivity(), courseTabItems)
        binding.pager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val item = courseTabItems[position]
                if (item.listener != null) {
                    item.listener?.onFragmentSelected()
                }
            }
        })

        TabLayoutMediator(
            binding.toolbar.tabs,
            binding.pager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = courseTabItems[position].title
        }.attach()

        if (courseTabItems.size - 1 > 1) {
            binding.pager.offscreenPageLimit = courseTabItems.size - 1
        }
    }

    private fun setupToolbar(hasAccess: Boolean) {
        binding.toolbar.apply {
            root.setVisibility(true)
            collapsedToolbarTitle.text = courseData.course.name
            courseOrganization.text = courseData.course.org
            courseTitle.text = courseData.course.name
            tabs.setVisibility(hasAccess)

            val expiryDate = CourseCardUtils.getFormattedDate(requireContext(), courseData)
            if (!expiryDate.isNullOrEmpty()) {
                courseExpiryDate.apply {
                    setVisibility(true)
                    text = expiryDate
                }
            }

            if (environment.config.isCourseSharingEnabled) {
                courseTitle.movementMethod = LinkMovementMethod.getInstance()
                val spannableString = setIconifiedText(
                    requireContext(),
                    courseData.course.name,
                    R.drawable.ic_share
                ) {
                    ShareUtils.showCourseShareMenu(
                        requireActivity(), courseTitle, courseData, analyticsRegistry, environment
                    )
                }
                courseTitle.text = spannableString
            }
            collapsedToolbarDismiss.setOnClickListener { requireActivity().finish() }
            expandedToolbarDismiss.setOnClickListener { requireActivity().finish() }

            if (hasAccess && courseData.isUpgradeable && environment.appFeaturesPrefs.isValuePropEnabled()) {
                layoutUpgradeBtn.root.setVisibility(true)
                (layoutUpgradeBtn.root as ShimmerFrameLayout).hideShimmer()
                layoutUpgradeBtn.btnUpgrade.setOnClickListener {
                    newInstance(
                        Analytics.Screens.PLS_COURSE_DASHBOARD,
                        courseData.courseId,
                        courseData.courseSku,
                        courseData.course.name,
                        courseData.course.isSelfPaced
                    ).show(childFragmentManager, CourseModalDialogFragment.TAG)
                }
                layoutUpgradeBtn.btnUpgrade.setText(R.string.value_prop_course_card_message)
            } else {
                layoutUpgradeBtn.root.setVisibility(false)
            }

            OnOffsetChangedListener { appBarLayout: AppBarLayout, verticalOffset: Int ->
                val maxScroll = appBarLayout.totalScrollRange
                val percentage = abs(verticalOffset).toFloat() / maxScroll.toFloat()
                handleToolbarVisibility(
                    binding.toolbar.collapsedToolbarLayout,
                    binding.toolbar.expandedToolbarLayout,
                    percentage
                )
            }.apply {
                appbar.addOnOffsetChangedListener(this)
            }
            ViewAnimationUtil.startAlphaAnimation(
                binding.toolbar.collapsedToolbarLayout,
                View.INVISIBLE
            )
            showCertificate()
        }
    }

    private fun showCertificate() {
        binding.toolbar.certificate.apply {
            if (courseData.isCertificateEarned && environment.config.areCertificateLinksEnabled()) {
                root.setVisibility(true)
                viewCertificate.setOnClickListener {
                    environment.router.showCertificate(
                        requireContext(),
                        courseData
                    )
                }
            } else {
                root.setVisibility(false)
            }
        }
    }

    private fun showShiftDateSnackBar(isSuccess: Boolean) {
        val snackbarErrorNotification = SnackbarErrorNotification(binding.root)
        if (isSuccess) {
            var actionListener: View.OnClickListener? = null
            var actionResId = 0

            if (binding.pager.currentItem != 3) {
                actionListener = View.OnClickListener { binding.pager.currentItem = 3 }
                actionResId = R.string.assessment_view_all_dates
            }
            snackbarErrorNotification.showError(
                R.string.assessment_shift_dates_success_msg,
                0, actionResId,
                SnackbarErrorNotification.COURSE_DATE_MESSAGE_DURATION,
                actionListener
            )
        } else {
            snackbarErrorNotification.showError(
                R.string.course_dates_reset_unsuccessful,
                0, 0,
                SnackbarErrorNotification.COURSE_DATE_MESSAGE_DURATION, null
            )
        }
        environment.analyticsRegistry.trackPLSCourseDatesShift(
            courseData.courseId, courseData.mode, Analytics.Screens.PLS_COURSE_DASHBOARD, isSuccess
        )
    }

    private fun showCalendarOutOfDateDialog(calendarId: Long) {
        val alertDialogFragment = AlertDialogFragment.newInstance(
            getString(R.string.title_calendar_out_of_date),
            getString(R.string.message_calendar_out_of_date),
            getString(R.string.label_update_now),
            { _: DialogInterface?, _: Int -> updateCalendarEvents() },
            getString(R.string.label_remove_course_calendar)
        ) { _: DialogInterface?, _: Int ->
            removeCalendar(
                calendarId
            )
        }
        alertDialogFragment.isCancelable = false
        alertDialogFragment.show(childFragmentManager, null)
    }

    private fun updateCalendarEvents() {
        trackCalendarEvent(
            Analytics.Events.CALENDAR_SYNC_UPDATE,
            Analytics.Values.CALENDAR_SYNC_UPDATE
        )
        val newCalId = createOrUpdateCalendar(
            contextOrThrow, accountName, CalendarContract.ACCOUNT_TYPE_LOCAL, calendarTitle
        )
        checkCalendarSyncEnabled(environment.config,
            object : OnCalendarSyncListener {
                override fun onCalendarSyncResponse(response: CourseDatesCalendarSync) {
                    courseDateViewModel.addOrUpdateEventsInCalendar(
                        contextOrThrow,
                        newCalId,
                        courseData.courseId,
                        courseData.course.name,
                        response.isDeepLinkEnabled,
                        true
                    )
                }
            })
    }

    private fun removeCalendar(calendarId: Long) {
        trackCalendarEvent(
            Analytics.Events.CALENDAR_SYNC_REMOVE, Analytics.Values.CALENDAR_SYNC_REMOVE
        )
        deleteCalendar(contextOrThrow, calendarId)
        showCalendarRemovedSnackbar()
        trackCalendarEvent(
            Analytics.Events.CALENDAR_REMOVE_SUCCESS, Analytics.Values.CALENDAR_REMOVE_SUCCESS
        )
    }

    private fun trackCalendarEvent(eventName: String, biValue: String) {
        environment.analyticsRegistry.trackCalendarEvent(
            eventName,
            biValue,
            courseData.courseId,
            courseData.mode,
            courseData.course.isSelfPaced,
            courseDateViewModel.getSyncingCalendarTime()
        )
        courseDateViewModel.resetSyncingCalendarTime()
    }

    private fun fetchCourseById() {
        val courseId = arguments?.getString(Router.EXTRA_COURSE_ID) ?: return

        courseApi.enrolledCourses.enqueue(object :
            GetCourseByIdCallback(requireActivity(), courseId, null) {

            override fun onResponse(coursesResponse: EnrolledCoursesResponse) {
                if (activity != null) {
                    arguments?.putSerializable(Router.EXTRA_COURSE_DATA, coursesResponse)
                    val fullscreenLoader = fullscreenLoader
                    if (fullscreenLoader != null && fullscreenLoader.isResumed) {
                        SnackbarErrorNotification(binding.root).showUpgradeSuccessSnackbar(R.string.purchase_success_message)
                        // Passing the listener to fullscreenLoader() is necessary to prevent the app
                        // from crashing when the parent fragment releases the dialog during a restart.
                        fullscreenLoader.closeLoader(object : FullScreenDismissListener {
                            override fun onDismiss() {
                                requireActivity().runOnUiThread {
                                    restartFragment(this@CourseTabsDashboardFragment)
                                }
                            }
                        })
                    } else {
                        restartFragment(this@CourseTabsDashboardFragment)
                    }
                }
            }

            override fun onFailure(error: Throwable) {
                if (activity != null) {
                    val fullscreenLoader = fullscreenLoader
                    if (fullscreenLoader != null && fullscreenLoader.isResumed) {
                        iapViewModel.dispatchError(ErrorMessage.COURSE_REFRESH_CODE, null, error)
                    } else {
                        arguments?.putBoolean(ARG_COURSE_NOT_FOUND, true)
                        restartFragment(this@CourseTabsDashboardFragment)
                        logger.error(
                            Exception("Invalid Course ID provided via deeplink: $courseId"),
                            true
                        )
                    }
                }
            }
        })
    }

    private fun createCourseOutlineItem(screenName: String?): FragmentItemModel {
        return FragmentItemModel(
            CourseOutlineFragment::class.java,
            resources.getString(R.string.label_home),
            CourseOutlineFragment.makeArguments(
                courseData, arguments?.getString(Router.EXTRA_COURSE_COMPONENT_ID),
                false, screenName
            )
        ) {
            environment.analyticsRegistry.trackScreenView(
                Analytics.Screens.COURSE_OUTLINE, courseData.courseId, null
            )
        }
    }

    private fun createVideosItem(): FragmentItemModel {
        return FragmentItemModel(
            CourseOutlineFragment::class.java,
            resources.getString(R.string.videos_title),
            CourseOutlineFragment.makeArguments(courseData, null, true, null)
        ) {
            environment.analyticsRegistry.trackScreenView(
                Analytics.Screens.VIDEOS_COURSE_VIDEOS, courseData.courseId, null
            )
        }
    }

    private fun createDiscussionsItem(): FragmentItemModel {
        return FragmentItemModel(
            CourseDiscussionTopicsFragment::class.java,
            resources.getString(R.string.discussion_title),
            arguments
        ) {
            environment.analyticsRegistry.trackScreenView(
                Analytics.Screens.FORUM_VIEW_TOPICS, courseData.courseId, null, null
            )
        }
    }

    private fun createDatesItem(): FragmentItemModel {
        return FragmentItemModel(
            CourseDatesPageFragment::class.java,
            resources.getString(R.string.label_dates),
            CourseDatesPageFragment.makeArguments(courseData)
        ) {
            analyticsRegistry.trackScreenView(
                Analytics.Screens.COURSE_DATES, courseData.courseId, null
            )
        }
    }

    private fun createHandoutsItem(): FragmentItemModel {
        return FragmentItemModel(
            CourseHandoutFragment::class.java,
            resources.getString(R.string.handouts_title),
            CourseHandoutFragment.makeArguments(courseData)
        ) {
            analyticsRegistry.trackScreenView(
                Analytics.Screens.COURSE_HANDOUTS, courseData.courseId, null
            )
        }
    }

    private fun createAnnouncementsItem(): FragmentItemModel {
        return FragmentItemModel(
            CourseAnnouncementsFragment::class.java,
            resources.getString(R.string.announcement_title),
            CourseAnnouncementsFragment.makeArguments(courseData)
        ) {
            analyticsRegistry.trackScreenView(
                Analytics.Screens.COURSE_ANNOUNCEMENTS, courseData.courseId, null
            )
        }
    }

    /**
     * It will handle the toolbar's collapse or expand state based on the scroll position. It will
     * also disable the clickable views of the toolbar because the alpha attribute has been used to
     * handle the toolbar's transition from one state to another.
     *
     * Inspiration: http://www.devexchanges.info/2016/03/android-tip-custom-coordinatorlayout.html
     *
     * @param collapsedToolbar Parent view for Toolbar design in collapsed state
     * @param expandedToolbar  Parent view for Toolbar design in expanded state
     * @param percentage       Percentage of Toolbar's current scroll over max scroll
     */
    private fun handleToolbarVisibility(
        collapsedToolbar: View, expandedToolbar: View,
        percentage: Float
    ) {
        val minVisibleCollapsedToolbarPercentage = 0.9f
        val maxHiddenExpandedToolbarPercentage = 0.8f

        if (percentage >= minVisibleCollapsedToolbarPercentage) {
            if (!isCollapsedTitleVisible) {
                ViewAnimationUtil.startAlphaAnimation(collapsedToolbar, View.VISIBLE)
                isCollapsedTitleVisible = true
            }
        } else {
            if (isCollapsedTitleVisible) {
                ViewAnimationUtil.startAlphaAnimation(collapsedToolbar, View.INVISIBLE)
                isCollapsedTitleVisible = false
            }
        }
        if (percentage >= maxHiddenExpandedToolbarPercentage) {
            if (isExpandedTitleVisible) {
                ViewAnimationUtil.startAlphaAnimation(expandedToolbar, View.INVISIBLE)
                isExpandedTitleVisible = false
            }
            binding.toolbar.apply {
                courseTitle.isEnabled = false
                expandedToolbarDismiss.isEnabled = false
                layoutUpgradeBtn.root.isEnabled = false
            }
        } else {
            if (!isExpandedTitleVisible) {
                ViewAnimationUtil.startAlphaAnimation(expandedToolbar, View.VISIBLE)
                isExpandedTitleVisible = true
            }
            binding.toolbar.apply {
                courseTitle.isEnabled = true
                expandedToolbarDismiss.isEnabled = true
                layoutUpgradeBtn.root.isEnabled = true
            }
        }
    }

    @Subscribe
    fun onEventMainThread(event: IAPFlowEvent) {
        if (!this.isResumed) {
            return
        }
        if (event.flowAction == IAPFlowData.IAPAction.PURCHASE_FLOW_COMPLETE) {
            courseData.mode = EnrollmentMode.VERIFIED.toString()
            arguments?.putString(Router.EXTRA_COURSE_ID, courseData.courseId)
            courseDateViewModel.fetchCourseDates(courseData.courseId, true)
            fetchCourseById()
            EventBus.getDefault().post(MainDashboardRefreshEvent())
        }
    }

    companion object {
        private const val ARG_COURSE_NOT_FOUND = "ARG_COURSE_NOT_FOUND"

        fun newInstance(bundle: Bundle?): CourseTabsDashboardFragment =
            CourseTabsDashboardFragment().apply {
                arguments = bundle
            }

        fun newInstance(
            courseData: EnrolledCoursesResponse?, courseId: String?,
            @ScreenDef screenName: String?
        ): CourseTabsDashboardFragment = newInstance(
            Bundle().apply {
                putSerializable(Router.EXTRA_COURSE_DATA, courseData)
                putSerializable(Router.EXTRA_COURSE_ID, courseId)
                putSerializable(Router.EXTRA_SCREEN_NAME, screenName)
            }
        )
    }
}
