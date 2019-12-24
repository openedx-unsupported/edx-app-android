package org.humana.mobile.view;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconImageView;

import org.humana.mobile.R;
import org.humana.mobile.base.BaseFragment;
import org.humana.mobile.base.BaseFragmentActivity;
import org.humana.mobile.core.IEdxEnvironment;
import org.humana.mobile.course.CourseAPI;
import org.humana.mobile.event.CourseDashboardRefreshEvent;
import org.humana.mobile.event.NetworkConnectivityChangeEvent;
import org.humana.mobile.exception.CourseContentNotValidException;
import org.humana.mobile.http.notifications.FullScreenErrorNotification;
import org.humana.mobile.interfaces.RefreshListener;
import org.humana.mobile.loader.AsyncTaskResult;
import org.humana.mobile.loader.CourseOutlineAsyncLoader;
import org.humana.mobile.logger.Logger;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.model.course.BlockPath;
import org.humana.mobile.model.course.BlockType;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.model.course.CourseStructureV1Model;
import org.humana.mobile.model.course.HasDownloadEntry;
import org.humana.mobile.model.course.VideoBlockModel;
import org.humana.mobile.model.db.DownloadEntry;
import org.humana.mobile.module.analytics.Analytics;
import org.humana.mobile.module.prefs.LoginPrefs;
import org.humana.mobile.module.storage.DownloadCompletedEvent;
import org.humana.mobile.module.storage.DownloadedVideoDeletedEvent;
import org.humana.mobile.module.storage.IStorage;
import org.humana.mobile.services.CourseManager;
import org.humana.mobile.services.LastAccessManager;
import org.humana.mobile.services.VideoDownloadHelper;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.analytics.Analytic;
import org.humana.mobile.tta.analytics.analytics_enums.Action;
import org.humana.mobile.tta.analytics.analytics_enums.Source;
import org.humana.mobile.tta.data.DataManager;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.scorm.ScormBlockModel;
import org.humana.mobile.tta.scorm.ScormManager;
import org.humana.mobile.tta.ui.custom.DropDownFilterView;
import org.humana.mobile.tta.ui.programs.pendingUnits.PendingUnitsListActivity;
import org.humana.mobile.tta.ui.programs.pendingUnits.viewModel.PendingUnitsListViewModel;
import org.humana.mobile.tta.utils.ActivityUtil;
import org.humana.mobile.tta.utils.MXPDFManager;
import org.humana.mobile.util.NetworkUtil;
import org.humana.mobile.util.PermissionsUtil;
import org.humana.mobile.util.UiUtil;
import org.humana.mobile.view.adapters.CourseOutlineAdapter;
import org.humana.mobile.view.common.TaskProgressCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import retrofit2.Call;

