package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
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
import android.widget.TextView;

import com.google.inject.Inject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconImageView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.BlockPath;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.storage.DownloadCompletedEvent;
import org.edx.mobile.module.storage.DownloadedVideoDeletedEvent;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.services.VideoDownloadHelper;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.adapters.CourseOutlineAdapter;
import org.edx.mobile.view.common.TaskProcessCallback;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 *  @deprecated As of release v2.13, see {@link NewCourseOutlineFragment} as an alternate.
 */
@Deprecated
public class CourseOutlineFragment extends BaseFragment {

    protected final Logger logger = new Logger(getClass().getName());
    static public String TAG = CourseOutlineFragment.class.getCanonicalName();
    static final int REQUEST_SHOW_COURSE_UNIT_DETAIL = 0;
    private static final int AUTOSCROLL_DELAY_MS = 500;
    private static final int SNACKBAR_SHOWTIME_MS = 5000;

    private CourseOutlineAdapter adapter;
    private ListView listView;
    private TaskProcessCallback taskProcessCallback;
    private EnrolledCoursesResponse courseData;
    private String courseComponentId;
    private boolean isVideoMode;
    private boolean isOnCourseOutline;
    private ActionMode deleteMode;

    @Inject
    CourseManager courseManager;

    @Inject
    VideoDownloadHelper downloadManager;

    @Inject
    protected IEdxEnvironment environment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        courseData = (EnrolledCoursesResponse) bundle.getSerializable(Router.EXTRA_COURSE_DATA);
        courseComponentId = bundle.getString(Router.EXTRA_COURSE_COMPONENT_ID);
        isVideoMode = bundle.getBoolean(Router.EXTRA_IS_VIDEOS_MODE);
        isOnCourseOutline = bundle.getBoolean(Router.EXTRA_IS_ON_COURSE_OUTLINE);

        View view = inflater.inflate(R.layout.fragment_course_outline, container, false);
        listView = (ListView) view.findViewById(R.id.outline_list);
        initializeAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (deleteMode != null) {
                    deleteMode.finish();
                }
                listView.clearChoices();
                CourseOutlineAdapter.SectionRow row = adapter.getItem(position);
                CourseComponent comp = row.component;
                if (comp.isContainer()) {
                    environment.getRouter().showCourseContainerOutline(CourseOutlineFragment.this,
                            REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, comp.getId(), null, isVideoMode);
                } else {
                    environment.getRouter().showCourseUnitDetail(CourseOutlineFragment.this,
                            REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, comp.getId(), isVideoMode);
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

        return view;
    }

    /**
     * Callback to handle the deletion of videos using the Contextual Action Bar.
     */
    private ActionMode.Callback deleteModelCallback = new ActionMode.Callback() {
        // Called when the action mode is created; startActionMode/startSupportActionMode was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        restore(savedInstanceState);
        loadData(getView());
        updateRowSelection(getArguments().getString(Router.EXTRA_LAST_ACCESSED_ID));
    }

    public void setTaskProcessCallback(TaskProcessCallback callback) {
        this.taskProcessCallback = callback;
    }

    protected CourseComponent getCourseComponent() {
        return courseManager.getComponentById(courseData.getCourse().getId(), courseComponentId);
    }

    //Loading data to the Adapter
    private void loadData(final View view) {
        if (courseData == null)
            return;
        CourseComponent courseComponent = getCourseComponent();
        adapter.setData(courseComponent);
        updateMessageView(view);
    }

    public void updateMessageView(View view) {
        if (view == null)
            view = getView();
        if (view == null)
            return;
        TextView messageView = (TextView) view.findViewById(R.id.no_chapter_tv);
        if (adapter.getCount() == 0) {
            messageView.setVisibility(View.VISIBLE);
            messageView.setText(isVideoMode ? R.string.no_videos_text : R.string.no_chapter_text);
        } else {
            messageView.setVisibility(View.GONE);
        }
    }

    private void initializeAdapter() {
        if (adapter == null) {
            // creating adapter just once
            adapter = new CourseOutlineAdapter(getActivity(), environment.getConfig(),
                    environment.getDatabase(), environment.getStorage(),
                    new CourseOutlineAdapter.DownloadListener() {
                        @Override
                        public void download(List<? extends HasDownloadEntry> models) {
                            CourseOutlineActivity activity = (CourseOutlineActivity) getActivity();
                            if (NetworkUtil.verifyDownloadPossible(activity)) {
                                downloadManager.downloadVideos(models, getActivity(),
                                        (VideoDownloadHelper.DownloadManagerCallback) getActivity());
                            }
                        }

                        @Override
                        public void download(DownloadEntry videoData) {
                            CourseOutlineActivity activity = (CourseOutlineActivity) getActivity();
                            if (NetworkUtil.verifyDownloadPossible(activity)) {
                                downloadManager.downloadVideo(videoData, activity, activity);
                            }
                        }

                        @Override
                        public void viewDownloadsStatus() {
                            environment.getRouter().showDownloads(getActivity());
                        }
                    }, isVideoMode);
        }
    }

    private void restore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            courseData = (EnrolledCoursesResponse) savedInstanceState.getSerializable(Router.EXTRA_COURSE_DATA);
            courseComponentId = (String) savedInstanceState.getString(Router.EXTRA_COURSE_COMPONENT_ID);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (courseData != null)
            outState.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
        if (courseComponentId != null)
            outState.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId);
    }

    public void reloadList() {
        if (adapter != null) {
            adapter.reloadData();
        }
    }

    private void updateRowSelection(String lastAccessedId) {
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
                        CourseComponent outlineComp = courseManager.getComponentById(
                                courseData.getCourse().getId(), courseComponentId);
                        String leafCompId = (String) data.getSerializableExtra(Router.EXTRA_COURSE_COMPONENT_ID);
                        CourseComponent leafComp = courseManager.getComponentById(
                                courseData.getCourse().getId(), leafCompId);
                        BlockPath outlinePath = outlineComp.getPath();
                        BlockPath leafPath = leafComp.getPath();
                        int outlinePathSize = outlinePath.getPath().size();
                        if (!outlineComp.equals(leafPath.get(outlinePathSize - 1))) {
                            getActivity().setResult(Activity.RESULT_OK, data);
                            getActivity().finish();
                        } else {
                            int leafPathSize = leafPath.getPath().size();
                            if (outlinePathSize == leafPathSize - 2) {
                                updateRowSelection(leafCompId);
                            } else {
                                for (int i = outlinePathSize + 1; i < leafPathSize - 1; i += 2) {
                                    CourseComponent nextComp = leafPath.get(i);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DownloadCompletedEvent e) {
        adapter.notifyDataSetChanged();
    }

    @SuppressWarnings("unused")
    public void onEvent(DownloadedVideoDeletedEvent e) {
        adapter.notifyDataSetChanged();
    }
}
