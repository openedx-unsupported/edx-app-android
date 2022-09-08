package org.edx.mobile.view

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.BuildConfig.VERSION_NAME
import org.edx.mobile.R
import org.edx.mobile.base.BaseFragment
import org.edx.mobile.databinding.FragmentCourseDatesPageBinding
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.http.notifications.FullScreenErrorNotification
import org.edx.mobile.http.notifications.SnackbarErrorNotification
import org.edx.mobile.interfaces.OnDateBlockListener
import org.edx.mobile.model.CourseDatesCalendarSync
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.course.CourseBannerInfoModel
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.util.*
import org.edx.mobile.view.adapters.CourseDatesAdapter
import org.edx.mobile.view.dialog.AlertDialogFragment
import org.edx.mobile.viewModel.CourseDateViewModel

@AndroidEntryPoint
class CourseDatesPageFragment : OfflineSupportBaseFragment(), BaseFragment.PermissionListener {

    private lateinit var errorNotification: FullScreenErrorNotification

    private lateinit var binding: FragmentCourseDatesPageBinding
    private val viewModel: CourseDateViewModel by viewModels()

    private var onDateItemClick: OnDateBlockListener = object : OnDateBlockListener {
        override fun onClick(link: String, blockId: String) {
            val component =
                courseManager.getComponentByIdFromAppLevelCache(courseData.courseId, blockId)
            if (blockId.isNotEmpty() && component != null) {
                environment.router.showCourseUnitDetail(
                    this@CourseDatesPageFragment,
                    REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, null, blockId, false
                )
                environment.analyticsRegistry.trackDatesCourseComponentTapped(
                    courseData.courseId,
                    component.id,
                    component.type.toString().toLowerCase(),
                    link
                )
            } else {
                showOpenInBrowserDialog(link)
                if (blockId.isNotEmpty()) {
                    environment.analyticsRegistry.trackUnsupportedComponentTapped(
                        courseData.courseId,
                        blockId,
                        link
                    )
                }
            }
        }
    }
    private lateinit var courseData: EnrolledCoursesResponse
    private var isSelfPaced: Boolean = true
    private var isDeepLinkEnabled: Boolean = false
    private lateinit var calendarTitle: String
    private lateinit var accountName: String
    private lateinit var keyValMap: Map<String, CharSequence>
    private var isCalendarExist: Boolean = false
    private lateinit var loaderDialog: AlertDialogFragment


    companion object {
        @JvmStatic
        fun makeArguments(courseData: EnrolledCoursesResponse): Bundle {
            val courseBundle = Bundle()
            courseBundle.putSerializable(Router.EXTRA_COURSE_DATA, courseData)
            return courseBundle
        }
    }

