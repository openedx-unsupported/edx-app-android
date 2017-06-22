package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.BlockPath;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.storage.DownloadCompletedEvent;
import org.edx.mobile.module.storage.DownloadedVideoDeletedEvent;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.services.VideoDownloadHelper;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.adapters.CourseOutlineAdapter;
import org.edx.mobile.view.common.TaskProcessCallback;

import java.util.List;

import de.greenrobot.event.EventBus;

public class CourseOutlineFragment extends BaseFragment {

    protected final Logger logger = new Logger(getClass().getName());
    static public String TAG = CourseOutlineFragment.class.getCanonicalName();
    static final int REQUEST_SHOW_COURSE_UNIT_DETAIL = 0;
    private static final int AUTOSCROLL_DELAY_MS = 500;

    private CourseOutlineAdapter adapter;
    private ListView listView;
    private TaskProcessCallback taskProcessCallback;
    private EnrolledCoursesResponse courseData;
    private String courseComponentId;
    private boolean isVideoMode;

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

        View view = inflater.inflate(R.layout.fragment_course_outline, container, false);
        listView = (ListView)view.findViewById(R.id.outline_list);
        initializeAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        restore(savedInstanceState);
        loadData(getView());
        updateRowSelection(getArguments().getString(Router.EXTRA_LAST_ACCESSED_ID));
    }

    public void setTaskProcessCallback(TaskProcessCallback callback){
        this.taskProcessCallback = callback;
    }

    protected CourseComponent getCourseComponent(){
        return courseManager.getComponentById(courseData.getCourse().getId(), courseComponentId);
    }

    //Loading data to the Adapter
    private void loadData(final View view) {
        if ( courseData == null )
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
        if ( courseData != null)
            outState.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
        if ( courseComponentId != null )
            outState.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId);
    }

    public void reloadList(){
        if ( adapter != null ){
            adapter.reloadData();
        }
    }

    private void updateRowSelection(String lastAccessedId){
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