public class CourseOutlineFragment extends OfflineSupportBaseFragment
        implements LastAccessManager.LastAccessManagerCallback, RefreshListener,
        VideoDownloadHelper.DownloadManagerCallback,
        LoaderManager.LoaderCallbacks<AsyncTaskResult<CourseComponent>>, BaseFragment.PermissionListener {
    private final Logger logger = new Logger(getClass().getName());
    private static final int REQUEST_SHOW_COURSE_UNIT_DETAIL = 0;
    private static final int AUTOSCROLL_DELAY_MS = 500;
    private static final int SNACKBAR_SHOWTIME_MS = 5000;
    private DataManager mDataManager;

    private CourseOutlineAdapter adapter;
    private ListView listView;
    private EnrolledCoursesResponse courseData;
    private String courseComponentId;
    private boolean isVideoMode;
    private boolean isOnCourseOutline;
    private boolean isFetchingLastAccessed;
    // Flag to differentiate between single or multiple video download
    private boolean isSingleVideoDownload;
    private ActionMode deleteMode;
    private DownloadEntry downloadEntry;
    private List<? extends HasDownloadEntry> downloadEntries;
    private SwipeRefreshLayout swipeContainer;
    public FloatingActionButton  mfab;
    public float unitRating = 0;

    public ObservableField<String> userName = new ObservableField<String>();

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

    private String unidId, unitType, unitTitle, unitDesc;
    private LinearLayout linearLayout;

    @Inject
    Analytic aHelper;

    @Inject
    public LoginPrefs loginPrefs;

    @Inject
    @NonNull
    ScormManager scormManager;

    private int requestCode;

    private String respondentList;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_course_outline, container, false);
        listView = (ListView) view.findViewById(R.id.outline_list);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        errorNotification = new FullScreenErrorNotification(swipeContainer);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        linearLayout = view.findViewById(R.id.ll_approval);
        mfab = view.findViewById(R.id.fab);



        final Bundle bundle;
        {
            if (savedInstanceState != null) {
                bundle = savedInstanceState;
                unidId = bundle.getString(Router.EXTRA_Unit_id);
                unitType = bundle.getString(Router.EXTRA_Unit_TYPE);
                unitTitle = bundle.getString(Router.EXTRA_TITLE);
                unitDesc = bundle.getString(Router.EXTRA_UNIT_DESC);

            } else {
                bundle = getArguments();
            }
        }

        if (!Constants.UNIT_ID.equals("")){
            linearLayout.setVisibility(View.VISIBLE);
        }



        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Hide the progress bar as swipe layout has its own progress indicator
                loadingIndicator.setVisibility(View.GONE);
                errorNotification.hideError();
                getCourseComponentFromServer(false);
            }
        });
        UiUtil.setSwipeRefreshLayoutColors(swipeContainer);

        restore(bundle);
        initListView(view);
        fetchCourseComponent();

