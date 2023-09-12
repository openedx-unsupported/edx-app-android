package org.edx.mobile.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import org.edx.mobile.extenstion.ViewExtKt;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.http.notifications.SnackbarErrorNotification;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.CourseUpgradeResponse;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.BlockPath;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.EnrollmentMode;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.course.SectionRow;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.iap.IAPFlowData;
import org.edx.mobile.model.video.VideoQuality;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.storage.BulkVideosDownloadCancelledEvent;
import org.edx.mobile.module.storage.DownloadCompletedEvent;
import org.edx.mobile.module.storage.DownloadedVideoDeletedEvent;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.services.EdxCookieManager;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.ConfigUtil;
import org.edx.mobile.util.MediaConsentUtils;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.UiUtils;
import org.edx.mobile.util.observer.EventObserver;
import org.edx.mobile.view.adapters.CourseOutlineAdapter;
import org.edx.mobile.view.dialog.DownloadSizeExceedDialog;
import org.edx.mobile.view.dialog.FullscreenLoaderDialogFragment;
import org.edx.mobile.view.dialog.IDialogCallback;
import org.edx.mobile.view.dialog.VideoDownloadQualityDialogFragment;
import org.edx.mobile.viewModel.CourseViewModel;
import org.edx.mobile.viewModel.CourseViewModel.CoursesRequestType;
import org.edx.mobile.viewModel.VideoViewModel;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import kotlin.Pair;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class CourseOutlineFragment extends OfflineSupportBaseFragment implements RefreshListener {
    private final Logger logger = new Logger(getClass().getName());
    private static final int AUTOSCROLL_DELAY_MS = 500;

    private CourseOutlineAdapter adapter;
    private ListView listView;
    private EnrolledCoursesResponse courseData;
    private String courseComponentId;
    private boolean isVideoMode;
    private boolean isOnCourseOutline;
    // Flag to differentiate between single or multiple video download
    private boolean isSingleVideoDownload;
    private DownloadEntry downloadEntry;
    private List<? extends HasDownloadEntry> downloadEntries;
    private SwipeRefreshLayout swipeContainer;

    private FullScreenErrorNotification errorNotification;

    @Inject
    CourseAPI courseApi;

    private CourseViewModel courseViewModel;

    private VideoViewModel videoViewModel;

    private View loadingIndicator;
    private FrameLayout flBulkDownload;
    private View videoQualityLayout;
    private TextView tvVideoDownloadQuality;
    private CourseOutlineAdapter.DownloadListener downloadListener;
    private Call<CourseUpgradeResponse> getCourseUpgradeStatus;
    private CourseUpgradeResponse courseUpgradeData;
    private String screenName;

    private boolean refreshOnResume = false;

    private final ActivityResultLauncher<Intent> courseUnitDetailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent resultData = result.getData();
                if (result.getResultCode() == Activity.RESULT_OK && resultData != null) {
                    if (resultData.getBooleanExtra(AppConstants.COURSE_UPGRADED, false)) {
                        courseData.setMode(EnrollmentMode.VERIFIED.toString());
                        if (!isOnCourseOutline) {
                            // As the Course Outline Fragment is used multiple time as a stack for CourseDashboard & SubComponents
                            // So need to pass data from SubComponent screen to CourseDashboard to update the views after user
                            // Purchase course from Locked Component
                            Intent intent = new Intent();
                            intent.putExtra(AppConstants.COURSE_UPGRADED, true);
                            requireActivity().setResult(Activity.RESULT_OK, intent);
                            fetchCourseComponent();
                        } else {
                            EventBus.getDefault().post(new MyCoursesRefreshEvent());
                        }
                    } else {
                        final CourseComponent outlineComp = courseManager.getComponentByIdFromAppLevelCache(
                                courseData.getCourseId(), courseComponentId);
                        navigateToCourseUnit(resultData, courseData, outlineComp);
                    }
                }
            });

    private final ActivityResultLauncher<String> storagePermissionLauncher = registerForActivityResult(
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
                courseViewModel.getCourseData(courseData.getCourseId(), null, false, true,
                        CoursesRequestType.LIVE.INSTANCE);
            }
        });
        UiUtils.INSTANCE.setSwipeRefreshLayoutColors(swipeContainer);
        restore(bundle);
        initListView();
        initVideoObserver();

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

    private void initVideoObserver() {
        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);

        videoViewModel.getRefreshUI().observe(getViewLifecycleOwner(), new EventObserver<>(refresh -> {
            if (refresh) {
                updateListUI();
            }
            return null;
        }));

        videoViewModel.getInfoMessage().observe(getViewLifecycleOwner(), new EventObserver<>(strResId -> {
            showInfoMessage(getString(strResId));
            return null;
        }));

        videoViewModel.getDownloadSizeExceeded().observe(getViewLifecycleOwner(), new EventObserver<>(downloads -> {
            if (!downloads.isEmpty()) {
                IDialogCallback callback = new IDialogCallback() {
                    @Override
                    public void onPositiveClicked() {
                        videoViewModel.startDownload(downloads, true);
                    }

                    @Override
                    public void onNegativeClicked() {
                        EventBus.getDefault().post(new BulkVideosDownloadCancelledEvent());
                    }
                };

                DownloadSizeExceedDialog.newInstance(callback).show(
                        requireActivity().getSupportFragmentManager(), "dialog"
                );
            }
            return null;
        }));

        videoViewModel.getSelectedVideosPosition().observe(getViewLifecycleOwner(), new EventObserver<>(position -> {
            if (position.getSecond() != ListView.INVALID_POSITION && position.getSecond() == listView.getCheckedItemPosition()) {
                deleteDownloadedVideosAtPosition(position.getSecond());
            }
            return null;
        }));

        videoViewModel.getClearChoices().observe(getViewLifecycleOwner(), new EventObserver<>(shouldClear -> {
            if (shouldClear) {
                listView.clearChoices();
                listView.requestLayout();
            }
            return null;
        }));
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
                fullscreenLoader.closeLoader(null);
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
                    if (throwable instanceof CourseContentNotValidException) {
                        errorNotification.showError(requireContext(), throwable);
                        logger.error(throwable, true);
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

    private void initListView() {
        initAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            listView.clearChoices();
            final CourseComponent component = adapter.getItem(position).getComponent();
            showComponentDetailScreen(component);
        });

        listView.setOnItemLongClickListener((parent, itemView, position, id) -> {
            final AppCompatImageView bulkDownloadIcon = (AppCompatImageView) itemView.findViewById(R.id.bulk_download);
            if (bulkDownloadIcon != null && bulkDownloadIcon.getTag() != null &&
                    (Integer) bulkDownloadIcon.getTag() == R.drawable.ic_download_done) {
                VideoMoreOptionsBottomSheet.newInstance(new Pair<>(0, position)).show(getChildFragmentManager(), null);
                listView.setItemChecked(position, true);
                return true;
            }
            return false;
        });
    }

    private void showComponentDetailScreen(CourseComponent component) {
        Intent intent = environment.getRouter().getCourseUnitDetailIntent(requireActivity(),
                courseData, courseUpgradeData, component.getId(), isVideoMode);
        environment.getAnalyticsRegistry().trackScreenView(
                Analytics.Screens.UNIT_DETAIL, courseData.getCourse().getId(), component.getParent().getInternalName());
        courseUnitDetailLauncher.launch(intent);
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
                        storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }

                @Override
                public void download(DownloadEntry videoData) {
                    downloadEntry = videoData;
                    isSingleVideoDownload = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        onPermissionGranted();
                    } else {
                        storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }

                @Override
                public void viewDownloadsStatus() {
                    environment.getRouter().showDownloads(getActivity());
                }
            };
            adapter = new CourseOutlineAdapter(requireActivity(), courseData, environment, downloadListener,
                    isVideoMode, isOnCourseOutline);
        }
    }

    private void detectDeepLinking() {
        if (Screen.COURSE_COMPONENT.equalsIgnoreCase(screenName) &&
                !TextUtils.isEmpty(courseComponentId)) {
            Intent courseUnitDetailIntent = environment.getRouter()
                    .getCourseUnitDetailIntent(requireActivity(), courseData, courseUpgradeData,
                            courseComponentId, isVideoMode);
            courseUnitDetailLauncher.launch(courseUnitDetailIntent);
            screenName = null;
        }
    }

    public void onPermissionGranted() {
        if (isSingleVideoDownload) {
            videoViewModel.downloadSingleVideo(downloadEntry);
        } else {
            MediaConsentUtils.requestStreamMedia(requireActivity(), new IDialogCallback() {
                @Override
                public void onPositiveClicked() {
                    videoViewModel.downloadMultipleVideos((List<HasDownloadEntry>) downloadEntries);
                }

                @Override
                public void onNegativeClicked() {
                    showInfoMessage(getString(R.string.wifi_off_message));
                    EventBus.getDefault().post(new BulkVideosDownloadCancelledEvent());
                }
            });
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

    private void deleteDownloadedVideosAtPosition(int checkedItemPosition) {
        // Change the icon to download icon immediately
        final View rowView = listView.getChildAt(checkedItemPosition - listView.getFirstVisiblePosition());
        if (rowView != null) {
            // rowView will be null, if the user scrolls away from the checked item
            final AppCompatImageView bulkDownloadIcon = rowView.findViewById(R.id.bulk_download);
            bulkDownloadIcon.setImageDrawable(UiUtils.INSTANCE.getDrawable(requireContext(), R.drawable.ic_download));
            bulkDownloadIcon.setTag(R.drawable.ic_download);
        }

        final SectionRow rowItem = adapter.getItem(checkedItemPosition);
        final List<CourseComponent> videos = rowItem.getComponent().getVideos(true);

        if (isOnCourseOutline) {
            environment.getAnalyticsRegistry().trackSubsectionVideosDelete(courseData.getCourseId(),
                    rowItem.getComponent().getId());
        } else {
            environment.getAnalyticsRegistry().trackUnitVideoDelete(courseData.getCourseId(),
                    rowItem.getComponent().getId());
        }

        showVideosDeletedSnackBar(rowItem, videos);
    }

    private void showVideosDeletedSnackBar(SectionRow rowItem, List<CourseComponent> videos) {
        /*
          The android docs have NOT been updated yet, but if you jump into the source code you'll
          notice that the parameter to the method setDuration(int duration) can either be one of
          LENGTH_SHORT, LENGTH_LONG, LENGTH_INDEFINITE or a custom duration in milliseconds.

          https://stackoverflow.com/a/30552666
          https://github.com/material-components/material-components-android/commit/2cb77c9331cc3c6a5034aace0238b96508acf47d
         */
        @SuppressLint("WrongConstant") final Snackbar snackbar = Snackbar.make(
                listView,
                getResources().getQuantityString(
                        R.plurals.delete_video_snackbar_msg,
                        videos.size(),
                        videos.size()
                ),
                AppConstants.SNACKBAR_SHOWTIME_MS
        );

        snackbar.setAction(R.string.label_undo, view -> {
            // No need of implementation as we'll handle the action in SnackBar's
            // onDismissed callback.
        });
        snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<>() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                // SnackBar is being dismissed by any action other than its action button's press
                if (event != DISMISS_EVENT_ACTION) {
                    final IStorage storage = environment.getStorage();
                    for (CourseComponent video : videos) {
                        final VideoBlockModel videoBlockModel = (VideoBlockModel) video;
                        final DownloadEntry downloadEntry = videoBlockModel.getDownloadEntry(storage);
                        if (downloadEntry != null && downloadEntry.isDownloaded()) {
                            // This check is necessary because, this callback gets called multiple
                            // times when SnackBar is about to dismiss and the activity finishes
                            storage.removeDownload(downloadEntry);
                        } else {
                            return;
                        }
                    }
                } else {
                    if (isOnCourseOutline) {
                        environment.getAnalyticsRegistry().trackUndoingSubsectionVideosDelete(
                                courseData.getCourseId(), rowItem.getComponent().getId());
                    } else {
                        environment.getAnalyticsRegistry().trackUndoingUnitVideoDelete(
                                courseData.getCourseId(), rowItem.getComponent().getId());
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
        snackbar.show();
    }

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
        getCourseUpgradeFirebaseConfig();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
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

    public boolean showInfoMessage(String message) {
        final Activity activity = getActivity();
        return activity instanceof BaseFragmentActivity && ((BaseFragmentActivity) getActivity()).showInfoMessage(message);
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
