package org.edx.mobile.view

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.R
import org.edx.mobile.authentication.LoginAPI
import org.edx.mobile.base.MainApplication
import org.edx.mobile.databinding.FragmentMyCoursesListBinding
import org.edx.mobile.databinding.PanelFindCourseBinding
import org.edx.mobile.deeplink.DeepLink
import org.edx.mobile.deeplink.DeepLinkManager
import org.edx.mobile.deeplink.Screen
import org.edx.mobile.event.EnrolledInCourseEvent
import org.edx.mobile.event.MainDashboardRefreshEvent
import org.edx.mobile.event.MoveToDiscoveryTabEvent
import org.edx.mobile.event.NetworkConnectivityChangeEvent
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.http.notifications.FullScreenErrorNotification
import org.edx.mobile.http.notifications.SnackbarErrorNotification
import org.edx.mobile.interfaces.RefreshListener
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.module.analytics.InAppPurchasesAnalytics
import org.edx.mobile.util.NetworkUtil
import org.edx.mobile.util.NonNullObserver
import org.edx.mobile.util.UiUtils
import org.edx.mobile.view.adapters.MyCoursesAdapter
import org.edx.mobile.view.dialog.CourseModalDialogFragment
import org.edx.mobile.view.dialog.FullscreenLoaderDialogFragment
import org.edx.mobile.viewModel.CourseViewModel
import org.edx.mobile.viewModel.CourseViewModel.CoursesRequestType
import org.edx.mobile.viewModel.InAppPurchasesViewModel
import org.edx.mobile.wrapper.InAppPurchasesDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

@AndroidEntryPoint
class MyCoursesListFragment : OfflineSupportBaseFragment(), RefreshListener {

    private lateinit var adapter: MyCoursesAdapter
    private lateinit var binding: FragmentMyCoursesListBinding

    private val courseViewModel: CourseViewModel by viewModels()
    private val iapViewModel: InAppPurchasesViewModel
            by viewModels(ownerProducer = { requireActivity() })

    @Inject
    lateinit var loginAPI: LoginAPI

    @Inject
    lateinit var iapAnalytics: InAppPurchasesAnalytics

    @Inject
    lateinit var iapDialog: InAppPurchasesDialog

