package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.inject.Inject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconImageView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.event.CourseDashboardRefreshEvent;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.loader.AsyncTaskResult;
import org.edx.mobile.loader.CourseOutlineAsyncLoader;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.BlockPath;
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
import org.edx.mobile.services.LastAccessManager;
import org.edx.mobile.services.VideoDownloadHelper;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.adapters.NewCourseOutlineAdapter;
import org.edx.mobile.view.common.TaskProgressCallback;

import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit2.Call;

/**
 * TODO: Rename class name to 'CourseOutlineFragment' once old/deprecated  {@link CourseOutlineFragment} is deleted.
 */
public class NewCourseOutlineFragment extends OfflineSupportBaseFragment
        implements LastAccessManager.LastAccessManagerCallback, RefreshListener,
        VideoDownloadHelper.DownloadManagerCallback,
        LoaderManager.LoaderCallbacks<AsyncTaskResult<CourseComponent>> {
    private final Logger logger = new Logger(getClass().getName());
    private static final int REQUEST_SHOW_COURSE_UNIT_DETAIL = 0;
    private static final int AUTOSCROLL_DELAY_MS = 500;
    private static final int SNACKBAR_SHOWTIME_MS = 5000;

    private NewCourseOutlineAdapter adapter;
    private ListView listView;
    private EnrolledCoursesResponse courseData;
    private String courseComponentId;
    private boolean isVideoMode;
    private boolean isOnCourseOutline;
    private boolean isFetchingLastAccessed;
    private ActionMode deleteMode;

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

    @Inject
    protected IEdxEnvironment environment;

    private View loadingIndicator;

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
        restore(bundle);

        final View view = inflater.inflate(R.layout.fragment_course_outline_new, container, false);
        initListView(view);
        errorNotification = new FullScreenErrorNotification(listView);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        fetchCourseComponent();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateRowSelection(getArguments().getString(Router.EXTRA_LAST_ACCESSED_ID));
    }

    private void restore(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            final Bundle bundle = savedInstanceState.getBundle(Router.EXTRA_BUNDLE);
            courseData = (EnrolledCoursesResponse) bundle.getSerializable(Router.EXTRA_COURSE_DATA);
            courseComponentId = bundle.getString(Router.EXTRA_COURSE_COMPONENT_ID);
            isVideoMode = savedInstanceState.getBoolean(Router.EXTRA_IS_VIDEOS_MODE);
            isOnCourseOutline = isOnCourseOutline();
        }
    }

    private void fetchCourseComponent() {
        final String courseId = courseData.getCourse().getId();
        if (courseComponentId != null) {
            // Its not a course outline so course data would definitely exist in cache
            loadData(courseManager.getComponentByIdFromAppLevelCache(courseId, courseComponentId));
            return;
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

    @Override
    public Loader<AsyncTaskResult<CourseComponent>> onCreateLoader(int id, Bundle args) {
        return new CourseOutlineAsyncLoader(getContext(), courseData.getCourse().getId());
    }

    @Override
    public void onLoadFinished(Loader<AsyncTaskResult<CourseComponent>> loader, AsyncTaskResult<CourseComponent> result) {
        final CourseComponent courseComponent = result.getResult();
        if (courseComponent != null) {
            // Course data exist in persistable cache
            loadData(courseComponent);
            loadingIndicator.setVisibility(View.GONE);
            // Send a server call in background for refreshed data
            getCourseComponentFromServer(false);
            return;
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
        final TaskProgressCallback progressCallback = showProgress ?
                new TaskProgressCallback.ProgressViewController(loadingIndicator) : null;
        final String courseId = courseData.getCourse().getId();
        getHierarchyCall = courseApi.getCourseStructureWithoutStale(courseId);
        getHierarchyCall.enqueue(new CourseAPI.GetCourseStructureCallback(getActivity(), courseId,
                progressCallback, errorNotification, null, this) {
            @Override
            protected void onResponse(@NonNull final CourseComponent courseComponent) {
                courseComponentId = courseComponent.getId();
                courseManager.addCourseDataInAppLevelCache(courseId, courseComponent);
                loadData(courseComponent);
            }

            @Override
            protected void onFinish() {
                if (!EventBus.getDefault().isRegistered(NewCourseOutlineFragment.this)) {
                    EventBus.getDefault().registerSticky(NewCourseOutlineFragment.this);
                }
            }
        });
    }

    private void initListView(@NonNull View view) {
        listView = (ListView) view.findViewById(R.id.outline_list);
        initAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (deleteMode != null) {
                    deleteMode.finish();
                }
                listView.clearChoices();
                final CourseComponent component = adapter.getItem(position).component;
                if (component.isContainer()) {
                    environment.getRouter().showCourseContainerOutline(NewCourseOutlineFragment.this,
                            REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, component.getId(), null, isVideoMode);
                } else {
                    environment.getRouter().showCourseUnitDetail(NewCourseOutlineFragment.this,
                            REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, component.getId(), isVideoMode);
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (((IconImageView) view.findViewById(R.id.bulk_download)).getIcon() == FontAwesomeIcons.fa_check) {
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
            adapter = new NewCourseOutlineAdapter(getActivity(), this, courseData, environment,
                    new NewCourseOutlineAdapter.DownloadListener() {
                        @Override
                        public void download(List<? extends HasDownloadEntry> models) {
                            downloadManager.downloadVideos(models, getActivity(), NewCourseOutlineFragment.this);
                        }

                        @Override
                        public void download(DownloadEntry videoData) {
                            downloadManager.downloadVideo(videoData, getActivity(), NewCourseOutlineFragment.this);
                        }

                        @Override
                        public void viewDownloadsStatus() {
                            environment.getRouter().showDownloads(getActivity());
                        }
                    }, isVideoMode, isOnCourseOutline);
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

                    final NewCourseOutlineAdapter.SectionRow rowItem = adapter.getItem(checkedItemPosition);
                    final List<CourseComponent> videos = rowItem.component.getVideos(true);
                    final int totalVideos = videos.size();

                    if (isOnCourseOutline) {
                        environment.getAnalyticsRegistry().trackSubsectionVideosDelete(
                                courseData.getCourse().getId(), rowItem.component.getId());
                    } else {
                        environment.getAnalyticsRegistry().trackUnitVideoDelete(
                                courseData.getCourse().getId(), rowItem.component.getId());
                    }

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
     */
    private void loadData(@NonNull CourseComponent courseComponent) {
        if (courseData == null)
            return;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().registerSticky(this);
        }
        if (!isOnCourseOutline) {
            // We only need to set the title of Course Outline screen, where we show a subsection's units
            getActivity().setTitle(courseComponent.getDisplayName());
        }
        adapter.setData(courseComponent);
        if (adapter.hasCourseData()) {
            errorNotification.hideError();
        } else {
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

    @Override
    public void onRevisit() {
        super.onRevisit();
        fetchLastAccessed();
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
        if (courseComponentId != null)
            bundle.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId);
        outState.putBundle(Router.EXTRA_BUNDLE, bundle);
        outState.putBoolean(Router.EXTRA_IS_VIDEOS_MODE, isVideoMode);
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
                                            NewCourseOutlineFragment.this,
                                            REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData,
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
        // TODO: Remove this log, its just for performance testing purpose
        logger.debug("PERFORMANCE: Download COMPLETED");
        adapter.notifyDataSetChanged();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DownloadedVideoDeletedEvent e) {
        // TODO: Remove this log, its just for performance testing purpose
        logger.debug("PERFORMANCE: Download DELETED");
        adapter.notifyDataSetChanged();
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
                                    environment.getRouter().showCourseContainerOutline(
                                            getActivity(), courseData, finalLastAccessComponent.getId());
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
    }

    @Override
    public void onDownloadFailedToStart() {
        reloadList();
    }

    @Override
    public void showProgressDialog(int numDownloads) {
    }

    @Override
    public void updateListUI() {
        reloadList();
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
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (getHierarchyCall != null) {
            getHierarchyCall.cancel();
            getHierarchyCall = null;
        }
    }
}