    override fun isShowingFullScreenError(): Boolean {
        return errorNotification.isShowing
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_course_dates_page, container, false)
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SHOW_COURSE_UNIT_DETAIL && resultCode == Activity.RESULT_OK
            && data != null
        ) {
            val outlineComp: CourseComponent? =
                courseManager.getCourseDataFromAppLevelCache(courseData.courseId)
            outlineComp?.let {
                navigateToCourseUnit(data, courseData, outlineComp)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionListener = this

        courseData = arguments?.getSerializable(Router.EXTRA_COURSE_DATA) as EnrolledCoursesResponse
        isSelfPaced = courseData.course.isSelfPaced
        calendarTitle = CalendarUtils.getCourseCalendarTitle(environment, courseData.course.name)
        accountName = CalendarUtils.getUserAccountForSync(environment)
        keyValMap = mapOf(
            AppConstants.PLATFORM_NAME to environment.config.platformName,
            AppConstants.COURSE_NAME to calendarTitle
        )

        errorNotification = FullScreenErrorNotification(binding.swipeContainer)
        loaderDialog = AlertDialogFragment.newInstance(
            R.string.title_syncing_calendar,
            R.layout.alert_dialog_progress
        )

        binding.swipeContainer.setOnRefreshListener {
            // Hide the progress bar as swipe layout has its own progress indicator
            binding.loadingIndicator.loadingIndicator.visibility = View.GONE
            errorNotification.hideError()
            viewModel.fetchCourseDates(
                courseID = courseData.courseId,
                forceRefresh = true,
                showLoader = false,
                isSwipeRefresh = true
            )
        }
        UiUtils.setSwipeRefreshLayoutColors(binding.swipeContainer)
        initObserver()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchCourseDates(
            courseID = courseData.courseId,
            forceRefresh = false,
            showLoader = true,
            isSwipeRefresh = false
        )
    }

    private fun initObserver() {
        viewModel.showLoader.observe(viewLifecycleOwner, Observer { showLoader ->
            binding.loadingIndicator.loadingIndicator.visibility =
                if (showLoader) View.VISIBLE else View.GONE
        })

        viewModel.bannerInfo.observe(viewLifecycleOwner, Observer {
            initDatesBanner(it)
        })

        viewModel.syncLoader.observe(viewLifecycleOwner, Observer { syncLoader ->
            if (syncLoader) {
                loaderDialog.isCancelable = false
                loaderDialog.showNow(childFragmentManager, null)
            } else {
                checkIfCalendarExists()
                dismissLoader()
            }
        })

        viewModel.courseDates.observe(viewLifecycleOwner, Observer { dates ->
            if (dates.courseDateBlocks.isNullOrEmpty()) {
                viewModel.setError(
                    ErrorMessage.COURSE_DATES_CODE,
                    HttpStatus.NO_CONTENT,
                    getString(R.string.course_dates_unavailable_message)
                )
            } else {
                dates.organiseCourseDates()
                binding.rvDates.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = CourseDatesAdapter(dates.courseDatesMap, onDateItemClick)
                }
                val outdatedCalenderId = CalendarUtils.isCalendarOutOfDate(
                    requireContext(),
                    accountName,
                    calendarTitle,
                    dates.courseDateBlocks
                )
                if (outdatedCalenderId != -1L) {
                    showCalendarOutOfDateDialog(outdatedCalenderId)
                }
            }
        })

        viewModel.resetCourseDates.observe(viewLifecycleOwner, Observer { resetCourseDates ->
            if (resetCourseDates != null) {
                if (!CalendarUtils.isCalendarExists(contextOrThrow, accountName, calendarTitle)) {
                    showShiftDateSnackBar(true)
                }
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMsg ->
            if (errorMsg != null) {
                if (errorMsg.throwable is HttpStatusException) {
                    when (errorMsg.throwable.statusCode) {
                        HttpStatus.UNAUTHORIZED -> {
                            environment.router?.forceLogout(
                                contextOrThrow,
                                environment.analyticsRegistry,
                                environment.notificationDelegate
                            )
                            return@Observer
                        }
                        else ->
                            errorNotification.showError(
                                contextOrThrow,
                                errorMsg.throwable,
                                -1,
                                null
                            )
                    }
                } else {
                    when (errorMsg.errorCode) {
                        ErrorMessage.COURSE_DATES_CODE ->
                            errorNotification.showError(
                                contextOrThrow,
                                errorMsg.throwable,
                                -1,
                                null
                            )
                        ErrorMessage.BANNER_INFO_CODE ->
                            initDatesBanner(null)
                        ErrorMessage.COURSE_RESET_DATES_CODE ->
                            showShiftDateSnackBar(false)
                    }
                }
            }
        })

        viewModel.swipeRefresh.observe(viewLifecycleOwner, Observer { enableSwipeListener ->
            binding.swipeContainer.isRefreshing = enableSwipeListener
        })
    }

    private fun showCalendarOutOfDateDialog(calendarId: Long) {
        val alertDialogFragment =
            AlertDialogFragment.newInstance(getString(R.string.title_calendar_out_of_date),
                getString(R.string.message_calendar_out_of_date),
                getString(R.string.label_update_now),
                { _: DialogInterface?, _: Int ->
                    trackCalendarEvent(
                        Analytics.Events.CALENDAR_SYNC_UPDATE,
                        Analytics.Values.CALENDAR_SYNC_UPDATE
                    )
                    val newCalId = CalendarUtils.createOrUpdateCalendar(
                        context = contextOrThrow,
                        accountName = accountName,
                        calendarTitle = calendarTitle
                    )
                    viewModel.addOrUpdateEventsInCalendar(
                        contextOrThrow,
                        newCalId,
                        courseData.courseId,
                        courseData.course.name,
                        isDeepLinkEnabled,
                        true
                    )
                },
                getString(R.string.label_remove_course_calendar),
                { _: DialogInterface?, _: Int ->
                    trackCalendarEvent(
                        Analytics.Events.CALENDAR_SYNC_REMOVE,
                        Analytics.Values.CALENDAR_SYNC_REMOVE
                    )
                    deleteCalendar(calendarId)
                    binding.switchSync.isChecked = false
                })
        alertDialogFragment.isCancelable = false
        alertDialogFragment.show(childFragmentManager, null)
    }

    /**
     * Initialized dates info banner on CourseDatesPageFragment
     *
     * @param courseBannerInfo object of course deadline info
     */
    private fun initDatesBanner(courseBannerInfo: CourseBannerInfoModel?) {
        if (courseBannerInfo == null || courseBannerInfo.hasEnded) {
            binding.banner.containerLayout.visibility = View.GONE
            binding.syncCalendarContainer.visibility = View.GONE
            return
        }
        ConfigUtil.checkCalendarSyncEnabled(
            environment.config,
            object : ConfigUtil.OnCalendarSyncListener {
                override fun onCalendarSyncResponse(response: CourseDatesCalendarSync) {
                    if (!response.disabledVersions.contains(VERSION_NAME) && ((response.isSelfPlacedEnable && isSelfPaced) || (response.isInstructorPlacedEnable && !isSelfPaced))) {
                        binding.syncCalendarContainer.visibility = View.VISIBLE
                        isDeepLinkEnabled = response.isDeepLinkEnabled
                        initializedSyncContainer()
                    }
                }
            })

        CourseDateUtil.setupCourseDatesBanner(view = binding.banner.root,
            isCourseDatePage = true,
            courseId = courseData.courseId,
            enrollmentMode = courseData.mode,
            isSelfPaced = isSelfPaced,
            screenName = Analytics.Screens.PLS_COURSE_DATES,
            analyticsRegistry = environment.analyticsRegistry,
            courseBannerInfoModel = courseBannerInfo,
            clickListener = View.OnClickListener { viewModel.resetCourseDatesBanner(courseID = courseData.courseId) })

    }

    private fun initializedSyncContainer() {
        checkIfCalendarExists()
        binding.switchSync.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!CalendarUtils.permissions.any { permission ->
                        PermissionsUtil.checkPermissions(
                            permission,
                            contextOrThrow
                        )
                    }) {
                    askCalendarPermission()
                } else if (isCalendarExist.not()) {
                    askForCalendarSync()
                }
            } else if (CalendarUtils.hasPermissions(context = contextOrThrow)) {
                val calendarId = CalendarUtils.getCalendarId(
                    context = contextOrThrow,
                    accountName = accountName,
                    calendarTitle = calendarTitle
                )
                if (calendarId != -1L) {
                    askCalendarRemoveDialog(calendarId)
                }
            }
            trackCalendarEvent(
                if (isChecked) Analytics.Events.CALENDAR_TOGGLE_ON else Analytics.Events.CALENDAR_TOGGLE_OFF,
                if (isChecked) Analytics.Values.CALENDAR_TOGGLE_ON else Analytics.Values.CALENDAR_TOGGLE_OFF
            )
        }
    }

    private fun checkIfCalendarExists() {
        isCalendarExist = CalendarUtils.isCalendarExists(
            context = contextOrThrow,
            accountName = accountName,
            calendarTitle = calendarTitle
        )
        binding.switchSync.isChecked = isCalendarExist
    }

    private fun askCalendarPermission() {
        val title: String = ResourceUtil.getFormattedString(
            resources,
            R.string.title_request_calendar_permission,
            AppConstants.PLATFORM_NAME,
            environment.config.platformName
        ).toString()
        val message: String = ResourceUtil.getFormattedString(
            resources,
            R.string.message_request_calendar_permission,
            AppConstants.PLATFORM_NAME,
            environment.config.platformName
        ).toString()

        val alertDialog =
            AlertDialogFragment.newInstance(title, message, getString(R.string.label_ok),
                { _: DialogInterface, _: Int ->
                    PermissionsUtil.requestPermissions(
                        PermissionsUtil.CALENDAR_PERMISSION_REQUEST,
                        CalendarUtils.permissions,
                        this@CourseDatesPageFragment
                    )
                },
                getString(R.string.label_do_not_allow),
                { _: DialogInterface?, _: Int ->
                    trackCalendarEvent(
                        Analytics.Events.CALENDAR_ACCESS_DONT_ALLOW,
                        Analytics.Values.CALENDAR_ACCESS_DONT_ALLOW
                    )
                    binding.switchSync.isChecked = false
                })
        alertDialog.isCancelable = false
        alertDialog.show(childFragmentManager, null)
    }

    private fun askForCalendarSync() {
        val title: String = ResourceUtil.getFormattedString(
            resources,
            R.string.title_add_course_calendar,
            AppConstants.COURSE_NAME,
            calendarTitle
        ).toString()
        val message: String = ResourceUtil.getFormattedString(
            resources,
            R.string.message_add_course_calendar,
            keyValMap
        ).toString()

        val alertDialog =
            AlertDialogFragment.newInstance(title, message, getString(R.string.label_ok),
                { _: DialogInterface, _: Int ->
                    trackCalendarEvent(
                        Analytics.Events.CALENDAR_ADD_OK,
                        Analytics.Values.CALENDAR_ADD_OK
                    )
                    insertCalendarEvent()
                },
                getString(R.string.label_cancel),
                { _: DialogInterface?, _: Int ->
                    trackCalendarEvent(
                        Analytics.Events.CALENDAR_ADD_CANCEL,
                        Analytics.Values.CALENDAR_ADD_CANCEL
                    )
                    binding.switchSync.isChecked = false
                })
        alertDialog.isCancelable = false
        alertDialog.show(childFragmentManager, null)
    }

    private fun showShiftDateSnackBar(isSuccess: Boolean) {
        val snackbarErrorNotification = SnackbarErrorNotification(binding.root)
        snackbarErrorNotification.showError(
            if (isSuccess) R.string.assessment_shift_dates_success_msg else R.string.course_dates_reset_unsuccessful,
            0, 0, SnackbarErrorNotification.COURSE_DATE_MESSAGE_DURATION, null
        )
        environment.analyticsRegistry.trackPLSCourseDatesShift(
            courseData.courseId,
            courseData.mode,
            Analytics.Screens.PLS_COURSE_DATES,
            isSuccess
        )
    }

    private fun insertCalendarEvent() {
        val calendarId: Long = CalendarUtils.createOrUpdateCalendar(
            context = contextOrThrow,
            accountName = accountName,
            calendarTitle = calendarTitle
        )
        // if app unable to create the Calendar for the course
        if (calendarId == -1L) {
            Toast.makeText(
                contextOrThrow,
                getString(R.string.adding_calendar_error_message),
                Toast.LENGTH_SHORT
            ).show()
            binding.switchSync.isChecked = false
            return
        }
        viewModel.addOrUpdateEventsInCalendar(
            contextOrThrow,
            calendarId,
            courseData.courseId,
            courseData.course.name,
            isDeepLinkEnabled,
            false
        )
    }

    private fun dismissLoader() {
        loaderDialog.dismiss()
        if (viewModel.areEventsUpdated) {
            showCalendarUpdatedSnackbar()
            trackCalendarEvent(
                Analytics.Events.CALENDAR_UPDATE_SUCCESS,
                Analytics.Values.CALENDAR_UPDATE_SUCCESS
            )
        } else {
            calendarAddedSuccessDialog()
            trackCalendarEvent(
                Analytics.Events.CALENDAR_ADD_SUCCESS,
                Analytics.Values.CALENDAR_ADD_SUCCESS
            )
        }
    }

    private fun calendarAddedSuccessDialog() {
        isCalendarExist = true
        if (environment.courseCalendarPrefs.isSyncAlertPopupDisabled(
                courseData.course.name.replace(
                    " ",
                    "_"
                )
            )
        ) {
            showAddCalendarSuccessSnackbar()
        } else {
            environment.courseCalendarPrefs.setSyncAlertPopupDisabled(
                courseData.course.name.replace(
                    " ",
                    "_"
                ), true
            )
            val message: String = ResourceUtil.getFormattedString(
                resources,
                R.string.message_for_alert_after_course_calendar_added,
                AppConstants.COURSE_NAME,
                calendarTitle
            ).toString()

            AlertDialogFragment.newInstance(null, message, getString(R.string.label_done),
                { _: DialogInterface, _: Int ->
                    trackCalendarEvent(
                        Analytics.Events.CALENDAR_CONFIRMATION_DONE,
                        Analytics.Values.CALENDAR_CONFIRMATION_DONE
                    )
                },
                getString(R.string.label_view_events),
                { _: DialogInterface?, _: Int ->
                    trackCalendarEvent(
                        Analytics.Events.CALENDAR_VIEW_EVENTS,
                        Analytics.Values.CALENDAR_VIEW_EVENTS
                    )
                    CalendarUtils.openCalendarApp(this)
                }).show(childFragmentManager, null)
        }
    }


    private fun showAddCalendarSuccessSnackbar() {
        val snackbarErrorNotification = SnackbarErrorNotification(binding.root)
        snackbarErrorNotification.showError(
            R.string.message_after_course_calendar_added,
            0, R.string.label_close, SnackbarErrorNotification.COURSE_DATE_MESSAGE_DURATION
        ) { snackbarErrorNotification.hideError() }
    }

    private fun askCalendarRemoveDialog(calendarId: Long) {
        val title: String = ResourceUtil.getFormattedString(
            resources,
            R.string.title_remove_course_calendar,
            AppConstants.COURSE_NAME,
            calendarTitle
        ).toString()
        val message: String = ResourceUtil.getFormattedString(
            resources,
            R.string.message_remove_course_calendar,
            keyValMap
        ).toString()

        val alertDialog =
            AlertDialogFragment.newInstance(title, message, getString(R.string.label_remove),
                { _: DialogInterface, _: Int ->
                    trackCalendarEvent(
                        Analytics.Events.CALENDAR_REMOVE_OK,
                        Analytics.Values.CALENDAR_REMOVE_OK
                    )
                    deleteCalendar(calendarId)
                },
                getString(R.string.label_cancel),
                { _: DialogInterface?, _: Int ->
                    trackCalendarEvent(
                        Analytics.Events.CALENDAR_REMOVE_CANCEL,
                        Analytics.Values.CALENDAR_REMOVE_CANCEL
                    )
                    binding.switchSync.isChecked = true
                })
        alertDialog.isCancelable = false
        alertDialog.show(childFragmentManager, null)
    }

    private fun deleteCalendar(calendarId: Long) {
        CalendarUtils.deleteCalendar(context = contextOrThrow, calendarId = calendarId)
        isCalendarExist = false
        showCalendarRemovedSnackbar()
        trackCalendarEvent(
            Analytics.Events.CALENDAR_REMOVE_SUCCESS,
            Analytics.Values.CALENDAR_REMOVE_SUCCESS
        )
    }

    private fun showOpenInBrowserDialog(link: String) {
        AlertDialogFragment.newInstance(
            null,
            getString(R.string.assessment_not_available),
            getString(R.string.assessment_view_on_web),
            { _: DialogInterface, _: Int -> BrowserUtil.open(activity, link, true) },
            getString(R.string.label_cancel),
            null
        ).show(childFragmentManager, null)
    }

    override fun onPermissionGranted(permissions: Array<out String>?, requestCode: Int) {
        askForCalendarSync()
        trackCalendarEvent(Analytics.Events.CALENDAR_ACCESS_OK, Analytics.Values.CALENDAR_ACCESS_OK)
    }

    override fun onPermissionDenied(permissions: Array<out String>?, requestCode: Int) {
        binding.switchSync.isChecked = false
        trackCalendarEvent(
            Analytics.Events.CALENDAR_ACCESS_DONT_ALLOW,
            Analytics.Values.CALENDAR_ACCESS_DONT_ALLOW
        )
    }

    private fun trackCalendarEvent(eventName: String, biValue: String) {
        environment.analyticsRegistry.trackCalendarEvent(
            eventName,
            biValue,
            courseData.courseId,
            courseData.mode,
            isSelfPaced,
            viewModel.getSyncingCalendarTime()
        )
        viewModel.resetSyncingCalendarTime()
    }
}