    private lateinit var errorNotification: FullScreenErrorNotification
    private var fullscreenLoader: FullscreenLoaderDialogFragment? = null
    private var refreshOnResume = false
    private var refreshOnPurchase = false
    private var isObserversInitialized = true
    private var lastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = object : MyCoursesAdapter(activity, environment) {
            override fun onItemClicked(model: EnrolledCoursesResponse) {
                activity?.let { activity ->
                    environment.router.showCourseDashboardTabs(activity, model, false)
                }
            }

            override fun onAnnouncementClicked(model: EnrolledCoursesResponse) {
                activity?.let { activity ->
                    environment.router.showCourseDashboardTabs(activity, model, true)
                }
            }

            override fun onValuePropClicked(model: EnrolledCoursesResponse) {
                //This time is checked to avoid taps in quick succession
                val currentTime = SystemClock.elapsedRealtime()
                if (currentTime - lastClickTime > MIN_CLICK_INTERVAL) {
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
            courseViewModel.fetchEnrolledCourses(
                type = CoursesRequestType.STALE,
                showProgress = false
            )
        }
        UiUtils.setSwipeRefreshLayoutColors(binding.swipeContainer)

        // Add empty view to cause divider to render at the top of the list.
        binding.myCourseList.addHeaderView(View(context), null, false)
        binding.myCourseList.adapter = adapter
        binding.myCourseList.onItemClickListener = adapter

        initCourseObservers()
        courseViewModel.fetchEnrolledCourses(type = CoursesRequestType.CACHE)

        return binding.root
    }

    private fun initCourseObservers() {
        courseViewModel.showProgress.observe(viewLifecycleOwner, NonNullObserver {
            binding.loadingIndicator.root.setVisibility(it)
            if (it) {
                errorNotification.hideError()
            }
        })

        courseViewModel.enrolledCoursesResponse.observe(
            viewLifecycleOwner,
            NonNullObserver { enrolledCourses ->
                populateCourseData(data = enrolledCourses)
                if (environment.appFeaturesPrefs.isIAPEnabled(environment.loginPrefs.isOddUserId)) {
                    initInAppPurchaseSetup()
                    resetPurchase()
                    iapViewModel.detectUnfulfilledPurchase(
                        requireActivity(),
                        environment.loginPrefs.userId,
                        enrolledCourses
                    )
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
                            context?.let { context ->
                                errorNotification.showError(
                                    context,
                                    it
                                )
                            }
                        }
                    }
                }
                (fullscreenLoader?.isAdded == true) -> {
                    iapViewModel.dispatchError(
                        requestType = ErrorMessage.COURSE_REFRESH_CODE,
                        errorMessage = it.message
                    )
                }
                else -> {
                    showError(it)
                }
            }
            invalidateView()
        })
    }

    private fun initIAPObservers() {
        iapViewModel.executeResponse.observe(
            viewLifecycleOwner,
            NonNullObserver {
                if (iapViewModel.upgradeMode.isSilentMode()) {
                    iapDialog.showNewExperienceAlertDialog(this, { _, _ ->
                        iapViewModel.showFullScreenLoader(true)
                    }, { _, _ ->
                        resetPurchase()
                    })
                }
            })

        iapViewModel.showFullscreenLoaderDialog.observe(
            viewLifecycleOwner,
            NonNullObserver { canShowLoader ->
                if (canShowLoader) {
                    fullscreenLoader?.show(childFragmentManager, FullscreenLoaderDialogFragment.TAG)
                    iapViewModel.showFullScreenLoader(false)
                }
            })

        iapViewModel.refreshCourseData.observe(
            viewLifecycleOwner,
            NonNullObserver { refreshCourse ->
                if (refreshCourse) {
                    refreshOnPurchase = true
                    courseViewModel.fetchEnrolledCourses(
                        type = CoursesRequestType.LIVE,
                        showProgress = false
                    )
                    iapViewModel.refreshCourseData(false)
                }
            })

        iapViewModel.purchaseFlowComplete.observe(
            viewLifecycleOwner,
            NonNullObserver { isPurchaseCompleted ->
                if (isPurchaseCompleted) {
                    if (fullscreenLoader?.isAdded == true) fullscreenLoader?.dismiss()
                    iapAnalytics.trackIAPEvent(Analytics.Events.IAP_COURSE_UPGRADE_SUCCESS)
                    iapAnalytics.trackIAPEvent(Analytics.Events.IAP_UNLOCK_UPGRADED_CONTENT_TIME)
                    iapAnalytics.trackIAPEvent(Analytics.Events.IAP_UNLOCK_UPGRADED_CONTENT_REFRESH_TIME)
                    SnackbarErrorNotification(binding.root).showError(R.string.purchase_success_message)
                    iapViewModel.resetPurchase(false)
                }
            })

        iapViewModel.errorMessage.observe(viewLifecycleOwner, NonNullObserver { errorMsg ->
            if (iapViewModel.upgradeMode.isNormalMode()) {
                return@NonNullObserver
            }
            iapDialog.handleIAPException(
                fragment = this@MyCoursesListFragment,
                errorMessage = errorMsg,
                retryListener = { _, _ ->
                    if (errorMsg.requestType == ErrorMessage.EXECUTE_ORDER_CODE)
                        iapViewModel.executeOrder(requireActivity())
                    else
                        iapViewModel.refreshCourseData(true)
                },
                cancelListener = { _, _ -> resetPurchase() }
            )
            iapViewModel.errorMessageShown()
        })
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this@MyCoursesListFragment)) {
            EventBus.getDefault().register(this@MyCoursesListFragment)
        }
        if (refreshOnResume) {
            courseViewModel.fetchEnrolledCourses(
                type = CoursesRequestType.CACHE,
                showProgress = false
            )
            refreshOnResume = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true)
    fun onEventMainThread(event: EnrolledInCourseEvent?) {
        refreshOnResume = true
    }

    private fun initInAppPurchaseSetup() {
        if (isAdded && environment.appFeaturesPrefs.isValuePropEnabled() &&
            isObserversInitialized
        ) {
            initFullscreenLoader()
            initIAPObservers()
            isObserversInitialized = false
        }
    }

    private fun populateCourseData(
        data: List<EnrolledCoursesResponse>
    ) {
        if (data.isNotEmpty()) {
            adapter.setItems(data)
        }

        addFindCoursesFooter()
        adapter.notifyDataSetChanged()

        if (adapter.isEmpty && !environment.config.discoveryConfig.isDiscoveryEnabled) {
            errorNotification.showError(
                R.string.no_courses_to_display,
                R.drawable.ic_error, 0, null
            )
            binding.myCourseList.visibility = View.GONE
        }
        invalidateView()
    }

    private fun initFullscreenLoader() {
        // To proceed with the same instance of dialog fragment in case of orientation change
        fullscreenLoader = try {
            childFragmentManager.findFragmentByTag(FullscreenLoaderDialogFragment.TAG) as FullscreenLoaderDialogFragment
        } catch (e: Exception) {
            FullscreenLoaderDialogFragment.newInstance()
        }
    }

    private fun resetPurchase() {
        if (refreshOnPurchase && fullscreenLoader?.isAdded == true) {
            Timer("", false).schedule(
                fullscreenLoader?.getRemainingVisibleTime()
                    ?: FullscreenLoaderDialogFragment.MINIMUM_DISPLAY_DELAY
            ) {
                refreshOnPurchase = false
                iapViewModel.resetPurchase(true)
            }
        } else if (iapViewModel.upgradeMode.isSilentMode()) {
            iapViewModel.resetPurchase(false)
        }
    }

    private fun detectDeeplink() {
        if (arguments?.get(Router.EXTRA_DEEP_LINK) != null) {
            (arguments?.get(Router.EXTRA_DEEP_LINK) as DeepLink).let { deeplink ->
                DeepLinkManager.proceedDeeplink(requireActivity(), deeplink)
                MainApplication.instance().showBanner(loginAPI, true)
            }
        } else {
            MainApplication.instance().showBanner(loginAPI, false)
        }
    }

    private fun invalidateView() {
        binding.swipeContainer.isRefreshing = false
        binding.loadingIndicator.root.visibility = View.GONE
    }

    private fun showError(error: Throwable) {
        context?.let { context ->
            errorNotification.showError(context, error, R.string.lbl_reload) {
                if (NetworkUtil.isConnected(context)) {
                    onRefresh()
                }
            }
        }
    }

    private fun addFindCoursesFooter() {
        // Validate footer is not already added.
        if (binding.myCourseList.footerViewsCount > 0) {
            return
        }
        if (environment.config.discoveryConfig.isDiscoveryEnabled) {
            // Add 'Find a Course' list item as a footer.
            val footer: PanelFindCourseBinding = DataBindingUtil.inflate(
                LayoutInflater.from(activity),
                R.layout.panel_find_course, binding.myCourseList, false
            )
            binding.myCourseList.addFooterView(footer.root, null, false)
            footer.courseBtn.setOnClickListener {
                environment.analyticsRegistry?.trackUserFindsCourses()
                EventBus.getDefault().post(MoveToDiscoveryTabEvent(Screen.DISCOVERY))
            }
        }
        // Add empty view to cause divider to render at the bottom of the list.
        binding.myCourseList.addFooterView(View(context), null, false)
    }

    override fun onRefresh() {
        EventBus.getDefault().post(MainDashboardRefreshEvent())
    }

    @Subscribe(sticky = true)
    fun onEvent(event: MainDashboardRefreshEvent?) {
        courseViewModel.fetchEnrolledCourses(type = CoursesRequestType.STALE)
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
