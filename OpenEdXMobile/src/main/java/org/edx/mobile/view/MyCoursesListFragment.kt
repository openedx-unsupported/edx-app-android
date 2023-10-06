package org.edx.mobile.view

import android.content.DialogInterface
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnScrollChangedListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.R
import org.edx.mobile.authentication.LoginAPI
import org.edx.mobile.base.MainApplication
import org.edx.mobile.databinding.FragmentMyCoursesListBinding
import org.edx.mobile.deeplink.DeepLink
import org.edx.mobile.deeplink.DeepLinkManager
import org.edx.mobile.deeplink.Screen
import org.edx.mobile.event.EnrolledInCourseEvent
import org.edx.mobile.event.IAPFlowEvent
import org.edx.mobile.event.MainDashboardRefreshEvent
import org.edx.mobile.event.MoveToDiscoveryTabEvent
import org.edx.mobile.event.MyCoursesRefreshEvent
import org.edx.mobile.event.NetworkConnectivityChangeEvent
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.extenstion.parcelable
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.http.notifications.FullScreenErrorNotification
import org.edx.mobile.http.notifications.SnackbarErrorNotification
import org.edx.mobile.interfaces.RefreshListener
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.iap.IAPFlowData
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.module.analytics.InAppPurchasesAnalytics
import org.edx.mobile.util.AppStoreUtils
import org.edx.mobile.util.InAppPurchasesException
import org.edx.mobile.util.NetworkUtil
import org.edx.mobile.util.NonNullObserver
import org.edx.mobile.util.UiUtils
import org.edx.mobile.util.observer.EventObserver
import org.edx.mobile.view.adapters.BaseListAdapter
import org.edx.mobile.view.adapters.MyCoursesListAdapter
import org.edx.mobile.view.custom.error.EdxErrorState
import org.edx.mobile.view.dialog.CourseModalDialogFragment
import org.edx.mobile.view.dialog.FullscreenLoaderDialogFragment
import org.edx.mobile.viewModel.CourseViewModel
import org.edx.mobile.viewModel.CourseViewModel.CoursesRequestType
import org.edx.mobile.viewModel.InAppPurchasesViewModel
import org.edx.mobile.wrapper.InAppPurchasesDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

@AndroidEntryPoint
class MyCoursesListFragment : OfflineSupportBaseFragment(), RefreshListener {

    private lateinit var adapter: MyCoursesListAdapter
    private lateinit var binding: FragmentMyCoursesListBinding

    private val courseViewModel: CourseViewModel by viewModels()
    private val iapViewModel: InAppPurchasesViewModel by viewModels()

    @Inject
    lateinit var loginAPI: LoginAPI

    @Inject
    lateinit var iapAnalytics: InAppPurchasesAnalytics

    @Inject
    lateinit var iapDialog: InAppPurchasesDialog

    private lateinit var errorNotification: FullScreenErrorNotification
    private lateinit var onScrollChangedListener: OnScrollChangedListener

    private var refreshOnResume = false
    private var isObserversInitialized = true
    private var lastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = object : MyCoursesListAdapter(environment) {
            override fun onItemClicked(model: EnrolledCoursesResponse) {
                activity?.let { activity ->
                    environment.router.showCourseDashboardTabs(activity, model)
                }
            }

            override fun onAnnouncementClicked(model: EnrolledCoursesResponse) {
                activity?.let { activity ->
                    environment.router.showCourseDashboardTabs(
                        activity,
                        model,
                        Screen.COURSE_ANNOUNCEMENT
                    )
                }
            }

            override fun onValuePropClicked(model: EnrolledCoursesResponse) {
                //This time is checked to avoid taps in quick succession
                val currentTime = SystemClock.elapsedRealtime()
                if (currentTime - lastClickTime > BaseListAdapter.MIN_CLICK_INTERVAL) {
                    lastClickTime = currentTime
                    CourseModalDialogFragment.newInstance(
                        Analytics.Screens.COURSE_ENROLLMENT,
                        model.courseId,
                        model.courseSku,
                        model.course.name,
                        model.course.isSelfPaced
                    ).show(childFragmentManager, CourseModalDialogFragment.TAG)
                }
            }
        }
        detectDeeplink()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyCoursesListBinding.inflate(inflater, container, false)
        errorNotification = FullScreenErrorNotification(binding.myCourseList)

