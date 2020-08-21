package org.edx.mobile.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.joanzapata.iconify.fonts.FontAwesomeIcons
import de.greenrobot.event.EventBus
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.edx.mobile.R
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.course.CourseAPI
import org.edx.mobile.databinding.FragmentMyCoursesListBinding
import org.edx.mobile.databinding.PanelFindCourseBinding
import org.edx.mobile.deeplink.Screen
import org.edx.mobile.event.EnrolledInCourseEvent
import org.edx.mobile.event.MainDashboardRefreshEvent
import org.edx.mobile.event.MoveToDiscoveryTabEvent
import org.edx.mobile.event.NetworkConnectivityChangeEvent
import org.edx.mobile.exception.AuthException
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.http.notifications.FullScreenErrorNotification
import org.edx.mobile.interfaces.RefreshListener
import org.edx.mobile.logger.Logger
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.module.db.DataCallback
import org.edx.mobile.util.ConfigUtil.Companion.isCourseDiscoveryEnabled
import org.edx.mobile.util.NetworkUtil
import org.edx.mobile.util.UiUtil
import org.edx.mobile.view.adapters.MyCoursesAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject

class MyCoursesListFragment : OfflineSupportBaseFragment(), RefreshListener {
    private lateinit var adapter: MyCoursesAdapter
    private lateinit var binding: FragmentMyCoursesListBinding
    private val logger = Logger(javaClass.simpleName)
    private var refreshOnResume = false

    @Inject
    private lateinit var environment: IEdxEnvironment

    @Inject
    private lateinit var courseAPI: CourseAPI
    private lateinit var errorNotification: FullScreenErrorNotification

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
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_courses_list, container, false)
        errorNotification = FullScreenErrorNotification(binding.myCourseList)
        binding.swipeContainer.setOnRefreshListener {
            // Hide the progress bar as swipe layout has its own progress indicator
            binding.loadingIndicator.root.visibility = View.GONE
            errorNotification.hideError()
            loadData(showProgress = false, isCached = false)
        }
        UiUtil.setSwipeRefreshLayoutColors(binding.swipeContainer)
        // Add empty view to cause divider to render at the top of the list.
        binding.myCourseList.addHeaderView(View(context), null, false)
        binding.myCourseList.adapter = adapter
        binding.myCourseList.onItemClickListener = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData(showProgress = true, isCached = true)
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
            loadData(showProgress = false, isCached = true)
            refreshOnResume = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    fun onEventMainThread(event: EnrolledInCourseEvent?) {
        refreshOnResume = true
    }

    /**
     * Method to obtain enrolled courses data from api or cache
     * @param showProgress: show loading indicator if true, false else wise
     * @param isCached: make cached api call if true, server api call else wise
     */
    private fun loadData(showProgress: Boolean, isCached: Boolean) {
        if (showProgress) {
            binding.loadingIndicator.root.visibility = View.VISIBLE
            errorNotification.hideError()
        }
        if (isCached) {
            getUserEnrolledCourses(courseAPI.enrolledCoursesFromCache, isCached = true)
        } else {
            getUserEnrolledCourses(courseAPI.enrolledCourses)
        }
    }

    private fun getUserEnrolledCourses(enrolledCoursesApi: Call<List<EnrolledCoursesResponse>>, isCached: Boolean = false) {
        enrolledCoursesApi.enqueue(object : Callback<List<EnrolledCoursesResponse>> {
            override fun onResponse(call: Call<List<EnrolledCoursesResponse>>, response: Response<List<EnrolledCoursesResponse>>) {
                if (response.isSuccessful && response.code() == HttpStatus.OK) {
                    populateCourseData(ArrayList(response.body()), isCachedData = isCached)
                    if (isCached) {
                        getUserEnrolledCourses(courseAPI.enrolledCourses)
                    }
                } else if (isCached) {
                    getUserEnrolledCourses(courseAPI.enrolledCourses)
                } else {
                    when {
                        response.code() == HttpStatus.UNAUTHORIZED -> {
                            environment.router?.forceLogout(context,
                                    environment.analyticsRegistry,
                                    environment.notificationDelegate)
                        }
                        response.code() == HttpStatus.UPGRADE_REQUIRED -> {
                            context?.let { context ->
                                errorNotification.showError(context, HttpStatusException(Response.error<Any>(response.code(),
                                        ResponseBody.create(MediaType.parse("text/plain"), ""))), 0, null)
                            }
                        }
                        adapter.isEmpty -> {
                            showError(HttpStatusException(Response.error<Any>(response.code(),
                                    ResponseBody.create(MediaType.parse("text/plain"), response.message()))))
                        }
                    }
                }
                invalidateView()
            }

            override fun onFailure(call: Call<List<EnrolledCoursesResponse>>, t: Throwable) {
                if (isCached) {
                    getUserEnrolledCourses(courseAPI.enrolledCourses)
                } else {
                    if (t is AuthException || (t is HttpStatusException && t.statusCode == HttpStatus.UNAUTHORIZED)) {
                        environment.router?.forceLogout(context,
                                environment.analyticsRegistry,
                                environment.notificationDelegate)
                    } else if (adapter.isEmpty) {
                        showError(t)
                        invalidateView()
                    }
                }
            }
        })

    }

    private fun populateCourseData(data: ArrayList<EnrolledCoursesResponse>, isCachedData: Boolean = false) {
        if (isCachedData.not()) {
            updateDatabaseAfterDownload(data)
        }
        if (data.size > 0) {
            adapter.setItems(data)
        }
        addFindCoursesFooter()
        adapter.notifyDataSetChanged()
        if (adapter.isEmpty && !isCourseDiscoveryEnabled(environment)) {
            errorNotification.showError(R.string.no_courses_to_display,
                    FontAwesomeIcons.fa_exclamation_circle, 0, null)
            binding.myCourseList.visibility = View.GONE
        } else {
            binding.myCourseList.visibility = View.VISIBLE
            errorNotification.hideError()
        }
    }

    private fun updateDatabaseAfterDownload(list: ArrayList<EnrolledCoursesResponse>?) {
        if (list != null && list.size > 0) {
            //update all videos in the DB as Deactivated
            environment.database?.updateAllVideosAsDeactivated(dataCallback)
            for (i in list.indices) {
                //Check if the flag of isIs_active is marked to true,
                //then activate all videos
                if (list[i].isIs_active) {
                    //update all videos for a course fetched in the API as Activated
                    environment.database?.updateVideosActivatedForCourse(list[i].course.id,
                            dataCallback)
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
        if (isCourseDiscoveryEnabled(environment)) {
            // Add 'Find a Course' list item as a footer.
            val footer: PanelFindCourseBinding = DataBindingUtil.inflate(LayoutInflater.from(activity),
                    R.layout.panel_find_course, binding.myCourseList, false)
            binding.myCourseList.addFooterView(footer.root, null, false)
            footer.courseBtn.setOnClickListener {
                environment.analyticsRegistry?.trackUserFindsCourses()
                EventBus.getDefault().post(MoveToDiscoveryTabEvent(Screen.COURSE_DISCOVERY))
            }
        }
        // Add empty view to cause divider to render at the bottom of the list.
        binding.myCourseList.addFooterView(View(context), null, false)
    }

    override fun onRefresh() {
        EventBus.getDefault().post(MainDashboardRefreshEvent())
    }

    fun onEvent(event: MainDashboardRefreshEvent?) {
        loadData(showProgress = true, isCached = false)
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
