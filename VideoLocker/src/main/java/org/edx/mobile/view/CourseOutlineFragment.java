package org.edx.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.base.MyVideosBaseFragment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.course.BlockPath;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.storage.DownloadCompletedEvent;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.services.VideoDownloadHelper;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.adapters.CourseOutlineAdapter;
import org.edx.mobile.view.common.TaskProcessCallback;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class CourseOutlineFragment extends MyVideosBaseFragment {

    protected final Logger logger = new Logger(getClass().getName());
    static public String TAG = CourseOutlineFragment.class.getCanonicalName();
    static final int REQUEST_SHOW_COURSE_UNIT_DETAIL = 0;
    private static final int AUTOSCROLL_DELAY_MS = 500;

    private CourseOutlineAdapter adapter;
    private ListView listView;
    private TaskProcessCallback taskProcessCallback;

    @Inject
    CourseManager courseManager;

    @Inject
    VideoDownloadHelper downloadManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
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
                            REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, comp.getId(), null);
                } else {
                    environment.getRouter().showCourseUnitDetail(CourseOutlineFragment.this,
                            REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, comp.getId());
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        restore(savedInstanceState);
        final Bundle bundle = getArguments();
        if( courseData == null ) {
            courseData = (EnrolledCoursesResponse) bundle.getSerializable(Router.EXTRA_ENROLLMENT);
            courseComponentId = bundle.getString(Router.EXTRA_COURSE_COMPONENT_ID);
        }
        loadData(getView());

        updateRowSelection(bundle.getString(Router.EXTRA_LAST_ACCESSED_ID));
    }

    @Override
    public void onResume(){
        super.onResume();
        //check if mode is changed
        if ( adapter != null ){
            boolean listRebuilt = adapter.checkModeChange();
            if ( !listRebuilt ){
                adapter.notifyDataSetChanged();
            }
        }
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
    public void updateMessageView(View view){
        if (view == null )
            view = getView();
        if ( view == null )
            return;
        TextView messageView = (TextView) view.findViewById(R.id.no_chapter_tv);
        if(adapter.getCount()==0){
            messageView.setVisibility(View.VISIBLE);
            if ( adapter.hasFilteredUnits() ){
                Context context = getActivity();
                Drawable modeSwitcherDrawable =
                        new IconDrawable(context, FontAwesomeIcons.fa_list)
                        .colorRes(context, R.color.edx_grayscale_neutral_light)
                        .sizeRes(context, R.dimen.content_unavailable_error_icon_size);
                messageView.setCompoundDrawablesWithIntrinsicBounds(
                        null, modeSwitcherDrawable, null, null);
                Resources resources = getResources();
                messageView.setText(ResourceUtil.getFormattedString(resources,
                        R.string.assessment_empty_video_info, "mode_switcher",
                        '{' + FontAwesomeIcons.fa_list.key() + " baseline}"));
                messageView.setContentDescription(ResourceUtil.getFormattedString(resources,
                        R.string.assessment_empty_video_info, "mode_switcher",
                        getText(R.string.course_change_mode)));
            } else {
                messageView.setCompoundDrawables(null, null, null, null);
                messageView.setText(R.string.no_chapter_text);
                messageView.setContentDescription(null);
            }
        }else{
            messageView.setVisibility(View.GONE);
        }
    }

    private void initializeAdapter() {
        if (adapter == null) {
            // creating adapter just once
            adapter = new CourseOutlineAdapter(getActivity(), environment.getDatabase(),
                    environment.getStorage(), new CourseOutlineAdapter.DownloadListener() {
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
            });
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<SectionEntry> saveEntries = new ArrayList<SectionEntry>();
        if(adapter!=null){
            //FIXME - we need to save data into the outState
        }
    }

    @Override
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
                                    environment.getRouter().showCourseContainerOutline(CourseOutlineFragment.this,
                                            REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, nextComp.getId(), leafCompId);
                                }
                                getActivity().overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
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
}