        binding.swipeContainer.setOnRefreshListener {
            errorNotification.hideError()
            binding.stateLayout.root.setVisibility(false)
            courseViewModel.fetchEnrolledCourses(
                type = CoursesRequestType.STALE,
                showProgress = false
            )
        }
        UiUtils.setSwipeRefreshLayoutColors(binding.swipeContainer)

        binding.myCourseList.adapter = adapter
        binding.myCourseList.addItemDecoration(
            DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL).apply {
                setDrawable(
                    UiUtils.getDrawable(
                        requireContext(),
                        R.drawable.my_course_list_recycler_view_divider
                    )
                )
            }
        )
        initCourseObservers()
        courseViewModel.fetchEnrolledCourses(type = CoursesRequestType.PERSISTABLE_CACHE)

        return binding.root
    }

    private fun initCourseObservers() {
        courseViewModel.showProgress.observe(viewLifecycleOwner, NonNullObserver {
            binding.loadingIndicator.root.setVisibility(it)
            if (it) {
                errorNotification.hideError()
                binding.stateLayout.root.setVisibility(false)
            }
        })

        courseViewModel.enrolledCoursesResponse.observe(
            viewLifecycleOwner,
            EventObserver { enrolledCourses ->
                populateCourseData(data = enrolledCourses)
                // Checking if the user is logged-in as we need userId to enable IAP
                if (environment.loginPrefs.isUserLoggedIn &&
                    environment.featuresPrefs.isIAPEnabledForUser(environment.loginPrefs.isOddUserId)
                ) {
                    initInAppPurchaseSetup()
                    val fullscreenLoader = FullscreenLoaderDialogFragment.getRetainedInstance(
                        fragmentManager = childFragmentManager
                    )
                    if (fullscreenLoader != null) {
                        SnackbarErrorNotification(binding.root).showUpgradeSuccessSnackbar(R.string.purchase_success_message)
                        fullscreenLoader.closeLoader()
                    } else if (environment.featuresPrefs.canAutoCheckUnfulfilledPurchase &&
                        courseViewModel.courseRequestType != CoursesRequestType.PERSISTABLE_CACHE
                    ) {
                        detectUnfulfilledPurchase(enrolledCourses)
                    }
                }
            })

        courseViewModel.handleError.observe(viewLifecycleOwner, NonNullObserver {
            when {
                (it is HttpStatusException) -> {
                    when (it.statusCode) {
                        HttpStatus.UNAUTHORIZED -> {
                            context?.let { context ->
                                environment.router?.forceLogout(
                                    context,
                                    environment.analyticsRegistry,
                                    environment.notificationDelegate
                                )
                            }
                        }

                        HttpStatus.UPGRADE_REQUIRED -> {
                            showError(EdxErrorState.State.UPDATE_APP)
                        }

                        else -> {
                            showError(EdxErrorState.State.NETWORK)
                        }
                    }
                }

                (FullscreenLoaderDialogFragment
                    .getRetainedInstance(fragmentManager = childFragmentManager)?.isAdded == true) -> {
                    iapViewModel.dispatchError(
                        requestType = ErrorMessage.COURSE_REFRESH_CODE,
                        errorMessage = it.message
                    )
                }

                else -> {
                    showError(EdxErrorState.State.NETWORK)
                }
            }
            invalidateView()
        })
    }

    private fun initIAPObservers() {
        iapViewModel.refreshCourseData.observe(viewLifecycleOwner, EventObserver { iapFlowData ->
            iapDialog.showNewExperienceAlertDialog(this, { _, _ ->
                iapAnalytics.trackIAPEvent(
                    eventName = Analytics.Events.IAP_NEW_EXPERIENCE_ALERT_ACTION,
                    actionTaken = Analytics.Values.ACTION_REFRESH
                )
                iapAnalytics.initUnlockContentTime()
                showFullscreenLoader(iapFlowData)
            }, { _, _ ->
                iapAnalytics.trackIAPEvent(
                    eventName = Analytics.Events.IAP_NEW_EXPERIENCE_ALERT_ACTION,
                    actionTaken = Analytics.Values.ACTION_CONTINUE_WITHOUT_UPDATE
                )
            })
        })

        iapViewModel.errorMessage.observe(viewLifecycleOwner, EventObserver { errorMessage ->
            var retryListener: DialogInterface.OnClickListener? = null
            if (errorMessage.canRetry()) {
                retryListener = DialogInterface.OnClickListener { _, _ ->
                    if (errorMessage.requestType == ErrorMessage.EXECUTE_ORDER_CODE) {
                        iapViewModel.executeOrder()
                    } else if (errorMessage.requestType == ErrorMessage.COURSE_REFRESH_CODE) {
                        courseViewModel.fetchEnrolledCourses(
                            type = CoursesRequestType.LIVE,
                            showProgress = false
                        )
                    } else if (HttpStatus.NOT_ACCEPTABLE == (errorMessage.throwable as InAppPurchasesException).httpErrorCode) {
                        showFullscreenLoader(iapFlowData = iapViewModel.iapFlowData)
                    }
                }
            }
            iapDialog.handleIAPException(
                fragment = this@MyCoursesListFragment,
                errorMessage = errorMessage,
                retryListener = retryListener,
                cancelListener = { _, _ ->
                    iapViewModel.iapFlowData.clear()
                    if (errorMessage.isPostUpgradeErrorType()) {
                        FullscreenLoaderDialogFragment.getRetainedInstance(childFragmentManager)
                            ?.dismiss()
                    }
                }
            )
        })
    }

    /**
     * Method to detect Unfulfilled Purchases.
     *
     * @param enrolledCourses User enrolled courses.
     * */
    private fun detectUnfulfilledPurchase(enrolledCourses: List<EnrolledCoursesResponse>) {
        iapAnalytics.reset()
        iapViewModel.detectUnfulfilledPurchase(
            environment.loginPrefs.userId,
            enrolledCourses,
            IAPFlowData.IAPFlowType.SILENT,
            Analytics.Screens.COURSE_ENROLLMENT
        )
    }

    override fun onStart() {
        super.onStart()
        // To force the SwipeRefreshLayout to use the scroll only when the underlying view has
        // scrolled up to its top.
        binding.swipeContainer.viewTreeObserver.addOnScrollChangedListener(
            OnScrollChangedListener {
                binding.swipeContainer.isEnabled = binding.stateLayout.root.scrollY == 0
            }.also {
                onScrollChangedListener = it
            })
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this@MyCoursesListFragment)) {
            EventBus.getDefault().register(this@MyCoursesListFragment)
        }
        if (refreshOnResume) {
            courseViewModel.fetchEnrolledCourses(
                type = CoursesRequestType.PERSISTABLE_CACHE,
                showProgress = false
            )
            refreshOnResume = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.swipeContainer.viewTreeObserver.removeOnScrollChangedListener(
            onScrollChangedListener
        )
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true)
    @Suppress("UNUSED_PARAMETER")
    fun onEventMainThread(event: EnrolledInCourseEvent?) {
        refreshOnResume = true
    }

    @Subscribe
    fun onEventMainThread(event: IAPFlowEvent) {
        if (!this.isResumed) {
            return
        }
        when (event.flowAction) {
            IAPFlowData.IAPAction.SHOW_FULL_SCREEN_LOADER -> {
                event.iapFlowData?.let {
                    showFullscreenLoader(event.iapFlowData)
                }
            }

            IAPFlowData.IAPAction.PURCHASE_FLOW_COMPLETE -> {
                courseViewModel.fetchEnrolledCourses(type = CoursesRequestType.LIVE)
            }
        }
    }

    private fun initInAppPurchaseSetup() {
        if (isAdded && environment.featuresPrefs.isValuePropEnabled && isObserversInitialized) {
            initIAPObservers()
            isObserversInitialized = false
        }
    }

    private fun populateCourseData(
        data: List<EnrolledCoursesResponse>
    ) {
        adapter.submitList(data)

        if (adapter.itemCount == 0 && environment.config.discoveryConfig.isDiscoveryEnabled) {
            binding.stateLayout.state.setState(EdxErrorState.State.EMPTY, Screen.MY_COURSES)
            binding.stateLayout.state.setActionListener {
                environment.analyticsRegistry?.trackUserFindsCourses(adapter.itemCount)
                EventBus.getDefault().post(MoveToDiscoveryTabEvent(Screen.DISCOVERY))
            }
            binding.stateLayout.root.setVisibility(true)
            binding.myCourseList.setVisibility(false)
        } else if (adapter.itemCount == 0 && !environment.config.discoveryConfig.isDiscoveryEnabled) {
            errorNotification.showError(
                R.string.no_courses_to_display,
                R.drawable.ic_error, 0, null
            )
            binding.myCourseList.setVisibility(false)
        } else {
            binding.stateLayout.root.setVisibility(false)
            binding.myCourseList.setVisibility(true)
        }
        invalidateView()
    }

    private fun showFullscreenLoader(iapFlowData: IAPFlowData) {
        // To proceed with the same instance of dialog fragment in case of orientation change
        val fullscreenLoader = try {
            FullscreenLoaderDialogFragment
                .getRetainedInstance(fragmentManager = childFragmentManager) as FullscreenLoaderDialogFragment
        } catch (e: Exception) {
            FullscreenLoaderDialogFragment.newInstance(iapFlowData)
        }
        fullscreenLoader.show(childFragmentManager, FullscreenLoaderDialogFragment.TAG)
    }

    private fun detectDeeplink() {
        arguments?.parcelable<DeepLink>(Router.EXTRA_DEEP_LINK)?.let {
            DeepLinkManager.proceedDeeplink(requireActivity(), it)
            MainApplication.instance().showBanner(loginAPI, true)
            arguments?.putParcelable(Router.EXTRA_DEEP_LINK, null)
        } ?: run {
            MainApplication.instance().showBanner(loginAPI, false)
        }
    }

    private fun invalidateView() {
        binding.swipeContainer.isRefreshing = false
        binding.loadingIndicator.root.visibility = View.GONE
    }

    private fun showError(state: EdxErrorState.State) {
        binding.myCourseList.setVisibility(false)
        binding.stateLayout.root.setVisibility(true)
        binding.stateLayout.state.setState(state, Screen.MY_COURSES)
        binding.stateLayout.state.setActionListener {
            when (state) {
                EdxErrorState.State.UPDATE_APP -> {
                    AppStoreUtils.openAppInAppStore(requireContext())
                }

                EdxErrorState.State.NETWORK -> {
                    onRefresh()
                }

                else -> {}
            }
        }
    }

    override fun onRefresh() {
        EventBus.getDefault().post(MainDashboardRefreshEvent())
    }

    @Subscribe(sticky = true)
    @Suppress("UNUSED_PARAMETER")
    fun onEvent(event: MainDashboardRefreshEvent) {
        courseViewModel.fetchEnrolledCourses(type = CoursesRequestType.LIVE)
    }

    @Subscribe(sticky = true)
    @Suppress("UNUSED_PARAMETER")
    fun onEvent(event: MyCoursesRefreshEvent) {
        courseViewModel.fetchEnrolledCourses(type = CoursesRequestType.LIVE)
    }

    override fun onRevisit() {
        super.onRevisit()
        if (NetworkUtil.isConnected(activity)) {
            binding.swipeContainer.isEnabled = true
        }
    }

    @Subscribe(sticky = true)
    fun onEvent(event: NetworkConnectivityChangeEvent?) {
        if (activity != null) {
            if (NetworkUtil.isConnected(context)) {
                binding.swipeContainer.isEnabled = true
            } else {
                //Disable swipe functionality and hide the loading view
                binding.swipeContainer.isEnabled = false
                binding.swipeContainer.isRefreshing = false
            }
            onNetworkConnectivityChangeEvent(event)
        }
    }

    override fun isShowingFullScreenError(): Boolean {
        return errorNotification.isShowing
    }
}