//        getUserUnitResponse();

        mfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                approveReturn(Constants.UNIT_ID,unitType,unitTitle, unitDesc);
            }
        });

        askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST);


        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        permissionListener = this;
        updateRowSelection(getArguments().getString(Router.EXTRA_LAST_ACCESSED_ID));
    }

    private void restore(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            final Bundle bundle = savedInstanceState.getBundle(Router.EXTRA_BUNDLE);
            courseData = (EnrolledCoursesResponse) bundle.getSerializable(Router.EXTRA_COURSE_DATA);
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

    @Override
    public Loader<AsyncTaskResult<CourseComponent>> onCreateLoader(int id, Bundle args) {
        return new CourseOutlineAsyncLoader(getContext(), courseData.getCourse().getId());
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
        final TaskProgressCallback progressCallback = showProgress ?
                new TaskProgressCallback.ProgressViewController(loadingIndicator) : null;
        final String courseId = courseData.getCourse().getId();
        getHierarchyCall = courseApi.getCourseStructureWithoutStale(courseId);
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

    private void initListView(@NonNull View v) {
        initAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (deleteMode != null) {
                    deleteMode.finish();
                }
//                listView.clearChoices();
//                final CourseComponent component = adapter.getItem(position).component;
//                if (component.isContainer()) {
//                    environment.getRouter().showCourseContainerOutline(CourseOutlineFragment.this,
//                            REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, component.getId(), null, isVideoMode);
//                } else {
//                    environment.getRouter().showCourseUnitDetail(CourseOutlineFragment.this,
//                            REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, component.getId(), isVideoMode);
//                }

                listView.clearChoices();
                CourseOutlineAdapter.SectionRow row = adapter.getItem(position);
                CourseComponent comp = row.component;

                if (comp == null)
                    return;

                if(comp.getRoot().getChildren().size()==1) {
                    if (comp.getChildren().size() > 0) {
                        comp = (CourseComponent) comp.getChildren().get(0);
                    }
                }
                else
                {
                    if(adapter.selectedUnit==null)
                        adapter.selectedUnit=comp;
                }

                if (comp.isContainer()) {
                    Log.i("gfs", "container: " + comp.getId());
                    environment.getRouter().showCourseContainerOutline(CourseOutlineFragment.this,
                            REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, comp.getId(), null);

                    // for analytics update
                    aHelper.addMxAnalytics_db(loginPrefs.getUsername(),
                            adapter.selectedUnit.getDisplayName(), Action.ViewUnit,
                            adapter.selectedUnit.getRoot().getDisplayName(), Source.Mobile);

                }
                else
                {

                    if (comp.getType()== BlockType.SCORM || comp.getType()==BlockType.PDF)
                    {
                        adapter.selectedUnit = comp;

                        ////ToDo need to optimise here Arjun

                        if(comp.getType()==BlockType.PDF )//&& scormManager.hasPdf(comp.getId())
                        {
                            //anlaytic hit for scrom view
                            aHelper.addMxAnalytics_db(loginPrefs.getUsername()
                                    ,adapter.selectedUnit.getDisplayName() , Action.ViewUnit,
                                    adapter.selectedUnit.getRoot().getDisplayName() , Source.Mobile);

                            ScormBlockModel model =comp.getScorms().get(0);
                            switch (mDataManager.getScormStatus(model)){
                                case not_downloaded:
                                    mDataManager.downloadSingle(model, getActivity(),
                                            new VideoDownloadHelper.DownloadManagerCallback() {
                                                @Override
                                                public void onDownloadStarted(Long result) {
                                                    Log.d("--> Download State", "onDownloadStarted");
                                                    adapter.notifyDataSetChanged();

                                                }

                                                @Override
                                                public void onDownloadFailedToStart() {
                                                    Log.d("--> Download State", "onDownloadFailedToStart");
                                                }

                                                @Override
                                                public void showProgressDialog(int numDownloads) {
                                                    adapter.notifyDataSetChanged();
                                                    Log.d("--> Download State", "showProgressDialog");
//                        progressDialog.setProgress(numDownloads);
                                                }

                                                @Override
                                                public void updateListUI() {
                                                    Log.d("--> Download State", "updateListUI");

                                                }

                                                @Override
                                                public boolean showInfoMessage(String message) {
                                                    Log.d("--> Download State", "showInfoMessage");
                                                    return false;
                                                }
                                            });
                                    break;
                                case downloading:
                                    break;
                                    case downloaded:

                                    break;

                                case watched:
                                    break;
                                case watching:
                                    break;
                            }


                        }
                        else if(comp.getType()==BlockType.SCORM && scormManager.has(comp.getId()))
                        {
                            //anlaytic hit for scrom view
                            aHelper.addMxAnalytics_db(loginPrefs.getUsername()
                                    ,adapter.selectedUnit.getDisplayName() , Action.ViewUnit,
                                    adapter.selectedUnit.getRoot().getDisplayName() , Source.Mobile);

                            adapter.doShow(scormManager.get(comp.getId()),adapter.selectedUnit);
                        }
                        else
                        {
                            if (!NetworkUtil.isConnected(getActivity()))
                            {
                                Toast.makeText(getActivity(), "Please connect to internet. And try again.", Toast.LENGTH_SHORT).show();
//                                adapter.reloadData();
                                return;
                            }
//
//                            CourseOutlineAdapter.ViewHolder viewHolder = (CourseOutlineAdapter.ViewHolder) view.getItemViewHolder();
//                            adapter.doDownload(comp, viewHolder);
                        }
                        return;

/*
                        if (scormManager.has(comp.getId())) {
                            //anlaytic hit for scrom view
                                aHelper.updateAnalytics(getActivity(), aHelper.getAnalyticParams(loginPrefs.getUsername()
                                        ,adapter.selectedUnit.getDisplayName() , Action.ViewUnit,
                                        adapter.selectedUnit.getRoot().getDisplayName() , Source.Mobile));

                            if(comp.getType()==BlockType.PDF && scormManager.hasPdf(comp.getId()))
                            {
                                pdfManager manager=new pdfManager();
                                manager.viewPDF(getActivity(),scormManager.getPdf(comp.getId()));
                            }

                            else if(comp.getType()== BlockType.SCORM)
                            {
                                adapter.doShow(scormManager.get(comp.getId()));
                            }
                        } else {
                            CourseOutlineAdapter.ViewHolder viewHolder = (CourseOutlineAdapter.ViewHolder) view.getItemViewHolder();
                            adapter.doDownload(comp, viewHolder);
                        }
                        return;*/
                    }

                    // Log.i("gfs", "Unit Detail: " + comp.getId());
                    environment.getRouter().showCourseUnitDetail(CourseOutlineFragment.this,
                            REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, comp.getId(), unitType, unitTitle);

                    // for analytics update
                    if (comp.getType()==BlockType.PROBLEM) {
                        aHelper.addMxAnalytics_db(loginPrefs.getUsername(),
                                comp.getDisplayName(), Action.ViewQuestion,
                                comp.getRoot().getDisplayName(), Source.Mobile);
                    }
                    else
                    {
                        aHelper.addMxAnalytics_db(loginPrefs.getUsername()
                                ,comp.getDisplayName() , Action.ViewUnit,
                                comp.getRoot().getDisplayName() , Source.Mobile);
                    }
                }
            }

        });


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
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
            adapter = new CourseOutlineAdapter(getActivity(), scormManager,this, courseData, environment,
                    new CourseOutlineAdapter.DownloadListener() {
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
                    }, isVideoMode, isOnCourseOutline);
        }

    }

    @Override
    public void onPermissionGranted(String[] permissions, int requestCode) {
        switch (requestCode) {
            case PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST:
                if (isSingleVideoDownload) {
                    downloadManager.downloadVideo(downloadEntry, getActivity(), CourseOutlineFragment.this);
                } else {
                    downloadManager.downloadVideos(downloadEntries, -1, getActivity(), CourseOutlineFragment.this);
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


    @Override
    public void onResume() {
        super.onResume();

    }

    /**
     * Load data to the adapter
     */
    private void loadData(@NonNull CourseComponent courseComponent) {
        courseComponentId = courseComponent.getId();
        if (courseData == null || getActivity() == null)
            return;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().registerSticky(this);
        }
        if (!isOnCourseOutline) {
            // We only need to set the title of Course Outline screen, where we showLoading a subsection's units
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
        final BlockPath outlinePath;
        try {
            outlinePath = outlineComp.getPath();
        } catch (Exception e) {
            return false;
        }
        final int outlinePathSize = outlinePath.getPath().size();

        return outlinePathSize <= 1;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DownloadCompletedEvent e) {
        adapter.notifyDataSetChanged();
        if (e.getEntry().isDownloaded()){
            adapter.notifyItem(e.getEntry());
            Log.d("onEventMainThread", "file Downloaded");
        }
        //progress bar hide
        //show delete button on unit
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DownloadedVideoDeletedEvent e) {
        adapter.notifyDataSetChanged();
        //show download button on unit
        //progress bar hide
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
                    // to be the course root component itself, then we don't need to showLoading it
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
    }


    public void approveReturn(String unitId, String type, String title, String desc) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_approve_return_unit);
        Button btnApprove = (Button) dialog.findViewById(R.id.btn_approve);
        Button btnReturn = (Button) dialog.findViewById(R.id.btn_return);
        EditText etRemarks = dialog.findViewById(R.id.et_remarks);
        DropDownFilterView filterView = dialog.findViewById(R.id.filter_view);
//        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
        TextView mtv_rating = dialog.findViewById(R.id.tv_ratings);
//        EditText dialogText =  dialog.findViewById(R.id.et_period_name);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);

        List<DropDownFilterView.FilterItem> items = new ArrayList<>();

        List<String> filterItems = new ArrayList<>();
        filterItems.add("Ratings");
        filterItems.add("Poor");
        filterItems.add("Fair");
        filterItems.add("Good");
        filterItems.add("Very Good");
        filterItems.add("Excellent");
        for (String filterItem : filterItems) {
            items.add(new DropDownFilterView.FilterItem(filterItem, filterItem,
                    false, R.color.primary_cyan, R.drawable.t_background_tag_filled
            ));
        }
        filterView.setFilterItems(items);
        filterView.setOnFilterItemListener((v, item, position, prev) -> {
            switch (item.getName()) {
                case "Poor":
                    unitRating = 1;
                    break;
                case "Fair":
                    unitRating = 2;
                    break;
                case "Good":
                    unitRating = 3;
                    break;
                case "Very Good":
                    unitRating = 4;
                    break;
                case "Excellent":
                    unitRating = 5;
                    break;
            }
        });
        btnApprove.setOnClickListener(v -> {
            String remarks = etRemarks.getText().toString();
            approveUnits(unitId, remarks, (int) unitRating, type, title,desc);
            dialog.dismiss();
        });

        btnReturn.setOnClickListener(v -> {
            String remarks = etRemarks.getText().toString();

            rejectUnits(unitId, remarks, (int) unitRating, type, title, desc);
            dialog.dismiss();
        });
        dialog.setCancelable(true);
        dialog.show();

    }


    public void approveUnits(String unitId, String remarks, int rating, String unitType, String unitTitle, String desc) {

        mDataManager.approveUnit(unitId,
                Constants.USERNAME, remarks, rating, new OnResponseCallback<SuccessResponse>() {
                    @Override
                    public void onSuccess(SuccessResponse data) {
                        Toast.makeText(getActivity(),"Unit Approved", Toast.LENGTH_SHORT).show();
                        sendNotifications(unitTitle, unitType ,desc, "AprroveUnit",unitId,
                                mDataManager.getLoginPrefs().getProgramId(), userName.get());
                        getActivity().finish();

                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });
    }

    public void rejectUnits(String unitId, String remarks, int rating, String unitType, String unitTitle, String unitDesc) {
        mDataManager.rejectUnit(unitId,
                Constants.USERNAME, remarks, rating, new OnResponseCallback<SuccessResponse>() {
                    @Override
                    public void onSuccess(SuccessResponse data) {
                        Toast.makeText(getActivity(),"Unit Returned", Toast.LENGTH_SHORT).show();
                        sendNotifications(unitTitle,unitType ,unitDesc, "ReturnUnit",unitId,
                                mDataManager.getLoginPrefs().getProgramId(),userName.get());

                        getActivity().finish();

                    }

                    @Override
                    public void onFailure(Exception e) {
                    }
                });
    }
   /* public void getUserUnitResponse() {
        mDataManager.setSpecificSession("student",
                "Student", "mx_humana_lms/api/" +
                        mDataManager.getLoginPrefs().getProgramId()+"/masquerade",environment.getLoginPrefs().getLoginUserCookie(),
                new OnResponseCallback<SuccessResponse>() {
                    @Override
                    public void onSuccess(SuccessResponse response) {
                        if (response.getSuccess()){

                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                    }
                });
    }*/


    public void askForPermissions(String[] permissions, int requestCode) {
        this.requestCode = requestCode;
        if (getActivity() != null) {
            if (permissionListener != null && getGrantedPermissionsCount(permissions) == permissions.length) {
                permissionListener.onPermissionGranted(permissions, requestCode);
            } else {
                PermissionsUtil.requestPermissions(requestCode, permissions, this);
            }
        }
    }

    public int getGrantedPermissionsCount(String[] permissions) {
        int grantedPermissionsCount = 0;
        for (String permission : permissions) {
            if (PermissionsUtil.checkPermissions(permission, getActivity())) {
                grantedPermissionsCount++;
            }
        }

        return grantedPermissionsCount;
    }


    // Notifications for firebase

    private void sendNotifications(String title, String type, String desc, String action,
                                   String action_id, String action_parent_id, String respondent) {
        String unique_id = Settings.Secure.getString(getActivity().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        mDataManager.sendNotifications(title, type, desc, action, action_id, action_parent_id,
                respondent,unique_id,
                new OnResponseCallback<SuccessResponse>() {
                    @Override
                    public void onSuccess(SuccessResponse response) {
                        if (response.getSuccess()){
//                            Toast.makeText(getActivity(),"Notification sent..", Toast.LENGTH_SHORT).show();
                            Log.d("Notification", "Notification sent..");
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.d("Notification Failure..", e.getMessage());
                    }
                });
    }

}
