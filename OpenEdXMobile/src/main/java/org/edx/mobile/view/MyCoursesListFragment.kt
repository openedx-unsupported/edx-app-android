package org.edx.mobile.view

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.edx.mobile.R
import org.edx.mobile.authentication.LoginAPI
import org.edx.mobile.base.MainApplication
import org.edx.mobile.course.CourseAPI
import org.edx.mobile.databinding.FragmentMyCoursesListBinding
import org.edx.mobile.databinding.PanelFindCourseBinding
import org.edx.mobile.deeplink.DeepLink
import org.edx.mobile.deeplink.DeepLinkManager
import org.edx.mobile.deeplink.Screen
import org.edx.mobile.event.EnrolledInCourseEvent
import org.edx.mobile.event.MainDashboardRefreshEvent
import org.edx.mobile.event.MoveToDiscoveryTabEvent
import org.edx.mobile.event.NetworkConnectivityChangeEvent
import org.edx.mobile.exception.AuthException
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.extenstion.decodeToLong
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.http.notifications.FullScreenErrorNotification
import org.edx.mobile.http.notifications.SnackbarErrorNotification
import org.edx.mobile.inapppurchases.BillingProcessor
import org.edx.mobile.inapppurchases.BillingProcessor.BillingFlowListeners
import org.edx.mobile.interfaces.RefreshListener
import org.edx.mobile.logger.Logger
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.api.EnrollmentResponse
import org.edx.mobile.model.course.EnrollmentMode
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.module.analytics.InAppPurchasesAnalytics
import org.edx.mobile.module.db.DataCallback
import org.edx.mobile.util.InAppPurchasesException
import org.edx.mobile.util.InAppPurchasesUtils
import org.edx.mobile.util.NetworkUtil
import org.edx.mobile.util.NonNullObserver
import org.edx.mobile.util.UiUtils
import org.edx.mobile.view.adapters.MyCoursesAdapter
import org.edx.mobile.view.dialog.AlertDialogFragment
import org.edx.mobile.view.dialog.CourseModalDialogFragment
import org.edx.mobile.view.dialog.FullscreenLoaderDialogFragment
import org.edx.mobile.viewModel.InAppPurchasesViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

@AndroidEntryPoint
class MyCoursesListFragment : OfflineSupportBaseFragment(), RefreshListener {
    private lateinit var adapter: MyCoursesAdapter
    private lateinit var binding: FragmentMyCoursesListBinding
    private val logger = Logger(javaClass.simpleName)
    private var refreshOnResume = false
    private var refreshOnPurchase = false
    private var isObserversInitialized = true
    private var lastClickTime: Long = 0
    private var incompletePurchases: List<Pair<String, String>> = emptyList()

    @Inject
    lateinit var courseAPI: CourseAPI

    @Inject
    lateinit var loginAPI: LoginAPI

    private val iapViewModel: InAppPurchasesViewModel
            by viewModels(ownerProducer = { requireActivity() })

    @Inject
    lateinit var iapAnalytics: InAppPurchasesAnalytics

    @Inject
    lateinit var iapUtils: InAppPurchasesUtils

