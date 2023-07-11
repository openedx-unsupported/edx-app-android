package org.edx.mobile.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.databinding.LayoutCourseDatesBannerBinding;
import org.edx.mobile.deeplink.DeepLink;
import org.edx.mobile.deeplink.Screen;
import org.edx.mobile.deeplink.ScreenDef;
import org.edx.mobile.event.CourseDashboardRefreshEvent;
import org.edx.mobile.event.CourseOutlineRefreshEvent;
import org.edx.mobile.event.CourseUpgradedEvent;
import org.edx.mobile.event.IAPFlowEvent;
import org.edx.mobile.event.LogoutEvent;
import org.edx.mobile.event.MediaStatusChangeEvent;
import org.edx.mobile.event.MyCoursesRefreshEvent;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.exception.CourseContentNotValidException;
import org.edx.mobile.exception.ErrorMessage;
import org.edx.mobile.extenstion.ViewExtKt;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.http.notifications.SnackbarErrorNotification;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.CourseComponentStatusResponse;
import org.edx.mobile.model.api.CourseUpgradeResponse;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.BlockPath;
import org.edx.mobile.model.course.CourseBannerInfoModel;
import org.edx.mobile.model.course.CourseBannerType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.EnrollmentMode;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.iap.IAPFlowData;
import org.edx.mobile.model.video.VideoQuality;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.storage.DownloadCompletedEvent;
import org.edx.mobile.module.storage.DownloadedVideoDeletedEvent;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.services.EdxCookieManager;
import org.edx.mobile.services.VideoDownloadHelper;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.CalendarUtils;
import org.edx.mobile.util.ConfigUtil;
import org.edx.mobile.util.CourseDateUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.UiUtils;
import org.edx.mobile.util.observer.EventObserver;
import org.edx.mobile.view.adapters.CourseOutlineAdapter;
import org.edx.mobile.view.dialog.AlertDialogFragment;
import org.edx.mobile.view.dialog.FullscreenLoaderDialogFragment;
import org.edx.mobile.view.dialog.VideoDownloadQualityDialogFragment;
import org.edx.mobile.viewModel.CourseDateViewModel;
import org.edx.mobile.viewModel.CourseViewModel;
import org.edx.mobile.viewModel.CourseViewModel.CoursesRequestType;
import org.edx.mobile.viewModel.InAppPurchasesViewModel;
import org.edx.mobile.wrapper.InAppPurchasesDialog;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class CourseOutlineFragment extends OfflineSupportBaseFragment implements RefreshListener,
        VideoDownloadHelper.DownloadManagerCallback {
    private final Logger logger = new Logger(getClass().getName());
    private static final int AUTOSCROLL_DELAY_MS = 500;
    private static final int SNACKBAR_SHOWTIME_MS = 5000;

    private CourseOutlineAdapter adapter;
    private ListView listView;
    private LayoutCourseDatesBannerBinding bannerViewBinding;
    private EnrolledCoursesResponse courseData;
    private String courseComponentId;
    private boolean isVideoMode;
    // Flag to check whether the course dates banner info api call can be made or not
    private boolean canFetchBannerInfo = true;
    // Flag to check if the course dates banner is visible or not
    private boolean isBannerVisible = false;
    private boolean isOnCourseOutline;
    // Flag to differentiate between single or multiple video download
    private boolean isSingleVideoDownload;
    private ActionMode deleteMode;
    private DownloadEntry downloadEntry;
    private List<? extends HasDownloadEntry> downloadEntries;
    private SwipeRefreshLayout swipeContainer;

    private FullScreenErrorNotification errorNotification;

    @Inject
    CourseAPI courseApi;

    @Inject
    VideoDownloadHelper downloadManager;

    @Inject
    InAppPurchasesDialog iapDialogs;

    private CourseViewModel courseViewModel;
    private CourseDateViewModel courseDateViewModel;
    private InAppPurchasesViewModel iapViewModel;

    private View loadingIndicator;
    private FrameLayout flBulkDownload;
    private View videoQualityLayout;
    private TextView tvVideoDownloadQuality;
    private CourseOutlineAdapter.DownloadListener downloadListener;
    private Call<CourseUpgradeResponse> getCourseUpgradeStatus;
    private CourseUpgradeResponse courseUpgradeData;
    private String calendarTitle = "";
    private String accountName = "";
    private String screenName;

    private AlertDialogFragment loaderDialog;
    private boolean refreshOnResume = false;

    private final ActivityResultLauncher<String> storagePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    onPermissionGranted();
                } else {
                    showPermissionDeniedMessage();
                    onPermissionDenied();
                }
            });

    public static Bundle makeArguments(@NonNull EnrolledCoursesResponse model,
                                       @Nullable String courseComponentId, boolean isVideosMode, @ScreenDef String screenName) {
        final Bundle arguments = new Bundle();
        final Bundle courseBundle = new Bundle();
        courseBundle.putSerializable(Router.EXTRA_COURSE_DATA, model);
        courseBundle.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId);
        courseBundle.putString(Router.EXTRA_SCREEN_NAME, screenName);

        arguments.putBundle(Router.EXTRA_BUNDLE, courseBundle);
        arguments.putBoolean(Router.EXTRA_IS_VIDEOS_MODE, isVideosMode);

        return arguments;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Bundle bundle;
        {
            if (savedInstanceState != null) {
                bundle = savedInstanceState;
            } else {
                bundle = getArguments();
            }
        }

        final View view = inflater.inflate(R.layout.fragment_course_outline, container, false);
        listView = (ListView) view.findViewById(R.id.outline_list);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        errorNotification = new FullScreenErrorNotification(swipeContainer);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        flBulkDownload = view.findViewById(R.id.fl_bulk_download_container);
        videoQualityLayout = view.findViewById(R.id.video_quality_layout);
        tvVideoDownloadQuality = view.findViewById(R.id.tv_video_download_quality);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Hide the progress bar as swipe layout has its own progress indicator
                loadingIndicator.setVisibility(View.GONE);
                errorNotification.hideError();
                canFetchBannerInfo = true;
                courseViewModel.getCourseData(courseData.getCourseId(), null, false, true,
                        CoursesRequestType.LIVE.INSTANCE);
            }
        });
        UiUtils.INSTANCE.setSwipeRefreshLayoutColors(swipeContainer);
        restore(bundle);
        calendarTitle = CalendarUtils.getCourseCalendarTitle(environment, courseData.getCourse().getName());
        accountName = CalendarUtils.getUserAccountForSync(environment);
        loaderDialog = AlertDialogFragment.newInstance(R.string.title_syncing_calendar, R.layout.alert_dialog_progress);
        initListView(view);

        if (isOnCourseOutline) {
            initCourseDateObserver();
            initInAppPurchaseSetup();
        }
        initCourseObservers();
        fetchCourseComponent();
        // Track CourseOutline for A/A test
        trackAATestCourseOutline();
        getCourseUpgradeFirebaseConfig();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateRowSelection(getArguments().getString(Router.EXTRA_LAST_ACCESSED_ID));
    }

    private void initCourseDateObserver() {
        courseDateViewModel = new ViewModelProvider(this).get(CourseDateViewModel.class);

        courseDateViewModel.getSyncLoader().observe(getViewLifecycleOwner(), new EventObserver<>(showLoader -> {
            if (showLoader) {
                loaderDialog.setCancelable(false);
                loaderDialog.showNow(getChildFragmentManager(), null);
            } else {
                loaderDialog.dismiss();
                showCalendarUpdatedSnackbar();
                trackCalendarEvent(Analytics.Events.CALENDAR_UPDATE_SUCCESS, Analytics.Values.CALENDAR_UPDATE_SUCCESS);
            }
            return null;
        }));

        courseDateViewModel.getCourseDates().observe(getViewLifecycleOwner(), new EventObserver<>(courseDates -> {
            if (courseDates.getCourseDateBlocks() != null) {
                courseDates.organiseCourseDates();
                long outdatedCalenderId = CalendarUtils.isCalendarOutOfDate(
                        requireContext(), accountName, calendarTitle, courseDates.getCourseDateBlocks());
                if (outdatedCalenderId != -1L) {
                    showCalendarOutOfDateDialog(outdatedCalenderId);
                }
            }
            return null;
        }));

        courseDateViewModel.getBannerInfo().observe(getViewLifecycleOwner(), this::initDatesBanner);

        courseDateViewModel.getShowLoader().observe(getViewLifecycleOwner(), flag ->
                loadingIndicator.setVisibility(flag ? View.VISIBLE : View.GONE));

        courseDateViewModel.getSwipeRefresh().observe(getViewLifecycleOwner(), canRefresh ->
                swipeContainer.setRefreshing(canRefresh));

        courseDateViewModel.getResetCourseDates().observe(getViewLifecycleOwner(), resetCourseDates -> {
            if (resetCourseDates != null) {
                if (!CalendarUtils.INSTANCE.isCalendarExists(getContextOrThrow(), accountName, calendarTitle)) {
                    showShiftDateSnackBar(true);
                }
            }
        });

        courseDateViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                if (errorMessage.getThrowable() instanceof HttpStatusException &&
                        ((HttpStatusException) errorMessage.getThrowable()).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    environment.getRouter().forceLogout(getContextOrThrow(),
                            environment.getAnalyticsRegistry(),
                            environment.getNotificationDelegate());
                } else {
                    switch (errorMessage.getRequestType()) {
                        case ErrorMessage.BANNER_INFO_CODE:
                            initDatesBanner(null);
                            break;
                        case ErrorMessage.COURSE_RESET_DATES_CODE:
                            showShiftDateSnackBar(false);
                            break;
                    }
                }
            }
        });
    }

    private void initInAppPurchaseSetup() {
        if (courseData.isAuditMode() && !isVideoMode) {
            initInAppPurchaseObserver();
        }
    }

    private void showFullscreenLoader(@NonNull IAPFlowData iapFlowData) {
        // To proceed with the same instance of dialog fragment in case of orientation change
        FullscreenLoaderDialogFragment fullscreenLoader = FullscreenLoaderDialogFragment
                .getRetainedInstance(getChildFragmentManager());
        if (fullscreenLoader == null) {
            fullscreenLoader = FullscreenLoaderDialogFragment.newInstance(iapFlowData);
        }
        fullscreenLoader.show(getChildFragmentManager(), FullscreenLoaderDialogFragment.TAG);
    }

    private void initInAppPurchaseObserver() {
        iapViewModel = new ViewModelProvider(this).get(InAppPurchasesViewModel.class);

        iapViewModel.getErrorMessage().observe(getViewLifecycleOwner(), new EventObserver<>(errorMessage -> {
            if (errorMessage.getRequestType() == ErrorMessage.COURSE_REFRESH_CODE) {
                iapDialogs.handleIAPException(
                        CourseOutlineFragment.this,
                        errorMessage,
                        (dialogInterface, i) -> courseViewModel
                                .getCourseData(courseData.getCourseId(), null, false, false,
                                        CoursesRequestType.LIVE.INSTANCE),
                        (dialogInterface, i) -> {
                            iapViewModel.getIapFlowData().clear();
                            FullscreenLoaderDialogFragment fullScreenLoader = FullscreenLoaderDialogFragment.getRetainedInstance(getChildFragmentManager());
                            if (fullScreenLoader != null) {
                                fullScreenLoader.dismiss();
                            }
                        }
                );
            }
            return null;
        }));
    }

    private void showCalendarOutOfDateDialog(Long calendarId) {
        AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(getString(R.string.title_calendar_out_of_date),
                getString(R.string.message_calendar_out_of_date),
                getString(R.string.label_update_now), (dialogInterface, which) -> updateCalendarEvents(),
                getString(R.string.label_remove_course_calendar), (dialogInterface, which) -> removeCalendar(calendarId));
        alertDialogFragment.setCancelable(false);
        alertDialogFragment.show(getChildFragmentManager(), null);
    }

    private void updateCalendarEvents() {
        trackCalendarEvent(Analytics.Events.CALENDAR_SYNC_UPDATE, Analytics.Values.CALENDAR_SYNC_UPDATE);
        long newCalId = CalendarUtils.createOrUpdateCalendar(getContextOrThrow(), accountName, CalendarContract.ACCOUNT_TYPE_LOCAL, calendarTitle);
        ConfigUtil.Companion.checkCalendarSyncEnabled(environment.getConfig(), response ->
                courseDateViewModel.addOrUpdateEventsInCalendar(getContextOrThrow(),
                        newCalId, courseData.getCourseId(), courseData.getCourse().getName(), response.isDeepLinkEnabled(), true));
    }

    private void removeCalendar(Long calendarId) {
        trackCalendarEvent(Analytics.Events.CALENDAR_SYNC_REMOVE, Analytics.Values.CALENDAR_SYNC_REMOVE);
        CalendarUtils.INSTANCE.deleteCalendar(getContextOrThrow(), calendarId);
        showCalendarRemovedSnackbar();
        trackCalendarEvent(Analytics.Events.CALENDAR_REMOVE_SUCCESS, Analytics.Values.CALENDAR_REMOVE_SUCCESS);
    }

    private void trackCalendarEvent(String eventName, String biValue) {
        environment.getAnalyticsRegistry().trackCalendarEvent(eventName, biValue, courseData.getCourseId(),
                courseData.getMode(), courseData.getCourse().isSelfPaced(), courseDateViewModel.getSyncingCalendarTime());
        courseDateViewModel.resetSyncingCalendarTime();
    }

    private void restore(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            final Bundle bundle = savedInstanceState.getBundle(Router.EXTRA_BUNDLE);
            courseData = (EnrolledCoursesResponse) bundle.getSerializable(Router.EXTRA_COURSE_DATA);
            courseUpgradeData = bundle.getParcelable(Router.EXTRA_COURSE_UPGRADE_DATA);
            courseComponentId = bundle.getString(Router.EXTRA_COURSE_COMPONENT_ID);
            screenName = bundle.getString(DeepLink.Keys.SCREEN_NAME);
            isVideoMode = savedInstanceState.getBoolean(Router.EXTRA_IS_VIDEOS_MODE);
            isSingleVideoDownload = savedInstanceState.getBoolean("isSingleVideoDownload");
            if (savedInstanceState.containsKey(Router.EXTRA_IS_ON_COURSE_OUTLINE)) {
                isOnCourseOutline = savedInstanceState.getBoolean(Router.EXTRA_IS_ON_COURSE_OUTLINE);
            } else {
                isOnCourseOutline = isOnCourseOutline();
            }
        }
    }

    private void fetchCourseComponent() {
        // Prepare the loader. Either re-connect with an existing one or start a new one.
        if (environment.getLoginPrefs().isUserLoggedIn()) {
            final String courseId = courseData.getCourseId();
            courseViewModel.getCourseData(courseId, courseComponentId, true, false,
                    CoursesRequestType.APP_LEVEL_CACHE.INSTANCE);
        } else {
            EventBus.getDefault().post(new LogoutEvent());
        }
    }

    private void initCourseObservers() {
        courseViewModel = new ViewModelProvider(this).get(CourseViewModel.class);
        courseViewModel.getCourseComponent().observe(getViewLifecycleOwner(), new EventObserver<>(courseComponent -> {
            if (!isAdded()) {
                return null;
            }
            loadData(validateCourseComponent(courseComponent));
            FullscreenLoaderDialogFragment fullscreenLoader = FullscreenLoaderDialogFragment
                    .getRetainedInstance(getChildFragmentManager());
            if (fullscreenLoader != null && fullscreenLoader.isResumed()) {
                new SnackbarErrorNotification(listView).showUpgradeSuccessSnackbar(R.string.purchase_success_message);
                fullscreenLoader.closeLoader();
            }
            return null;
        }));

        courseViewModel.getShowProgress().observe(getViewLifecycleOwner(), showProgress -> {
            ViewExtKt.setVisibility(loadingIndicator, showProgress);
        });

        courseViewModel.getSwipeRefresh().observe(getViewLifecycleOwner(), swipeRefresh -> {
            swipeContainer.setRefreshing(swipeRefresh);
        });

        courseViewModel.getHandleError().observe(getViewLifecycleOwner(), throwable -> {
            if (isAdded()) {
                if (throwable instanceof HttpStatusException &&
                        ((HttpStatusException) throwable).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    environment.getRouter().forceLogout(getContextOrThrow(),
                            environment.getAnalyticsRegistry(),
                            environment.getNotificationDelegate());
                } else {
                    FullscreenLoaderDialogFragment fullscreenLoader = FullscreenLoaderDialogFragment
                            .getRetainedInstance(getChildFragmentManager());
                    if (throwable instanceof CourseContentNotValidException) {
                        errorNotification.showError(requireContext(), throwable);
                        logger.error(throwable, true);
                    } else if (fullscreenLoader != null && fullscreenLoader.isResumed()) {
                        iapViewModel.dispatchError(ErrorMessage.COURSE_REFRESH_CODE, null, throwable);
                    } else {
                        errorNotification.showError(requireContext(), throwable, R.string.lbl_reload, v -> {
                            if (NetworkUtil.isConnected(requireContext())) {
                                onRefresh();
                            }
                        });
                    }
                    // Remove bulk video download if the course has NO downloadable videos
                    UiUtils.INSTANCE.removeFragmentByTag(CourseOutlineFragment.this, "bulk_download");
                    if (!EventBus.getDefault().isRegistered(this)) {
                        EventBus.getDefault().register(this);
                    }
                }
            }
        });
    }

    private void trackAATestCourseOutline() {
        if (!isVideoMode &&
                environment.getConfig().getFirebaseConfig().isEnabled()) {
            final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
                final String group = firebaseRemoteConfig.getString(Analytics.Keys.AA_EXPERIMENT);
                if (!TextUtils.isEmpty(group) && environment.getLoginPrefs().isUserLoggedIn()) {
                    final Map<String, String> values = new HashMap<>();
                    values.put(Analytics.Keys.EXPERIMENT, Analytics.Keys.AA_EXPERIMENT);
                    values.put(Analytics.Keys.GROUP, group);
                    values.put(Analytics.Keys.USER_ID, Long.toString(environment.getLoginPrefs().getUserId()));
                    values.put(Analytics.Keys.COURSE_ID, courseData.getCourseId());
                    environment.getAnalyticsRegistry().trackExperimentParams(Analytics.Events.MOBILE_EXPERIMENT_EVALUATED, values);
                }
            });
        }
    }

    /**
     * Validates the course component that we should load on screen i.e. based on
     * {@link #isOnCourseOutline} validates that the CourseComponent we are about to load on screen
     * has the same ID as {@link #courseComponentId}.
     *
     * @param courseComponent The course component to validate.
     * @return Validated course component having the same ID as {@link #courseComponentId}.
     */
    @NonNull
    private CourseComponent validateCourseComponent(@NonNull CourseComponent courseComponent) {
        if (!isOnCourseOutline) {
            final CourseComponent cached = courseManager.getComponentByIdFromAppLevelCache(
                    courseData.getCourseId(), courseComponentId);
            courseComponent = cached != null ? cached : courseComponent;
        }
        return courseComponent;
    }

    private void initListView(@NonNull View view) {
        initAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Checking if the onItemClick action performed on course dates banner
                if (!isVideoMode && isBannerVisible) {
                    if (position == 0)
                        return;
                    else
                        position -= 1;
                }
                if (deleteMode != null) {
                    deleteMode.finish();
                }
                listView.clearChoices();
                final CourseComponent component = adapter.getItem(position).component;
                if (component.isContainer()) {
                    environment.getRouter().showCourseContainerOutline(CourseOutlineFragment.this,
                            REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, courseUpgradeData, component.getId(), null, isVideoMode);
                } else {
                    if (adapter.getItemViewType(position) == CourseOutlineAdapter.SectionRow.RESUME_COURSE_ITEM) {
                        environment.getAnalyticsRegistry().trackResumeCourseBannerTapped(component.getCourseId(), component.getId());
                    }
                    environment.getRouter().showCourseUnitDetail(CourseOutlineFragment.this,
                            REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, courseUpgradeData, component.getId(), isVideoMode);

                    environment.getAnalyticsRegistry().trackScreenView(
                            Analytics.Screens.UNIT_DETAIL, courseData.getCourse().getId(), component.getParent().getInternalName());
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Checking if the onItemLongClick action performed on course dates banner
                if (!isVideoMode && isBannerVisible && position == 0) {
                    return false;
                }
                final AppCompatImageView bulkDownloadIcon = (AppCompatImageView) view.findViewById(R.id.bulk_download);
                if (bulkDownloadIcon != null && bulkDownloadIcon.getTag() != null &&
                        (Integer) bulkDownloadIcon.getTag() == R.drawable.ic_download_done) {
                    ((AppCompatActivity) getActivity()).startSupportActionMode(deleteModelCallback);
                    listView.setItemChecked(position, true);
                    return true;
                }
                return false;
            }
        });
    }

    private void initAdapter() {
        if (adapter == null) {
            // creating adapter just once
            downloadListener = new CourseOutlineAdapter.DownloadListener() {
                @Override
                public void download(List<? extends HasDownloadEntry> models) {
                    downloadEntries = models;
                    isSingleVideoDownload = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        onPermissionGranted();
                    } else {
                        storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }

                @Override
                public void download(DownloadEntry videoData) {
                    downloadEntry = videoData;
                    isSingleVideoDownload = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        onPermissionGranted();
                    } else {
                        storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }

                @Override
                public void viewDownloadsStatus() {
                    environment.getRouter().showDownloads(getActivity());
                }
            };
            adapter = new CourseOutlineAdapter(getActivity(), courseData, environment, downloadListener,
                    isVideoMode, isOnCourseOutline);
        }
    }

    /**
     * Initialized dates info banner on CourseOutlineFragment
     *
     * @param courseBannerInfo object of course deadline info
     */
    private void initDatesBanner(CourseBannerInfoModel courseBannerInfo) {
        if (bannerViewBinding == null)
            bannerViewBinding = LayoutCourseDatesBannerBinding.inflate(getLayoutInflater(), listView, false);

        if (courseBannerInfo != null && !isVideoMode && isOnCourseOutline && !courseBannerInfo.getHasEnded() &&
                courseBannerInfo.getDatesBannerInfo().getCourseBannerType() == CourseBannerType.RESET_DATES) {

            CourseDateUtil.INSTANCE.setupCourseDatesBanner(bannerViewBinding.getRoot(),
                    courseData.getCourse().getId(), courseData.getMode(), courseData.getCourse().isSelfPaced(),
                    Analytics.Screens.PLS_COURSE_DASHBOARD, environment.getAnalyticsRegistry(), courseBannerInfo,
                    v -> courseDateViewModel.resetCourseDatesBanner(courseData.getCourseId()));

            if (listView.getHeaderViewsCount() == 0 && ViewExtKt.isVisible(bannerViewBinding.getRoot())) {
                listView.addHeaderView(bannerViewBinding.getRoot());
                isBannerVisible = true;
            }
        } else {
            listView.removeHeaderView(bannerViewBinding.getRoot());
            isBannerVisible = false;
        }
    }

    private void detectDeepLinking() {
        if (Screen.COURSE_COMPONENT.equalsIgnoreCase(screenName)
                && !TextUtils.isEmpty(courseComponentId)) {
            environment.getRouter().showCourseUnitDetail(CourseOutlineFragment.this,
                    REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, courseUpgradeData, courseComponentId, false);
            screenName = null;
        }
    }

    private void showShiftDateSnackBar(boolean isSuccess) {
        if (!isAdded()) {
            return;
        }
        SnackbarErrorNotification snackbarErrorNotification = new SnackbarErrorNotification(listView);
        if (isSuccess) {
            snackbarErrorNotification.showError(R.string.assessment_shift_dates_success_msg,
                    0, R.string.assessment_view_all_dates, SnackbarErrorNotification.COURSE_DATE_MESSAGE_DURATION,
                    v -> environment.getRouter().showCourseDashboardTabs(getActivity(), courseData.getCourseId(), Screen.COURSE_DATES));
        } else {
            snackbarErrorNotification.showError(R.string.course_dates_reset_unsuccessful, 0,
                    0, SnackbarErrorNotification.COURSE_DATE_MESSAGE_DURATION, null);
        }
        environment.getAnalyticsRegistry().trackPLSCourseDatesShift(courseData.getCourseId(),
                courseData.getMode(), Analytics.Screens.PLS_COURSE_DASHBOARD, isSuccess);
    }

    public void onPermissionGranted() {
        if (isSingleVideoDownload) {
            downloadManager.downloadVideo(downloadEntry, getActivity(), CourseOutlineFragment.this);
        } else {
            downloadManager.downloadVideos(downloadEntries, getActivity(), CourseOutlineFragment.this);
        }
    }

    public void onPermissionDenied() {
        if (isSingleVideoDownload) {
            downloadEntry = null;
        } else {
            if (downloadEntries != null) {
                downloadEntries.clear();
                downloadEntries = null;
            }
        }
    }

    /**
     * Callback to handle the deletion of videos using the Contextual Action Bar.
     */
    private ActionMode.Callback deleteModelCallback = new ActionMode.Callback() {
        // Called when the action mode is created; startActionMode/startSupportActionMode was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            final MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.delete_contextual_menu, menu);
            menu.findItem(R.id.item_delete).setIcon(
                    UiUtils.INSTANCE.getDrawable(requireContext(), R.drawable.ic_delete, R.dimen.action_bar_icon_size)
            );
            mode.setTitle(R.string.delete_videos_title);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            deleteMode = mode;
            return false;
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.item_delete:
                    final int checkedItemPosition = listView.getCheckedItemPosition();
                    // Change the icon to download icon immediately
                    final View rowView = listView.getChildAt(checkedItemPosition - listView.getFirstVisiblePosition());
                    if (rowView != null) {
                        // rowView will be null, if the user scrolls away from the checked item
                        final AppCompatImageView bulkDownloadIcon = (AppCompatImageView) rowView.findViewById(R.id.bulk_download);
                        bulkDownloadIcon.setImageDrawable(UiUtils.INSTANCE.getDrawable(requireContext(), R.drawable.ic_download));
                        bulkDownloadIcon.setTag(R.drawable.ic_download);
                    }

                    final CourseOutlineAdapter.SectionRow rowItem = adapter.getItem(checkedItemPosition);
                    final List<CourseComponent> videos = rowItem.component.getVideos(true);
                    final int totalVideos = videos.size();

                    if (isOnCourseOutline) {
                        environment.getAnalyticsRegistry().trackSubsectionVideosDelete(
                                courseData.getCourseId(), rowItem.component.getId());
                    } else {
                        environment.getAnalyticsRegistry().trackUnitVideoDelete(
                                courseData.getCourseId(), rowItem.component.getId());
                    }

                    /*
                    The android docs have NOT been updated yet, but if you jump into the source code
                    you'll notice that the parameter to the method setDuration(int duration) can
                    either be one of LENGTH_SHORT, LENGTH_LONG, LENGTH_INDEFINITE or a custom
                    duration in milliseconds.
                    https://stackoverflow.com/a/30552666
                    https://github.com/material-components/material-components-android/commit/2cb77c9331cc3c6a5034aace0238b96508acf47d
                     */
                    @SuppressLint("WrongConstant") final Snackbar snackbar = Snackbar.make(listView,
                            getResources().getQuantityString(R.plurals.delete_video_snackbar_msg, totalVideos, totalVideos),
                            SNACKBAR_SHOWTIME_MS);
                    snackbar.setAction(R.string.label_undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // No need of implementation as we'll handle the action in SnackBar's
                            // onDismissed callback.
                        }
                    });
                    snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            // SnackBar is being dismissed by any action other than its action button's press
                            if (event != DISMISS_EVENT_ACTION) {
                                final IStorage storage = environment.getStorage();
                                for (CourseComponent video : videos) {
                                    final VideoBlockModel videoBlockModel = (VideoBlockModel) video;
                                    final DownloadEntry downloadEntry = videoBlockModel.getDownloadEntry(storage);
                                    if (downloadEntry.isDownloaded()) {
                                        // This check is necessary because, this callback gets
                                        // called multiple times when SnackBar is about to dismiss
                                        // and the activity finishes
                                        storage.removeDownload(downloadEntry);
                                    } else {
                                        return;
                                    }
                                }
                            } else {
                                if (isOnCourseOutline) {
                                    environment.getAnalyticsRegistry().trackUndoingSubsectionVideosDelete(
                                            courseData.getCourseId(), rowItem.component.getId());
                                } else {
                                    environment.getAnalyticsRegistry().trackUndoingUnitVideoDelete(
                                            courseData.getCourseId(), rowItem.component.getId());
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
                    snackbar.show();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            deleteMode = null;
            listView.clearChoices();
            listView.requestLayout();
        }
    };

    /**
     * Load data to the adapter
     *
     * @param courseComponent Components of course to be load
     */
    private void loadData(@NonNull CourseComponent courseComponent) {
        if (courseData == null || getActivity() == null)
            return;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (!isOnCourseOutline) {
            // We only need to set the title of Course Outline screen, where we show a subsection's units
            getActivity().setTitle(courseComponent.getDisplayName());
        }
        if (!isVideoMode && isOnCourseOutline && canFetchBannerInfo) {
            courseDateViewModel.fetchCourseDates(courseData.getCourseId(), true, !swipeContainer.isRefreshing(), swipeContainer.isRefreshing());
            canFetchBannerInfo = false;
        }

        adapter.setData(courseComponent);
        if (adapter.hasCourseData()) {
            setUpBulkDownloadHeader(courseComponent);
            setUpVideoQualityHeader(courseComponent);
            errorNotification.hideError();
        } else {
            // Remove bulk video download if the course has NO downloadable videos
            UiUtils.INSTANCE.removeFragmentByTag(CourseOutlineFragment.this, "bulk_download");
            if (isVideoMode) {
                errorNotification.showError(R.string.no_videos_text, 0, -1, null);
            } else {
                boolean isSpecialExamInfo = courseComponent.getSpecialExamInfo() != null;
                Map<String, String> values = new HashMap<>();
                values.put(Analytics.Keys.SUBSECTION_ID, courseComponent.getBlockId());
                environment.getAnalyticsRegistry().trackScreenView(isSpecialExamInfo ? Analytics.Screens.SPECIAL_EXAM_BLOCK : Analytics.Screens.EMPTY_SUBSECTION_OUTLINE,
                        courseComponent.getCourseId(), null, values);

                errorNotification.showError(R.string.assessment_not_available, 0, R.string.assessment_view_on_web, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        environment.getAnalyticsRegistry().trackSubsectionViewOnWebTapped(courseComponent.getCourseId(), courseComponent.getBlockId(), isSpecialExamInfo);
                        BrowserUtil.open(getActivity(), courseComponent.getWebUrl(), false);
                    }
                });
            }
        }

        if (!isOnCourseOutline) {
            environment.getAnalyticsRegistry().trackScreenView(
                    Analytics.Screens.SECTION_OUTLINE, courseData.getCourseId(), courseComponent.getInternalName());
        }
        fetchLastAccessed();
        detectDeepLinking();
        courseComponentId = courseComponent.getId();
    }

    private void setUpVideoQualityHeader(CourseComponent courseComponent) {
        if (isVideoMode && isOnCourseOutline && courseComponent.getDownloadableVideosCount() > 0) {
            videoQualityLayout.setVisibility(View.VISIBLE);
            videoQualityLayout.setOnClickListener(v -> {
                environment.getAnalyticsRegistry().trackEvent(Analytics.Events.COURSE_VIDEOS_VIDEO_DOWNLOAD_QUALITY_CLICKED,
                        Analytics.Values.COURSE_VIDEOS_VIDEO_DOWNLOAD_QUALITY_CLICKED);
                showVideoQualitySelectionModal(courseComponent);
            });
            setVideoQualityHeaderLabel(environment.getUserPrefs().getVideoQuality());
        }
    }

    private void getCourseUpgradeFirebaseConfig() {
        if (!isOnCourseOutline || isVideoMode || getCourseUpgradeStatus != null) {
            return;
        }
        ConfigUtil.Companion.checkCourseUpgradeEnabled(environment.getConfig(), enabled -> {
            if (enabled) {
                fetchCourseUpgradeStatus();
            } else {
                courseUpgradeData = null;
                updatePaymentsBannerVisibility(View.GONE);
            }
        });
    }

    private void fetchCourseUpgradeStatus() {
        if (getCourseUpgradeStatus == null) {
            getCourseUpgradeStatus = courseApi.getCourseUpgradeStatus(courseData.getCourseId());
            getCourseUpgradeStatus.enqueue(new Callback<CourseUpgradeResponse>() {
                @Override
                public void onResponse(Call<CourseUpgradeResponse> call, Response<CourseUpgradeResponse> response) {
                    // Setting the call to null ensures that only 1 request is enqueued at a specific point in time
                    getCourseUpgradeStatus = null;
                    if (getActivity() != null) {
                        // Set the revenue cookie
                        EdxCookieManager.getSharedInstance(getActivity()).setMobileCookie();
                        CourseUpgradeResponse courseUpgrade = response.body();
                        if (courseUpgrade != null && courseUpgrade.getShowUpsell()
                                && !TextUtils.isEmpty(courseUpgrade.getBasketUrl())) {
                            courseUpgradeData = courseUpgrade;
                            updatePaymentsBannerVisibility(View.VISIBLE);
                            PaymentsBannerFragment.Companion.loadPaymentsBannerFragment(
                                    R.id.fragment_container, courseData, null,
                                    courseUpgradeData, true, getChildFragmentManager(), true);
                        } else {
                            updatePaymentsBannerVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onFailure(Call<CourseUpgradeResponse> call, Throwable t) {
                    // Setting the call to null ensures that only 1 request is enqueued at a specific point in time
                    getCourseUpgradeStatus = null;
                    updatePaymentsBannerVisibility(View.GONE);
                }
            });
        }
    }

    private void updatePaymentsBannerVisibility(int visibility) {
        final View view = getView();
        if (view != null) {
            view.findViewById(R.id.fragment_container).setVisibility(visibility);
        }
    }

    private void setUpBulkDownloadHeader(CourseComponent courseComponent) {
        if (isVideoMode) {
            if (courseComponent.getDownloadableVideosCount() == 0) {
                // Remove bulk video download if the course has NO downloadable videos
                UiUtils.INSTANCE.removeFragmentByTag(CourseOutlineFragment.this, "bulk_download");
            } else if (getActivity() != null) {
                Fragment fragment = getChildFragmentManager().findFragmentByTag("bulk_download");
                if (fragment == null) {
                    // Add bulk video download item
                    fragment = new BulkDownloadFragment();
                    // Using commitAllowingStateLoss() method here because there is
                    // chance transaction could have happened even the fragments state
                    // is saved.
                    getChildFragmentManager().
                            beginTransaction().replace(flBulkDownload.getId(), fragment, "bulk_download").
                            commitAllowingStateLoss();
                }
                ((BulkDownloadFragment) fragment).populateViewHolder(downloadListener,
                        isOnCourseOutline ? courseComponent.getCourseId() : courseComponent.getId(),
                        courseComponent.getVideos(true));
            }
        }
    }

    private void setVideoQualityHeaderLabel(VideoQuality videoQuality) {
        tvVideoDownloadQuality.setText(videoQuality.getTitleResId());
    }

    private void showVideoQualitySelectionModal(CourseComponent courseComponent) {
        VideoDownloadQualityDialogFragment videoQualityDialog =
                VideoDownloadQualityDialogFragment.getInstance(environment, videoQuality -> {
                    setVideoQualityHeaderLabel(videoQuality);
                    adapter.notifyDataSetChanged();
                    setUpBulkDownloadHeader(courseComponent);
                });
        videoQualityDialog.show(getChildFragmentManager(), VideoDownloadQualityDialogFragment.getTAG());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (refreshOnResume) {
            courseViewModel.getCourseData(courseData.getCourseId(), courseComponentId, false,
                    false, CoursesRequestType.LIVE.INSTANCE);
            refreshOnResume = false;
        }
    }

    @Override
    public void onRevisit() {
        super.onRevisit();
        fetchLastAccessed();
        getCourseUpgradeFirebaseConfig();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void fetchLastAccessed() {
        if (isOnCourseOutline && !isVideoMode && environment.getLoginPrefs().isUserLoggedIn()) {
            courseApi.getCourseStatusInfo(courseData.getCourseId()).enqueue(
                    new ErrorHandlingCallback<CourseComponentStatusResponse>(
                            getContextOrThrow()) {
                        @Override
                        protected void onResponse(@NonNull final CourseComponentStatusResponse result) {
                            showResumeCourseView(result);
                        }

                        @Override
                        protected void onFailure(@NonNull Throwable error) {
                            //In case of failure no view will be added
                        }
                    });

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        final Bundle bundle = new Bundle();
        if (courseData != null)
            bundle.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
        if (courseUpgradeData != null)
            bundle.putParcelable(Router.EXTRA_COURSE_UPGRADE_DATA, courseUpgradeData);
        if (courseComponentId != null)
            bundle.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId);
        outState.putBundle(Router.EXTRA_BUNDLE, bundle);
        outState.putBoolean(Router.EXTRA_IS_VIDEOS_MODE, isVideoMode);
        outState.putBoolean("isSingleVideoDownload", isSingleVideoDownload);
        outState.putBoolean(Router.EXTRA_IS_ON_COURSE_OUTLINE, isOnCourseOutline);
    }

    public void reloadList() {
        if (adapter != null) {
            adapter.reloadData();
        }
    }

    public void updateRowSelection(@Nullable String lastAccessedId) {
        if (!TextUtils.isEmpty(lastAccessedId)) {
            final int selectedItemPosition = adapter.getPositionByItemId(lastAccessedId);
            if (selectedItemPosition != -1) {
                listView.setItemChecked(selectedItemPosition, true);
                listView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listView.smoothScrollToPosition(selectedItemPosition);
                    }
                }, AUTOSCROLL_DELAY_MS);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SHOW_COURSE_UNIT_DETAIL && resultCode == Activity.RESULT_OK
                && data != null) {
            if (data.getBooleanExtra(AppConstants.COURSE_UPGRADED, false)) {
                courseData.setMode(EnrollmentMode.VERIFIED.toString());
                if (!isOnCourseOutline) {
                    // As the Course Outline Fragment is used multiple time as a stack for CourseDashboard & SubComponents
                    // So need to pass data from SubComponent screen to CourseDashboard to update the views after user
                    // Purchase course from Locked Component
                    Intent resultData = new Intent();
                    resultData.putExtra(AppConstants.COURSE_UPGRADED, true);
                    requireActivity().setResult(Activity.RESULT_OK, resultData);
                    fetchCourseComponent();
                } else {
                    // Update the User CourseEnrollments & Dates banner if after user
                    // Purchase course from Locked Component
                    courseDateViewModel.fetchCourseDatesBannerInfo(courseData.getCourseId(), true);
                    EventBus.getDefault().post(new MyCoursesRefreshEvent());
                }
            } else {
                final CourseComponent outlineComp = courseManager.getComponentByIdFromAppLevelCache(
                        courseData.getCourseId(), courseComponentId);
                navigateToCourseUnit(data, courseData, outlineComp);
            }
        }
    }

    protected boolean isOnCourseOutline() {
        if (courseComponentId == null || getActivity() instanceof CourseTabsDashboardActivity) {
            return true;
        }
        final CourseComponent outlineComp = courseManager.getComponentByIdFromAppLevelCache(
                courseData.getCourseId(), courseComponentId);
        if (outlineComp != null) {
            final BlockPath outlinePath = outlineComp.getPath();
            final int outlinePathSize = outlinePath.getPath().size();

            return outlinePathSize <= 1;
        }
        return false;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEventMainThread(DownloadCompletedEvent e) {
        adapter.notifyDataSetChanged();
        updateBulkDownloadFragment();
    }

    @Subscribe(sticky = true)
    @SuppressWarnings("unused")
    public void onEventMainThread(MediaStatusChangeEvent e) {
        adapter.notifyDataSetChanged();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEventMainThread(DownloadedVideoDeletedEvent e) {
        adapter.notifyDataSetChanged();
        updateBulkDownloadFragment();
    }

    @Subscribe(sticky = true)
    @SuppressWarnings("unused")
    public void onEvent(CourseDashboardRefreshEvent e) {
        errorNotification.hideError();
        final Bundle arguments = getArguments();
        if (isOnCourseOutline() && arguments != null) {
            restore(arguments);
        }
        fetchCourseComponent();
    }

    @Subscribe(sticky = true)
    @SuppressWarnings("unused")
    public void onEvent(CourseOutlineRefreshEvent event) {
        errorNotification.hideError();
        if (isOnCourseOutline()) {
            refreshOnResume = true;
        }
    }

    @Subscribe(sticky = true)
    public void onEvent(CourseUpgradedEvent event) {
        if (!isOnCourseOutline || isVideoMode) {
            return;
        }
        // Hide payments banner
        updatePaymentsBannerVisibility(View.GONE);
        courseViewModel.getCourseData(courseData.getCourseId(), null, true, false,
                CoursesRequestType.LIVE.INSTANCE);
    }

    @Subscribe
    public void onEventMainThread(@NonNull IAPFlowEvent event) {
        // upgrade now button is only available on course outline
        if (!this.isResumed() || !isOnCourseOutline || isVideoMode) {
            return;
        }
        switch (event.getFlowAction()) {
            case SHOW_FULL_SCREEN_LOADER: {
                showFullscreenLoader(event.getIapFlowData());
                break;
            }
            case PURCHASE_FLOW_COMPLETE: {
                courseData.setMode(EnrollmentMode.VERIFIED.toString());
                courseViewModel.getCourseData(courseData.getCourseId(), null, false, false,
                        CoursesRequestType.LIVE.INSTANCE);
                EventBus.getDefault().post(new MyCoursesRefreshEvent());
                break;
            }
        }
    }

    public void showResumeCourseView(CourseComponentStatusResponse response) {
        if (getActivity() == null || TextUtils.isEmpty(response.getLastVisitedBlockId()))
            return;
        CourseComponent lastAccessComponent = courseManager.getComponentByIdFromAppLevelCache(courseData.getCourseId(), response.getLastVisitedBlockId());
        if (lastAccessComponent != null) {
            adapter.addResumeCourseView(lastAccessComponent);
        }
    }

    @Override
    public void onDownloadStarted(Long result) {
        reloadList();
        updateBulkDownloadFragment();
    }

    @Override
    public void onDownloadFailedToStart() {
        reloadList();
        updateBulkDownloadFragment();
    }

    @Override
    public void showProgressDialog(int numDownloads) {
    }

    @Override
    public void updateListUI() {
        reloadList();
        updateBulkDownloadFragment();
    }

    private void updateBulkDownloadFragment() {
        if (isAdded()) {
            final Fragment bulkDownloadFragment = getChildFragmentManager()
                    .findFragmentByTag("bulk_download");
            if (bulkDownloadFragment instanceof BulkDownloadFragment) {
                ((BulkDownloadFragment) bulkDownloadFragment).updateVideoStatus();
            }
        }
    }

    @Override
    public boolean showInfoMessage(String message) {
        final Activity activity = getActivity();
        return activity != null && activity instanceof BaseFragmentActivity && ((BaseFragmentActivity) getActivity()).showInfoMessage(message);
    }

    @Override
    public void onRefresh() {
        EventBus.getDefault().post(new CourseDashboardRefreshEvent());
    }

    @Subscribe(sticky = true)
    @SuppressWarnings("unused")
    public void onEvent(NetworkConnectivityChangeEvent event) {
        onNetworkConnectivityChangeEvent(event);
    }

    @Override
    protected boolean isShowingFullScreenError() {
        return errorNotification != null && errorNotification.isShowing();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (getCourseUpgradeStatus != null) {
            getCourseUpgradeStatus.cancel();
            getCourseUpgradeStatus = null;
        }
    }

    public boolean canUpdateRowSelection() {
        return true;
    }
}
