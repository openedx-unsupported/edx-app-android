package org.edx.mobile.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.inject.Inject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconImageView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.databinding.LayoutCourseDatesBannerBinding;
import org.edx.mobile.deeplink.Screen;
import org.edx.mobile.event.CourseDashboardRefreshEvent;
import org.edx.mobile.event.CourseUpgradedEvent;
import org.edx.mobile.event.MediaStatusChangeEvent;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.exception.AuthException;
import org.edx.mobile.exception.CourseContentNotValidException;
import org.edx.mobile.exception.ErrorMessage;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.http.notifications.SnackbarErrorNotification;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.loader.AsyncTaskResult;
import org.edx.mobile.loader.CourseOutlineAsyncLoader;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.CourseUpgradeResponse;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.course.BlockPath;
import org.edx.mobile.model.course.CourseBannerInfoModel;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.storage.DownloadCompletedEvent;
import org.edx.mobile.module.storage.DownloadedVideoDeletedEvent;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.services.EdxCookieManager;
import org.edx.mobile.services.LastAccessManager;
import org.edx.mobile.services.VideoDownloadHelper;
import org.edx.mobile.util.ConfigUtil;
import org.edx.mobile.util.CourseDateUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.PermissionsUtil;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.view.adapters.CourseOutlineAdapter;
import org.edx.mobile.view.common.TaskProgressCallback;
import org.edx.mobile.viewModel.CourseDateViewModel;
import org.edx.mobile.viewModel.ViewModelFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseOutlineFragment extends OfflineSupportBaseFragment
        implements LastAccessManager.LastAccessManagerCallback, RefreshListener,
        VideoDownloadHelper.DownloadManagerCallback,
        LoaderManager.LoaderCallbacks<AsyncTaskResult<CourseComponent>>, BaseFragment.PermissionListener {
    private final Logger logger = new Logger(getClass().getName());
    private static final int REQUEST_SHOW_COURSE_UNIT_DETAIL = 0;
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
    private boolean isFetchingLastAccessed;
    // Flag to differentiate between single or multiple video download
    private boolean isSingleVideoDownload;
    private ActionMode deleteMode;
    private DownloadEntry downloadEntry;
    private List<? extends HasDownloadEntry> downloadEntries;
    private SwipeRefreshLayout swipeContainer;

    private Call<CourseStructureV1Model> getHierarchyCall;

    private FullScreenErrorNotification errorNotification;

    @Inject
    private CourseManager courseManager;

    @Inject
    private CourseAPI courseApi;

    @Inject
    private LastAccessManager lastAccessManager;

    @Inject
    private VideoDownloadHelper downloadManager;

    private CourseDateViewModel courseDateViewModel;

    @Inject
    protected IEdxEnvironment environment;

    private View loadingIndicator;
    private FrameLayout flBulkDownload;
    private CourseOutlineAdapter.DownloadListener downloadListener;
    private Call<CourseUpgradeResponse> getCourseUpgradeStatus;
    private CourseUpgradeResponse courseUpgradeData;

    public static Bundle makeArguments(@NonNull EnrolledCoursesResponse model,
                                       @Nullable String courseComponentId,
                                       @Nullable String lastAccessedId, boolean isVideosMode) {
        final Bundle arguments = new Bundle();
        final Bundle courseBundle = new Bundle();
        courseBundle.putSerializable(Router.EXTRA_COURSE_DATA, model);
        courseBundle.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId);

        arguments.putBundle(Router.EXTRA_BUNDLE, courseBundle);
        arguments.putString(Router.EXTRA_LAST_ACCESSED_ID, lastAccessedId);
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
        courseDateViewModel = new ViewModelProvider(this, new ViewModelFactory()).get(CourseDateViewModel.class);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Hide the progress bar as swipe layout has its own progress indicator
                loadingIndicator.setVisibility(View.GONE);
                errorNotification.hideError();
                canFetchBannerInfo = true;
                getCourseComponentFromServer(false);
            }
        });
        UiUtil.setSwipeRefreshLayoutColors(swipeContainer);
        restore(bundle);
        initListView(view);
        initObserver();
        fetchCourseComponent();
        // Track CourseOutline for A/A test
        trackAATestCourseOutline();
        getCourseUpgradeFirebaseConfig();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        permissionListener = this;
        updateRowSelection(getArguments().getString(Router.EXTRA_LAST_ACCESSED_ID));
    }

    private void initObserver() {
        courseDateViewModel.getBannerInfo().observe(this, this::initDatesBanner);

        courseDateViewModel.getShowLoader().observe(this, flag ->
                loadingIndicator.setVisibility(flag ? View.VISIBLE : View.GONE));

        courseDateViewModel.getSwipeRefresh().observe(this, canRefresh ->
                swipeContainer.setRefreshing(canRefresh));

        courseDateViewModel.getResetCourseDates().observe(this, resetCourseDates -> {
            if (resetCourseDates != null) {
                showShiftDateSnackBar(true);
            }
        });

        courseDateViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                if (errorMessage.getThrowable() instanceof AuthException || errorMessage.getThrowable() instanceof HttpStatusException &&
                        ((HttpStatusException) errorMessage.getThrowable()).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    environment.getRouter().forceLogout(getContextOrThrow(),
                            environment.getAnalyticsRegistry(),
                            environment.getNotificationDelegate());
                } else {
                    switch (errorMessage.getErrorCode()) {
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

    private void restore(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            final Bundle bundle = savedInstanceState.getBundle(Router.EXTRA_BUNDLE);
            courseData = (EnrolledCoursesResponse) bundle.getSerializable(Router.EXTRA_COURSE_DATA);
            courseUpgradeData = bundle.getParcelable(Router.EXTRA_COURSE_UPGRADE_DATA);
            courseComponentId = bundle.getString(Router.EXTRA_COURSE_COMPONENT_ID);
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
        final String courseId = courseData.getCourse().getId();
        if (courseComponentId != null) {
            final CourseComponent courseComponent = courseManager.getComponentByIdFromAppLevelCache(courseId, courseComponentId);
            if (courseComponent != null) {
                // Course data exist in app session cache
                loadData(courseComponent);
                return;
            }
        }
        // Check if course data is available in app session cache
        final CourseComponent courseComponent = courseManager.getCourseDataFromAppLevelCache(courseId);
        if (courseComponent != null) {
            // Course data exist in app session cache
            loadData(courseComponent);
            return;
        }
        // Check if course data is available in persistable cache
        loadingIndicator.setVisibility(View.VISIBLE);
        // Prepare the loader. Either re-connect with an existing one or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    private void trackAATestCourseOutline() {
        if (!isVideoMode &&
                environment.getConfig().getFirebaseConfig().isEnabled()) {
            final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
                final String group = firebaseRemoteConfig.getString(Analytics.Keys.AA_EXPERIMENT);
                final ProfileModel profileModel = environment.getLoginPrefs().getCurrentUserProfile();
                if (!TextUtils.isEmpty(group) && profileModel != null) {
                    final Map<String, String> values = new HashMap<>();
                    values.put(Analytics.Keys.EXPERIMENT, Analytics.Keys.AA_EXPERIMENT);
                    values.put(Analytics.Keys.GROUP, group);
                    values.put(Analytics.Keys.USER_ID, profileModel.id.toString());
                    values.put(Analytics.Keys.COURSE_ID, courseData.getCourse().getId());
                    environment.getAnalyticsRegistry().trackExperimentParams(Analytics.Events.MOBILE_EXPERIMENT_EVALUATED, values);
                }
            });
        }
    }

    @NonNull
    @Override
    public Loader<AsyncTaskResult<CourseComponent>> onCreateLoader(int id, Bundle args) {
        final String blocksApiVersion = environment.getConfig().getApiUrlVersionConfig().getBlocksApiVersion();
        return new CourseOutlineAsyncLoader(getContext(), blocksApiVersion, courseData.getCourse().getId());
    }

    @Override
    public void onLoadFinished(Loader<AsyncTaskResult<CourseComponent>> loader, AsyncTaskResult<CourseComponent> result) {
        final CourseComponent courseComponent = result.getResult();
        if (courseComponent != null) {
            // Course data exist in persistable cache
            loadData(validateCourseComponent(courseComponent));
            loadingIndicator.setVisibility(View.GONE);
            // Send a server call in background for refreshed data
            getCourseComponentFromServer(false);
        } else {
            // Course data is neither available in app session cache nor available in persistable cache
            getCourseComponentFromServer(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<AsyncTaskResult<CourseComponent>> loader) {
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    public void getCourseComponentFromServer(boolean showProgress) {
        if (loadingIndicator.getVisibility() == View.VISIBLE) {
            showProgress = true;
        }
        final TaskProgressCallback progressCallback = showProgress ?
                new TaskProgressCallback.ProgressViewController(loadingIndicator) : null;
        final String blocksApiVersion = environment.getConfig().getApiUrlVersionConfig().getBlocksApiVersion();
        final String courseId = courseData.getCourse().getId();
        getHierarchyCall = courseApi.getCourseStructureWithoutStale(blocksApiVersion, courseId);
        getHierarchyCall.enqueue(new CourseAPI.GetCourseStructureCallback(getActivity(), courseId,
                progressCallback, errorNotification, null, this) {
            @Override
            protected void onResponse(@NonNull final CourseComponent courseComponent) {
                courseManager.addCourseDataInAppLevelCache(courseId, courseComponent);
                loadData(validateCourseComponent(courseComponent));
                swipeContainer.setRefreshing(false);
            }

            @Override
            protected void onFailure(@NonNull Throwable error) {
                super.onFailure(error);
                if (error instanceof CourseContentNotValidException) {
                    errorNotification.showError(getContext(), error);
                    logger.error(error, true);
                }
                swipeContainer.setRefreshing(false);
                // Remove bulk video download if the course has NO downloadable videos
                UiUtil.removeFragmentByTag(CourseOutlineFragment.this, "bulk_download");
            }

            @Override
            protected void onFinish() {
                if (!EventBus.getDefault().isRegistered(CourseOutlineFragment.this)) {
                    EventBus.getDefault().registerSticky(CourseOutlineFragment.this);
                }
                swipeContainer.setRefreshing(false);
            }
        });
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
                    courseData.getCourse().getId(), courseComponentId);
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
                    environment.getRouter().showCourseUnitDetail(CourseOutlineFragment.this,
                            REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, courseUpgradeData, component.getId(), isVideoMode);
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Checking if the onItemLongClick action performed on course dates banner
                if (!isVideoMode && isBannerVisible) {
                    if (position == 0)
                        return false;
                    else
                        position -= 1;
                }
                final IconImageView bulkDownloadIcon = (IconImageView) view.findViewById(R.id.bulk_download);
                if (bulkDownloadIcon != null && bulkDownloadIcon.getIcon() == FontAwesomeIcons.fa_check) {
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
                    askForPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST);
                }

                @Override
                public void download(DownloadEntry videoData) {
                    downloadEntry = videoData;
                    isSingleVideoDownload = true;
                    askForPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST);
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
            bannerViewBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.layout_course_dates_banner, listView, false);

        if (courseBannerInfo != null && !isVideoMode && isOnCourseOutline && !courseBannerInfo.getHasEnded()) {
            CourseDateUtil.INSTANCE.setupCourseDatesBanner(bannerViewBinding.getRoot(), courseData.getCourse().getId(),
                    courseData.getMode(), Analytics.Screens.PLS_COURSE_DASHBOARD, environment.getAnalyticsRegistry(), courseBannerInfo,
                    v -> courseDateViewModel.resetCourseDatesBanner(courseData.getCourse().getId()));

            if (listView.getHeaderViewsCount() == 0 && !TextUtils.isEmpty(bannerViewBinding.bannerInfo.getText())) {
                listView.addHeaderView(bannerViewBinding.getRoot());
                isBannerVisible = true;
            }
        } else {
            listView.removeHeaderView(bannerViewBinding.getRoot());
            isBannerVisible = false;
        }
    }

    private void showShiftDateSnackBar(boolean isSuccess) {
        SnackbarErrorNotification snackbarErrorNotification = new SnackbarErrorNotification(listView);
        if (isSuccess) {
            snackbarErrorNotification.showError(R.string.assessment_shift_dates_success_msg,
                    null, R.string.assessment_view_all_dates, SnackbarErrorNotification.COURSE_DATE_MESSAGE_DURATION,
                    v -> environment.getRouter().showCourseDashboardTabs(getActivity(), null, courseData.getCourse().getId(),
                            null, null, false, Screen.COURSE_DATES));
        } else {
            snackbarErrorNotification.showError(R.string.course_dates_reset_unsuccessful, null,
                    0, SnackbarErrorNotification.COURSE_DATE_MESSAGE_DURATION, null);
        }
        environment.getAnalyticsRegistry().trackPLSCourseDatesShift(courseData.getCourse().getId(),
                courseData.getMode(), Analytics.Screens.PLS_COURSE_DASHBOARD, isSuccess);
    }

    @Override
    public void onPermissionGranted(String[] permissions, int requestCode) {
        switch (requestCode) {
            case PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST:
                if (isSingleVideoDownload) {
                    downloadManager.downloadVideo(downloadEntry, getActivity(), CourseOutlineFragment.this);
                } else {
                    downloadManager.downloadVideos(downloadEntries, getActivity(), CourseOutlineFragment.this);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onPermissionDenied(String[] permissions, int requestCode) {
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
                    new IconDrawable(getContext(), FontAwesomeIcons.fa_trash_o)
                            .colorRes(getContext(), R.color.white)
                            .actionBarSize(getContext())
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
                        ((IconImageView) rowView.findViewById(R.id.bulk_download)).setIcon(FontAwesomeIcons.fa_download);
                    }

                    final CourseOutlineAdapter.SectionRow rowItem = adapter.getItem(checkedItemPosition);
                    final List<CourseComponent> videos = rowItem.component.getVideos(true);
                    final int totalVideos = videos.size();

                    if (isOnCourseOutline) {
                        environment.getAnalyticsRegistry().trackSubsectionVideosDelete(
                                courseData.getCourse().getId(), rowItem.component.getId());
                    } else {
                        environment.getAnalyticsRegistry().trackUnitVideoDelete(
                                courseData.getCourse().getId(), rowItem.component.getId());
                    }

                    /*
                    The android docs have NOT been updated yet, but if you jump into the source code
                    you'll notice that the parameter to the method setDuration(int duration) can
                    either be one of LENGTH_SHORT, LENGTH_LONG, LENGTH_INDEFINITE or a custom
                    duration in milliseconds.
                    https://stackoverflow.com/a/30552666
                    https://github.com/material-components/material-components-android/commit/2cb77c9331cc3c6a5034aace0238b96508acf47d
                     */
                    @SuppressLint("WrongConstant")
                    final Snackbar snackbar = Snackbar.make(listView,
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
                                            courseData.getCourse().getId(), rowItem.component.getId());
                                } else {
                                    environment.getAnalyticsRegistry().trackUndoingUnitVideoDelete(
                                            courseData.getCourse().getId(), rowItem.component.getId());
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
     * @param courseComponent Components of course to be load
     */
    private void loadData(@NonNull CourseComponent courseComponent) {
        courseComponentId = courseComponent.getId();
        if (courseData == null || getActivity() == null)
            return;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().registerSticky(this);
        }
        if (!isOnCourseOutline) {
            // We only need to set the title of Course Outline screen, where we show a subsection's units
            getActivity().setTitle(courseComponent.getDisplayName());
        }
        if (!isVideoMode && isOnCourseOutline && canFetchBannerInfo) {
            courseDateViewModel.fetchCourseDatesBannerInfo(courseData.getCourse().getId(), swipeContainer.isRefreshing());
            canFetchBannerInfo = false;
        }

        adapter.setData(courseComponent);
        if (adapter.hasCourseData()) {
            setUpBulkDownloadHeader(courseComponent);
            errorNotification.hideError();
        } else {
            // Remove bulk video download if the course has NO downloadable videos
            UiUtil.removeFragmentByTag(CourseOutlineFragment.this, "bulk_download");
            errorNotification.showError(isVideoMode ? R.string.no_videos_text : R.string.no_chapter_text, null, -1, null);
        }

        if (!isOnCourseOutline) {
            environment.getAnalyticsRegistry().trackScreenView(
                    Analytics.Screens.SECTION_OUTLINE, courseData.getCourse().getId(), courseComponent.getInternalName());

            // Update the last accessed item reference if we are in the course subsection view
            lastAccessManager.setLastAccessed(courseComponent.getCourseId(), courseComponent.getId());
        }

        fetchLastAccessed();
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
            getCourseUpgradeStatus = courseApi.getCourseUpgradeStatus(courseData.getCourse().getId());
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
                UiUtil.removeFragmentByTag(CourseOutlineFragment.this, "bulk_download");
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
        if (isOnCourseOutline && !isVideoMode) {
            lastAccessManager.fetchLastAccessed(this, courseData.getCourse().getId());
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

    private void updateRowSelection(@Nullable String lastAccessedId) {
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
        switch (requestCode) {
            // If user has navigated to a different unit, then we need to rearrange
            // the activity stack to point to it.
            case REQUEST_SHOW_COURSE_UNIT_DETAIL: {
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        final CourseComponent outlineComp = courseManager.getComponentByIdFromAppLevelCache(
                                courseData.getCourse().getId(), courseComponentId);
                        final String leafCompId = (String) data.getSerializableExtra(Router.EXTRA_COURSE_COMPONENT_ID);
                        final CourseComponent leafComp = courseManager.getComponentByIdFromAppLevelCache(
                                courseData.getCourse().getId(), leafCompId);
                        final BlockPath outlinePath = outlineComp.getPath();
                        final BlockPath leafPath = leafComp.getPath();
                        final int outlinePathSize = outlinePath.getPath().size();
                        if (!outlineComp.equals(leafPath.get(outlinePathSize - 1))) {
                            getActivity().setResult(Activity.RESULT_OK, data);
                            getActivity().finish();
                        } else {
                            final int leafPathSize = leafPath.getPath().size();
                            if (outlinePathSize == leafPathSize - 2) {
                                updateRowSelection(leafCompId);
                            } else {
                                for (int i = outlinePathSize + 1; i < leafPathSize - 1; i += 2) {
                                    final CourseComponent nextComp = leafPath.get(i);
                                    environment.getRouter().showCourseContainerOutline(
                                            CourseOutlineFragment.this,
                                            REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, courseUpgradeData,
                                            nextComp.getId(), leafCompId, isVideoMode);
                                }
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    protected boolean isOnCourseOutline() {
        if (courseComponentId == null) return true;
        final CourseComponent outlineComp = courseManager.getComponentByIdFromAppLevelCache(
                courseData.getCourse().getId(), courseComponentId);
        final BlockPath outlinePath = outlineComp.getPath();
        final int outlinePathSize = outlinePath.getPath().size();

        return outlinePathSize <= 1;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DownloadCompletedEvent e) {
        adapter.notifyDataSetChanged();
        updateBulkDownloadFragment();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(MediaStatusChangeEvent e) {
        adapter.notifyDataSetChanged();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DownloadedVideoDeletedEvent e) {
        adapter.notifyDataSetChanged();
        updateBulkDownloadFragment();
    }

    @SuppressWarnings("unused")
    public void onEvent(CourseDashboardRefreshEvent e) {
        errorNotification.hideError();
        final Bundle arguments = getArguments();
        if (isOnCourseOutline() && arguments != null) {
            restore(arguments);
        }
        fetchCourseComponent();
    }

    public void onEvent(CourseUpgradedEvent event) {
        if (!isOnCourseOutline || isVideoMode) {
            return;
        }
        // Hide payments banner
        updatePaymentsBannerVisibility(View.GONE);
        getCourseComponentFromServer(true);
    }

    @Override
    public boolean isFetchingLastAccessed() {
        return isFetchingLastAccessed;
    }

    @Override
    public void setFetchingLastAccessed(boolean accessed) {
        isFetchingLastAccessed = accessed;
    }

    @Override
    public void showLastAccessedView(String lastAccessedSubSectionId, String courseId, View view) {
        if (getActivity() == null)
            return;
        if (NetworkUtil.isConnected(getContext())) {
            if (courseId != null && lastAccessedSubSectionId != null) {
                CourseComponent lastAccessComponent = courseManager.getComponentByIdFromAppLevelCache(courseId, lastAccessedSubSectionId);
                if (lastAccessComponent != null) {
                    if (!lastAccessComponent.isContainer()) {   // true means its a course unit
                        // getting subsection
                        if (lastAccessComponent.getParent() != null)
                            lastAccessComponent = lastAccessComponent.getParent();
                        // now getting section
                        if (lastAccessComponent.getParent() != null) {
                            lastAccessComponent = lastAccessComponent.getParent();
                        }
                    }

                    // Handling the border case that if the Last Accessed component turns out
                    // to be the course root component itself, then we don't need to show it
                    if (!lastAccessComponent.getId().equals(courseId)) {
                        final CourseComponent finalLastAccessComponent = lastAccessComponent;
                        adapter.addLastAccessedView(finalLastAccessComponent, new View.OnClickListener() {
                            long lastClickTime = 0;

                            @Override
                            public void onClick(View v) {
                                //This has been used so that if user clicks continuously on the screen,
                                //two activities should not be opened
                                long currentTime = SystemClock.elapsedRealtime();
                                if (currentTime - lastClickTime > 1000) {
                                    lastClickTime = currentTime;
                                    environment.getRouter().showCourseContainerOutline(getActivity(),
                                            courseData, courseUpgradeData, finalLastAccessComponent.getId());
                                }
                            }
                        });
                    }
                }
            }
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
        if (getHierarchyCall != null) {
            getHierarchyCall.cancel();
            getHierarchyCall = null;
        }
        if (getCourseUpgradeStatus != null) {
            getCourseUpgradeStatus.cancel();
            getCourseUpgradeStatus = null;
        }
    }
}