    private lateinit var errorNotification: FullScreenErrorNotification
    private lateinit var enrolledCoursesCall: Call<EnrollmentResponse>
    private lateinit var billingProcessor: BillingProcessor
    private var fullscreenLoader: FullscreenLoaderDialogFragment? = null

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
            // Hide the progress bar as swipe layout has its own progress indicator
            binding.loadingIndicator.root.visibility = View.GONE
            errorNotification.hideError()
            loadData(showProgress = false, fromCache = false)
        }
        UiUtils.setSwipeRefreshLayoutColors(binding.swipeContainer)

        // Add empty view to cause divider to render at the top of the list.
        binding.myCourseList.addHeaderView(View(context), null, false)
        binding.myCourseList.adapter = adapter
        binding.myCourseList.onItemClickListener = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData(showProgress = true, fromCache = true)
    }

    private fun initObservers() {
        iapViewModel.executeResponse.observe(
            viewLifecycleOwner,
            NonNullObserver {
                if (iapViewModel.upgradeMode.isSilentMode()) {
                    showNewExperienceAlertDialog()
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
                    enrolledCoursesCall = courseAPI.enrolledCoursesWithoutStale
                    getUserEnrolledCourses(false)
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

                    // To start the upgrade process for other unfulfilled purchases if any
                    if (incompletePurchases.isNotEmpty()) {
                        purchaseCourse()
                    }
                }
            })

        iapViewModel.errorMessage.observe(
            viewLifecycleOwner,
            NonNullObserver { errorMsg ->
                if (iapViewModel.upgradeMode.isNormalMode()) return@NonNullObserver

                if (errorMsg.throwable is InAppPurchasesException) {
                    when (errorMsg.throwable.httpErrorCode) {
                        HttpStatus.UNAUTHORIZED -> {
                            environment.router?.forceLogout(
                                requireContext(),
                                environment.analyticsRegistry,
                                environment.notificationDelegate
                            )
                            return@NonNullObserver
                        }
                        else -> iapUtils.showPostUpgradeErrorDialog(
                            context = this,
                            errorCode = errorMsg.throwable.httpErrorCode,
                            errorMessage = errorMsg.throwable.errorMessage,
                            errorType = errorMsg.errorCode,
                            retryListener = { _, _ -> iapViewModel.executeOrder() },
                            cancelListener = { _, _ -> resetPurchase() }
                        )
                    }
                } else {
                    iapUtils.showPostUpgradeErrorDialog(
                        context = this,
                        errorType = errorMsg.errorCode,
                        retryListener = { _, _ ->
                            if (errorMsg.errorCode == ErrorMessage.EXECUTE_ORDER_CODE)
                                iapViewModel.executeOrder()
                            else
                                iapViewModel.refreshCourseData(true)
                        },
                        cancelListener = { _, _ -> resetPurchase() }

                    )
                }
                iapViewModel.errorMessageShown()
            }
        )
    }

    private fun showNewExperienceAlertDialog() {
        AlertDialogFragment.newInstance(
            getString(R.string.silent_course_upgrade_success_title),
            getString(R.string.silent_course_upgrade_success_message),
            getString(R.string.label_refresh_now),
            { _, _ ->
                iapViewModel.showFullScreenLoader(true)
            },
            getString(R.string.label_continue_without_update),
            { _, _ ->
                resetPurchase()
                if (incompletePurchases.isNotEmpty()) {
                    purchaseCourse()
                }
            }, false
        ).show(childFragmentManager, null)
    }

    private val dataCallback: DataCallback<Int> = object : DataCallback<Int>() {
        override fun onResult(result: Int) {}
        override fun onFail(ex: Exception) {
            logger.error(ex)
        }
    }

    override fun onResume() {
        super.onResume()
        if (refreshOnResume) {
            loadData(showProgress = false, fromCache = true)
            refreshOnResume = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        enrolledCoursesCall?.cancel()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true)
    fun onEventMainThread(event: EnrolledInCourseEvent?) {
        refreshOnResume = true
    }

    /**
     * Method to obtain enrolled courses data from api or cache
     * @param showProgress: show loading indicator if true, false else wise
     * @param fromCache: make cached api call if true, server api call else wise
     */
    private fun loadData(showProgress: Boolean, fromCache: Boolean) {
        if (showProgress) {
            binding.loadingIndicator.root.visibility = View.VISIBLE
            errorNotification.hideError()
        }
        enrolledCoursesCall =
            if (fromCache) courseAPI.enrolledCoursesFromCache else courseAPI.enrolledCourses
        getUserEnrolledCourses(fromCache)
    }

    private fun initInAppPurchaseSetup() {
        if (isAdded && environment.appFeaturesPrefs.isValuePropEnabled() &&
            isObserversInitialized
        ) {
            initFullscreenLoader()
            initObservers()
            isObserversInitialized = false
        }
    }

    private fun detectUnfulfilledPurchase(verifiedCoursesSku: List<String>?) {
        if (verifiedCoursesSku?.isEmpty() == null) return

        billingProcessor = BillingProcessor(requireContext(), object : BillingFlowListeners {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                super.onBillingSetupFinished(billingResult)

                billingProcessor.queryPurchase { _, purchases ->

                    if (purchases.isEmpty() || environment.loginPrefs.userId == null) return@queryPurchase

                    val purchasesList = purchases.filter {
                        it.accountIdentifiers?.obfuscatedAccountId?.decodeToLong() ==
                                environment.loginPrefs.userId
                    }.associate { it.skus[0] to it.purchaseToken }.toList()

                    handleIncompletePurchasesFlow(verifiedCoursesSku, purchasesList)
                }
            }
        })
    }

    /**
     * To detect and handle courses which are purchased but still not Verified
     *
     * @param verifiedCoursesSku: A list of SKUs of verified courses
     * @param purchasedCourses: A list of pairs of SKU and PurchaseToken of purchased courses
     *                           from Payment SDK
     */
    private fun handleIncompletePurchasesFlow(
        verifiedCoursesSku: List<String>,
        purchasedCourses: List<Pair<String, String>>
    ) {
        incompletePurchases = purchasedCourses.filter {
            it.first !in verifiedCoursesSku
        }.toMutableList()

        if (incompletePurchases.isNotEmpty()) {
            lifecycleScope.launch {
                purchaseCourse()
            }
        }
    }

    private fun purchaseCourse() {
        iapViewModel.upgradeMode = InAppPurchasesViewModel.UpgradeMode.SILENT
        iapViewModel.setPurchaseToken(incompletePurchases[0].second)
        iapViewModel.addProductToBasket(incompletePurchases[0].first)
        incompletePurchases = incompletePurchases.drop(1)
    }

    private fun getUserEnrolledCourses(fromCache: Boolean = false) {
        enrolledCoursesCall.enqueue(object : Callback<EnrollmentResponse> {
            override fun onResponse(
                call: Call<EnrollmentResponse>,
                response: Response<EnrollmentResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.let {
                        environment.appFeaturesPrefs.setAppConfig(it.appConfig)
                        populateCourseData(ArrayList(it.enrollments), isCachedData = fromCache)
                        initInAppPurchaseSetup()

                        // Fetch latest data from server in the background after displaying previously cached data
                        // Show loader if the cache data is empty
                        if (fromCache) {
                            loadData(
                                showProgress = it.enrollments.isEmpty(),
                                fromCache = false
                            )
                        } else {
                            resetPurchase()
                            if (incompletePurchases.isEmpty()) {
                                detectUnfulfilledPurchase(getVerifiedCoursesSku(it.enrollments))
                            }
                        }
                    } ?: addFindCoursesFooter().also { invalidateView() }
                } else if (fromCache) { // Fetch latest data from server if cache call's response is unSuccessful
                    loadData(showProgress = true, fromCache = false)
                } else {
                    when {
                        response.code() == HttpStatus.UNAUTHORIZED && context != null -> {
                            environment.router?.forceLogout(
                                context,
                                environment.analyticsRegistry,
                                environment.notificationDelegate
                            )
                        }
                        response.code() == HttpStatus.UPGRADE_REQUIRED -> {
                            context?.let { context ->
                                errorNotification.showError(
                                    context,
                                    HttpStatusException(response.code(), "")
                                )
                            }
                        }
                        adapter.isEmpty -> {
                            showError(HttpStatusException(response.code(), response.message()))
                        }
                    }
                    invalidateView()
                }
            }

            override fun onFailure(call: Call<EnrollmentResponse>, t: Throwable) {
                when {
                    call.isCanceled -> logger.error(t)
                    fromCache -> loadData(showProgress = true, fromCache = false)
                    (fullscreenLoader?.isAdded == true) -> iapViewModel.setError(
                        ErrorMessage.COURSE_REFRESH_CODE,
                        t
                    )
                    else -> {
                        if (t is AuthException || (t is HttpStatusException && t.statusCode == HttpStatus.UNAUTHORIZED)) {
                            environment.router?.forceLogout(
                                context,
                                environment.analyticsRegistry,
                                environment.notificationDelegate
                            )
                        } else if (adapter.isEmpty) {
                            showError(t)
                            invalidateView()
                        }
                    }
                }
            }
        })
    }

    private fun populateCourseData(
        data: ArrayList<EnrolledCoursesResponse>,
        isCachedData: Boolean = false
    ) {
        if (isCachedData.not()) {
            updateDatabaseAfterDownload(data)
        }
        if (data.isNotEmpty()) {
            adapter.setItems(data)
        }
        adapter.setValuePropEnabled(environment.appFeaturesPrefs.isValuePropEnabled())
        addFindCoursesFooter()
        adapter.notifyDataSetChanged()
        if (adapter.isEmpty && !environment.config.discoveryConfig.isDiscoveryEnabled) {
            errorNotification.showError(
                R.string.no_courses_to_display,
                R.drawable.ic_error, 0, null
            )
            binding.myCourseList.visibility = View.GONE
        } else {
            binding.myCourseList.visibility = View.VISIBLE
            errorNotification.hideError()
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

    /**
     * To get verified courses SKUs from enrolled courses response.
     */
    private fun getVerifiedCoursesSku(response: List<EnrolledCoursesResponse>): List<String> {
        return response.filter {
            EnrollmentMode.VERIFIED.toString().equals(it.mode, true)
        }.mapNotNull { it.courseSku }.toList()
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

    private fun updateDatabaseAfterDownload(list: ArrayList<EnrolledCoursesResponse>?) {
        if (list != null && list.size > 0) {
            //update all videos in the DB as Deactivated
            environment.database?.updateAllVideosAsDeactivated(dataCallback)
            for (i in list.indices) {
                //Check if the flag of isIs_active is marked to true,
                //then activate all videos
                if (list[i].isActive) {
                    //update all videos for a course fetched in the API as Activated
                    environment.database?.updateVideosActivatedForCourse(
                        list[i].course.id,
                        dataCallback
                    )
                } else {
                    list.removeAt(i)
                }
            }
            //Delete all videos which are marked as Deactivated in the database
            environment.storage?.deleteAllUnenrolledVideos()
        }
    }

    private fun invalidateView() {
        binding.swipeContainer.isRefreshing = false
        binding.loadingIndicator.root.visibility = View.GONE

        if (!EventBus.getDefault().isRegistered(this@MyCoursesListFragment)) {
            EventBus.getDefault().register(this@MyCoursesListFragment)
        }
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
        loadData(showProgress = true, fromCache = false)
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
