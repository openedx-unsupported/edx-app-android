package org.edx.mobile.view

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import de.greenrobot.event.EventBus
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
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
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.http.notifications.FullScreenErrorNotification
import org.edx.mobile.http.notifications.SnackbarErrorNotification
import org.edx.mobile.interfaces.RefreshListener
import org.edx.mobile.logger.Logger
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.module.db.DataCallback
import org.edx.mobile.util.ConfigUtil
import org.edx.mobile.util.NetworkUtil
import org.edx.mobile.util.NonNullObserver
import org.edx.mobile.util.UiUtils
import org.edx.mobile.view.adapters.MyCoursesAdapter
import org.edx.mobile.view.dialog.CourseModalDialogFragment
import org.edx.mobile.view.dialog.FullscreenLoaderDialogFragment
import org.edx.mobile.viewModel.InAppPurchasesViewModel
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
    private var lastClickTime: Long = 0

    @Inject
    lateinit var courseAPI: CourseAPI

    @Inject
    lateinit var loginAPI: LoginAPI

    private val iapViewModel: InAppPurchasesViewModel
            by viewModels(ownerProducer = { requireActivity() })

    private lateinit var errorNotification: FullScreenErrorNotification
    private lateinit var enrolledCoursesCall: Call<List<EnrolledCoursesResponse>>
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

            override fun onValuePropClicked(
                courseId: String,
                courseName: String,
                price: String,
                isSelfPaced: Boolean
            ) {
                //This time is checked to avoid taps in quick succession
                val currentTime = SystemClock.elapsedRealtime()
                if (currentTime - lastClickTime > MIN_CLICK_INTERVAL) {
                    lastClickTime = currentTime
                    CourseModalDialogFragment.newInstance(
                        environment.config.platformName,
                        Analytics.Screens.COURSE_ENROLLMENT,
                        courseId,
                        courseName,
                        price,
                        isSelfPaced
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
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_my_courses_list, container, false)
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
        binding.myCourseList.adapter = adapter.also {
            it.setValuePropEnabled(environment.remoteFeaturePrefs.isValuePropEnabled())
        }
        binding.myCourseList.onItemClickListener = adapter
        initInAppPurchaseSetup()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ConfigUtil.checkValuePropEnabled(
            environment.config,
            object : ConfigUtil.OnValuePropStatusListener {
                override fun isValuePropEnabled(isEnabled: Boolean) {
                    if (isEnabled != environment.remoteFeaturePrefs.isValuePropEnabled()) {
                        environment.remoteFeaturePrefs.setValuePropEnabled(isEnabled)
                        adapter.setValuePropEnabled(isEnabled)
                        adapter.notifyDataSetChanged()
                        initInAppPurchaseSetup()
                    }
                }
            })
        loadData(showProgress = true, fromCache = true)
    }

    private fun initInAppPurchaseSetup() {
        if (isAdded && environment.remoteFeaturePrefs.isValuePropEnabled()) {
            initFullscreenLoader()
            initObservers()
        }
    }

    private fun initObservers() {
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
                    fullscreenLoader?.dismiss()
                    SnackbarErrorNotification(binding.root).showError(R.string.purchase_success_message)
                    iapViewModel.resetPurchase(false)
                }
            })
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

    private fun getUserEnrolledCourses(fromCache: Boolean = false) {
        enrolledCoursesCall.enqueue(object : Callback<List<EnrolledCoursesResponse>> {
            override fun onResponse(
                call: Call<List<EnrolledCoursesResponse>>,
                response: Response<List<EnrolledCoursesResponse>>
            ) {
                if (response.isSuccessful && response.code() == HttpStatus.OK) {
                    populateCourseData(ArrayList(response.body()), isCachedData = fromCache)
                    // Fetch latest data from server in the background after displaying previously cached data
                    // Show loader if the cache data is empty
                    if (fromCache) {
                        loadData(
                            showProgress = response.body()?.isEmpty() == true,
                            fromCache = false
                        )
                    } else {
                        resetPurchase()
                    }
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
                                    context, HttpStatusException(
                                        Response.error<Any>(
                                            response.code(),
                                            ResponseBody.create(
                                                "text/plain".toMediaTypeOrNull(),
                                                ""
                                            )
                                        )
                                    ), 0, null
                                )
                            }
                        }
                        adapter.isEmpty -> {
                            showError(
                                HttpStatusException(
                                    Response.error<Any>(
                                        response.code(),
                                        ResponseBody.create(
                                            "text/plain".toMediaTypeOrNull(),
                                            response.message()
                                        )
                                    )
                                )
                            )
                        }
                    }
                    invalidateView()
                }
            }

            override fun onFailure(call: Call<List<EnrolledCoursesResponse>>, t: Throwable) {
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
        if (data.size > 0) {
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

    private fun resetPurchase() {
        if (refreshOnPurchase && fullscreenLoader?.isAdded == true) {
            Timer("", false).schedule(
                FullscreenLoaderDialogFragment.FULLSCREEN_DISPLAY_DELAY
            ) {
                refreshOnPurchase = false
                iapViewModel.resetPurchase(true)
            }
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
            EventBus.getDefault().registerSticky(this@MyCoursesListFragment)
        }
    }

    private fun showError(error: Throwable) {
        context?.let { context ->
            error.cause?.let { cause ->
                errorNotification.showError(context, cause, R.string.lbl_reload) {
                    if (NetworkUtil.isConnected(context)) {
                        onRefresh()
                    }
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

    fun onEvent(event: MainDashboardRefreshEvent?) {
        loadData(showProgress = true, fromCache = false)
    }

    override fun onRevisit() {
        super.onRevisit()
        if (NetworkUtil.isConnected(activity)) {
            binding.swipeContainer.isEnabled = true
        }
    }

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
