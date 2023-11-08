package org.edx.mobile.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.R
import org.edx.mobile.base.BaseFragmentActivity
import org.edx.mobile.databinding.FragmentCourseHomeBinding
import org.edx.mobile.deeplink.Screen
import org.edx.mobile.deeplink.ScreenDef
import org.edx.mobile.event.CourseDashboardRefreshEvent
import org.edx.mobile.event.CourseOutlineRefreshEvent
import org.edx.mobile.event.LogoutEvent
import org.edx.mobile.event.MediaStatusChangeEvent
import org.edx.mobile.event.NetworkConnectivityChangeEvent
import org.edx.mobile.event.RefreshCourseDashboardEvent
import org.edx.mobile.exception.CourseContentNotValidException
import org.edx.mobile.extenstion.isNotNullOrEmpty
import org.edx.mobile.extenstion.parcelable
import org.edx.mobile.extenstion.serializableOrThrow
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.http.notifications.FullScreenErrorNotification
import org.edx.mobile.logger.Logger
import org.edx.mobile.model.api.CourseUpgradeResponse
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.model.course.HasDownloadEntry
import org.edx.mobile.model.course.SectionRow
import org.edx.mobile.model.course.VideoBlockModel
import org.edx.mobile.module.storage.BulkVideosDownloadCancelledEvent
import org.edx.mobile.module.storage.DownloadCompletedEvent
import org.edx.mobile.module.storage.DownloadedVideoDeletedEvent
import org.edx.mobile.util.AppConstants
import org.edx.mobile.util.MediaConsentUtils
import org.edx.mobile.util.NetworkUtil
import org.edx.mobile.util.NonNullObserver
import org.edx.mobile.util.UiUtils
import org.edx.mobile.util.observer.EventObserver
import org.edx.mobile.view.adapters.CourseHomeAdapter
import org.edx.mobile.view.dialog.DownloadSizeExceedDialog
import org.edx.mobile.view.dialog.IDialogCallback
import org.edx.mobile.viewModel.CourseViewModel
import org.edx.mobile.viewModel.CourseViewModel.CoursesRequestType
import org.edx.mobile.viewModel.VideoViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class CourseHomeTabFragment : OfflineSupportBaseFragment(), CourseHomeAdapter.OnItemClickListener {

    private val logger = Logger(javaClass.name)
    private lateinit var binding: FragmentCourseHomeBinding
    private lateinit var adapter: CourseHomeAdapter

    private val courseViewModel: CourseViewModel by viewModels()
    private val videoViewModel: VideoViewModel by viewModels()

    private var courseUpgradeData: CourseUpgradeResponse? = null
    private lateinit var courseData: EnrolledCoursesResponse
    private var courseComponentId: String? = null
    private var downloadEntries: MutableList<HasDownloadEntry>? = null

    private lateinit var errorNotification: FullScreenErrorNotification
    private var screenName: String? = null
    private var refreshOnResume = false

    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                onPermissionGranted()
            } else {
                showPermissionDeniedMessage()
                onPermissionDenied()
            }
        }

    private val courseUnitDetailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val resultData = result.data
        if (result.resultCode == Activity.RESULT_OK && resultData != null) {
            // Check if the course has been upgraded
            val isCourseUpgraded = resultData.getBooleanExtra(AppConstants.COURSE_UPGRADED, false)

            if (isCourseUpgraded) {
                fetchCourseComponents(CoursesRequestType.LIVE)
                // Post a refresh event for the course dashboard toolbar
                if (EventBus.getDefault().isRegistered(this).not())
                    EventBus.getDefault().register(this)
                EventBus.getDefault().post(RefreshCourseDashboardEvent())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            courseData =
                this.serializableOrThrow(Router.EXTRA_COURSE_DATA) as EnrolledCoursesResponse
            courseUpgradeData = this.parcelable(Router.EXTRA_COURSE_UPGRADE_DATA)
            courseComponentId = this.getString(Router.EXTRA_COURSE_COMPONENT_ID)
            screenName = this.getString(Router.EXTRA_SCREEN_NAME)
        } ?: run {
            throw IllegalStateException("No arguments available")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCourseHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        errorNotification = FullScreenErrorNotification(binding.swipeContainer)
        initCourseObservers()
        initVideoObserver()
        fetchCourseComponents()
        binding.swipeContainer.setOnRefreshListener {
            fetchCourseComponents(coursesRequestType = CoursesRequestType.LIVE)
        }
        adapter = CourseHomeAdapter(environment.database, this@CourseHomeTabFragment)
        binding.rvCourseSections.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        if (refreshOnResume) {
            fetchCourseComponents(coursesRequestType = CoursesRequestType.LIVE)
            refreshOnResume = false
        }
    }

    override fun onRevisit() {
        super.onRevisit()
        courseViewModel.getCourseStatusInfo(courseData.courseId)
    }

    private fun fetchCourseComponents(
        coursesRequestType: CoursesRequestType = CoursesRequestType.APP_LEVEL_CACHE
    ) {
        // Prepare the loader. Either re-connect with an existing one or start a new one.
        if (environment.loginPrefs.isUserLoggedIn) {
            val courseId: String = courseData.courseId
            courseViewModel.getCourseData(
                courseId,
                showProgress = true,
                swipeRefresh = false,
                coursesRequestType = coursesRequestType
            )
            courseViewModel.getCourseStatusInfo(courseId)
        } else {
            EventBus.getDefault().post(LogoutEvent())
        }
    }

    private fun initCourseObservers() {
        courseViewModel.courseComponent.observe(
            viewLifecycleOwner,
            EventObserver { courseComponent ->
                if (isAdded) {
                    loadData(courseComponent)
                }
            })

        courseViewModel.lastAccessedComponent.observe(
            viewLifecycleOwner,
            EventObserver { lastAccessedComponent ->
                if (isAdded) {
                    showResumeCourseView(lastAccessedComponent)
                }
            })

        courseViewModel.showProgress.observe(viewLifecycleOwner, NonNullObserver { showProgress ->
            binding.loadingIndicator.loadingIndicator.setVisibility(showProgress)
        })

        courseViewModel.swipeRefresh.observe(viewLifecycleOwner, NonNullObserver { swipeRefresh ->
            binding.swipeContainer.isRefreshing = swipeRefresh
        })

        courseViewModel.handleError.observe(viewLifecycleOwner, NonNullObserver { throwable ->
            if (!isAdded) {
                return@NonNullObserver
            }
            if (throwable is HttpStatusException && throwable.statusCode == HttpStatus.UNAUTHORIZED) {
                EventBus.getDefault().post(LogoutEvent())
            } else {
                if (throwable is CourseContentNotValidException) {
                    errorNotification.showError(requireContext(), throwable)
                    logger.error(throwable, true)
                } else {
                    errorNotification.showError(
                        requireContext(),
                        throwable,
                        R.string.lbl_reload
                    ) {
                        if (NetworkUtil.isConnected(requireContext())) {
                            onRefresh()
                        }
                    }
                }

                if (!EventBus.getDefault().isRegistered(this)) {
                    EventBus.getDefault().register(this)
                }
            }
        })
    }

    /**
     * Adds resume course item view in the ListView.
     *
     * @param lastAccessedComponent The last accessed component
     */
    private fun showResumeCourseView(lastAccessedComponent: CourseComponent) {
        val sections = adapter.currentList.toMutableList()
        val resumeCourseItem = adapter.currentList.getOrNull(0)
        // if lastAccessedComponent is already exist
        if (resumeCourseItem?.type == SectionRow.RESUME_COURSE_ITEM) {
            sections[0] = SectionRow(SectionRow.RESUME_COURSE_ITEM, lastAccessedComponent)
        } else {
            sections.add(0, SectionRow(SectionRow.RESUME_COURSE_ITEM, lastAccessedComponent))
        }
        adapter.submitList(sections) {
            binding.rvCourseSections.smoothScrollToPosition(0)
        }
    }

    private fun loadData(courseComponent: CourseComponent) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        // retain the existing lastAccessedComponent
        val sectionList = courseComponent.sectionData.toMutableList()
        val resumeCourseItem = adapter.currentList.getOrNull(0)
        if (resumeCourseItem?.type == SectionRow.RESUME_COURSE_ITEM) {
            sectionList.add(0, resumeCourseItem)
        }
        adapter.submitList(sectionList)
        updateListUI()
        detectDeepLinking()
    }

    private fun detectDeepLinking() {
        if (Screen.COURSE_COMPONENT.equals(screenName, true)
            && courseComponentId.isNotNullOrEmpty()
        ) {
            val courseUnitDetailIntent = environment.router.getCourseUnitDetailIntent(
                requireActivity(),
                courseData,
                courseUpgradeData,
                courseComponentId,
                false
            )
            courseUnitDetailLauncher.launch(courseUnitDetailIntent)
            arguments?.putString(Router.EXTRA_SCREEN_NAME, null)
            screenName = null
        }
    }

    override fun onSectionItemClick(
        itemView: LinearLayout, parentPosition: Int, childPosition: Int
    ) {
        adapter.getItem(parentPosition, childPosition)?.let { component ->
            showComponentDetailScreen(component)
        }
    }

    override fun resumeCourseClicked(lastAccessedComponent: CourseComponent) {
        showComponentDetailScreen(lastAccessedComponent)
        environment.analyticsRegistry.trackResumeCourseBannerTapped(
            lastAccessedComponent.courseId,
            lastAccessedComponent.id
        )
    }

    private fun showComponentDetailScreen(component: CourseComponent) {
        courseUnitDetailLauncher.launch(
            environment.router.getCourseUnitDetailIntent(
                requireActivity(),
                courseData,
                courseUpgradeData,
                component.id,
                false
            )
        )
    }

    override fun onSectionItemLongClick(
        itemView: LinearLayout, parentPosition: Int, childPosition: Int
    ) {
        val bulkDownloadIcon: View? = itemView.findViewById(R.id.bulk_download)
        if (bulkDownloadIcon?.tag as Int? == R.drawable.ic_download_done) {
            val checkItemPosition = Pair(parentPosition, childPosition)
            VideoMoreOptionsBottomSheet.newInstance(checkItemPosition)
                .show(childFragmentManager, null)
            adapter.setItemChecked(
                parentPosition,
                childPosition
            )
        }
    }

    override fun viewDownloadsStatus() {
        environment.router.showDownloads(activity)
    }

    override fun download(downloadableVideos: List<HasDownloadEntry>) {
        downloadEntries = downloadableVideos.toMutableList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            onPermissionGranted()
        } else {
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun initVideoObserver() {
        videoViewModel.refreshUI.observe(viewLifecycleOwner, EventObserver {
            if (it) {
                updateListUI()
            }
        })

        videoViewModel.infoMessage.observe(viewLifecycleOwner, EventObserver { strResId ->
            showInfoMessage(getString(strResId))
        })

        videoViewModel.downloadSizeExceeded.observe(viewLifecycleOwner, EventObserver {
            if (it.isNotEmpty()) {
                DownloadSizeExceedDialog.newInstance(object : IDialogCallback {
                    override fun onPositiveClicked() {
                        videoViewModel.startDownload(it)
                    }

                    override fun onNegativeClicked() {
                        EventBus.getDefault().post(BulkVideosDownloadCancelledEvent())
                    }
                }).show(childFragmentManager, "dialog")
            }
        })

        videoViewModel.selectedVideosPosition.observe(
            viewLifecycleOwner,
            EventObserver { position: Pair<Int, Int> ->
                if (position.first != RecyclerView.NO_POSITION) {
                    adapter.clearChoicesAndUpdateUI(refresh = false)
                    deleteDownloadedVideosAtPosition(position)
                }
            })

        videoViewModel.clearChoices.observe(
            viewLifecycleOwner,
            EventObserver { shouldClear: Boolean ->
                if (shouldClear) {
                    adapter.clearChoicesAndUpdateUI()
                }
            })
    }

    private fun deleteDownloadedVideosAtPosition(position: Pair<Int, Int>) {
        // Change the icon to download icon immediately
        val outerViewHolder =
            binding.rvCourseSections.findViewHolderForLayoutPosition(position.first)
        val innerViewHolder =
            outerViewHolder?.itemView?.findViewById<RecyclerView>(R.id.rv_sub_section)
                ?.findViewHolderForAdapterPosition(position.second)
        if (innerViewHolder?.itemView != null) {
            // rowView will be null, if the user scrolls away from the checked item
            val bulkDownloadIcon =
                innerViewHolder.itemView.findViewById<AppCompatImageView>(R.id.bulk_download)
            bulkDownloadIcon.setImageDrawable(
                UiUtils.getDrawable(requireContext(), R.drawable.ic_download)
            )
            bulkDownloadIcon.tag = R.drawable.ic_download
        }
        val rowItem = adapter.getItem(position.first, position.second)
        rowItem?.let {
            val videos = rowItem.getVideos(true)
            environment.analyticsRegistry.trackSubsectionVideosDelete(
                courseData.courseId,
                rowItem.id
            )
            showVideosDeletedSnackBar(rowItem, videos)
        }
    }

    private fun showVideosDeletedSnackBar(
        courseComponent: CourseComponent,
        videos: List<CourseComponent>
    ) {
        /*
          The android docs have NOT been updated yet, but if you jump into the source code you'll
          notice that the parameter to the method setDuration(int duration) can either be one of
          LENGTH_SHORT, LENGTH_LONG, LENGTH_INDEFINITE or a custom duration in milliseconds.

          https://stackoverflow.com/a/30552666
          https://github.com/material-components/material-components-android/commit/2cb77c9331cc3c6a5034aace0238b96508acf47d
         */
        @SuppressLint("WrongConstant") val snackBar = Snackbar.make(
            binding.rvCourseSections,
            resources.getQuantityString(
                R.plurals.delete_video_snackbar_msg,
                videos.size,
                videos.size
            ),
            AppConstants.SNACKBAR_SHOWTIME_MS
        )
        snackBar.setAction(R.string.label_undo) { }
        snackBar.addCallback(object : BaseCallback<Snackbar?>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                // SnackBar is being dismissed by any action other than its action button's press
                if (event != DISMISS_EVENT_ACTION) {
                    val storage = environment.storage
                    for (video in videos) {
                        val videoBlockModel = video as VideoBlockModel
                        val downloadEntry = videoBlockModel.getDownloadEntry(storage)
                        if (downloadEntry != null && downloadEntry.isDownloaded) {
                            // This check is necessary because, this callback gets called multiple
                            // times when SnackBar is about to dismiss and the activity finishes
                            storage.removeDownload(downloadEntry)
                        } else {
                            return
                        }
                    }
                } else {
                    environment.analyticsRegistry.trackUndoingSubsectionVideosDelete(
                        courseData.courseId, courseComponent.id
                    )
                }
                updateListUI()
            }
        })
        snackBar.show()
    }

    private fun onPermissionGranted() {
        MediaConsentUtils.requestStreamMedia(requireActivity(), object : IDialogCallback {
            override fun onPositiveClicked() {
                videoViewModel.downloadMultipleVideos(downloadEntries)
            }

            override fun onNegativeClicked() {
                showInfoMessage(getString(R.string.wifi_off_message))
                EventBus.getDefault().post(BulkVideosDownloadCancelledEvent())
            }
        })
    }

    private fun onPermissionDenied() {
        if (downloadEntries != null) {
            downloadEntries?.clear()
            downloadEntries = null
        }
    }

    fun updateListUI() {
        adapter.updateList()
    }

    fun showInfoMessage(message: String?): Boolean {
        return activity is BaseFragmentActivity &&
                (activity as BaseFragmentActivity).showInfoMessage(message)
    }

    fun onRefresh() {
        EventBus.getDefault().post(CourseDashboardRefreshEvent())
    }

    override fun isShowingFullScreenError(): Boolean {
        return errorNotification.isShowing
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(sticky = true)
    fun onEventMainThread(event: MediaStatusChangeEvent) {
        updateListUI()
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: DownloadCompletedEvent) {
        updateListUI()
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: DownloadedVideoDeletedEvent) {
        updateListUI()
    }

    @Subscribe(sticky = true)
    fun onEvent(event: NetworkConnectivityChangeEvent) {
        onNetworkConnectivityChangeEvent(event)
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(sticky = true)
    fun onEvent(event: CourseDashboardRefreshEvent) {
        errorNotification.hideError()
        fetchCourseComponents()
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(sticky = true)
    fun onEvent(event: CourseOutlineRefreshEvent) {
        errorNotification.hideError()
        refreshOnResume = true
    }

    companion object {
        @JvmStatic
        fun makeArguments(
            model: EnrolledCoursesResponse,
            courseComponentId: String?,
            @ScreenDef screenName: String?
        ): Bundle {
            val courseBundle = Bundle()
            courseBundle.putSerializable(Router.EXTRA_COURSE_DATA, model)
            courseBundle.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId)
            courseBundle.putString(Router.EXTRA_SCREEN_NAME, screenName)
            return courseBundle
        }
    }
}
