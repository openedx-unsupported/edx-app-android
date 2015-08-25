package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.MyVideosBaseFragment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.services.VideoDownloadHelper;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.adapters.CourseOutlineAdapter;
import org.edx.mobile.view.common.TaskProcessCallback;
import org.edx.mobile.view.custom.ETextView;

import java.util.ArrayList;
import java.util.List;

import static org.edx.mobile.view.CourseOutlineActivity.REQUEST_SHOW_COURSE_UNIT_DETAIL;

public class CourseOutlineFragment extends MyVideosBaseFragment {

    protected final Logger logger = new Logger(getClass().getName());
    static public String TAG = CourseOutlineFragment.class.getCanonicalName();

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_outline, container,
                false);
        listView = (ListView)view.findViewById(R.id.outline_list);
        initializeAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CourseOutlineAdapter.SectionRow row = adapter.getItem(position);
                CourseComponent comp = row.component;
                if ( comp.isContainer() ){
                    environment.getRouter().showCourseContainerOutline(getActivity(),
                            REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, comp.getId());
                } else {
                    environment.getRouter().showCourseUnitDetail(getActivity(),
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
        try {
            if( courseData == null ) {
                final Bundle bundle = getArguments();
                courseData = (EnrolledCoursesResponse) bundle.getSerializable(Router.EXTRA_ENROLLMENT);
                courseComponentId = (String) bundle.getString(Router.EXTRA_COURSE_COMPONENT_ID);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        loadData(getView());
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
        ETextView messageView = (ETextView) view.findViewById(R.id.no_chapter_tv);
        if(adapter.getCount()==0){
            messageView.setVisibility(View.VISIBLE);
            if ( adapter.hasFilteredUnits() ){
                messageView.setText(R.string.assessment_empty_video_info);
            } else {
                messageView.setText(R.string.no_chapter_text);
            }
        }else{
            messageView.setVisibility(View.GONE);
        }
    }

    private void initializeAdapter(){
        if (adapter == null) {
            // creating adapter just once
            adapter = new CourseOutlineAdapter(getActivity(), environment.getDatabase(), environment.getStorage(), new CourseOutlineAdapter.DownloadListener() {
                @Override
                public void download(List<HasDownloadEntry> models) {
                    downloadManager.downloadVideos(
                            (List) models, (FragmentActivity) getActivity(), (VideoDownloadHelper.DownloadManagerCallback) getActivity());

                }

                @Override
                public void download(DownloadEntry videoData) {
                    downloadManager.downloadVideo(
                            videoData, (FragmentActivity) getActivity(), (VideoDownloadHelper.DownloadManagerCallback) getActivity());
                }
            });
        }

        if (!(NetworkUtil.isConnected(getActivity()))) {
            AppConstants.offline_flag = true;
        } else {
            AppConstants.offline_flag = false;
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
}
