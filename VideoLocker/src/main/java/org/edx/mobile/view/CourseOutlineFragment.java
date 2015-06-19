package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.edx.mobile.R;
import org.edx.mobile.base.MyVideosBaseFragment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.services.DownloadManager;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.adapters.CourseOutlineAdapter;
import org.edx.mobile.view.common.TaskProcessCallback;

import java.util.ArrayList;
import java.util.List;

public class CourseOutlineFragment extends MyVideosBaseFragment {

    protected final Logger logger = new Logger(getClass().getName());
    static public String TAG = CourseOutlineFragment.class.getCanonicalName();

    private CourseOutlineAdapter adapter;
    private ListView listView;
    private TaskProcessCallback taskProcessCallback;


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
            //we will refresh list only if there are some selection
            if ( !listRebuilt && adapter.getSelectedRow() != null ){
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void setTaskProcessCallback(TaskProcessCallback callback){
        this.taskProcessCallback = callback;
    }

    protected CourseComponent getCourseComponent(){
        return CourseManager.getSharedInstance().getComponentById(courseData.getCourse().getId(), courseComponentId);
    }

    //Loading data to the Adapter
    private void loadData(final View view) {
        if ( courseData == null )
            return;
        CourseComponent courseComponent = getCourseComponent();
        adapter.setData(courseComponent);
        if(adapter.getCount()==0){
            view.findViewById(R.id.no_chapter_tv).setVisibility(View.VISIBLE);
        }
    }

    private void initializeAdapter(){
        if (adapter == null) {
            // creating adapter just once
            adapter = new CourseOutlineAdapter(getActivity(), db, storage) {

                @Override
                public void rowClicked(SectionRow row) {
                    // handle click
                    try {
                        super.rowClicked(row);
                        CourseComponent comp = row.component;
                        if ( comp.isContainer() ){
                            Router.getInstance().showCourseContainerOutline(getActivity(), courseData, comp.getId());
                        } else {
                            Router.getInstance().showCourseUnitDetail(getActivity(), courseData, courseComponentId, comp);
                        }

                    } catch (Exception ex) {
                        logger.error(ex);
                    }
                }

                public void download(List<HasDownloadEntry> models){
                    DownloadManager.getSharedInstance().downloadVideos(
                        (List)models, (FragmentActivity)getActivity(), (DownloadManager.DownloadManagerCallback)getActivity());
                }

                public  void download(DownloadEntry videoData){
                    DownloadManager.getSharedInstance().downloadVideo(
                        videoData, (FragmentActivity)getActivity(), (DownloadManager.DownloadManagerCallback)getActivity());
                }
            };
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
