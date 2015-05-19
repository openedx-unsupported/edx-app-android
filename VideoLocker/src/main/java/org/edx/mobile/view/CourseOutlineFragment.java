package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.base.MyVideosBaseFragment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.ICourse;
import org.edx.mobile.model.ISequential;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.services.DownloadManager;
import org.edx.mobile.task.GetCourseOutlineTask;
import org.edx.mobile.third_party.view.PinnedSectionListView;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.adapters.CourseOutlineAdapter;
import org.edx.mobile.view.common.TaskProcessCallback;

import java.util.ArrayList;
import java.util.List;

public class CourseOutlineFragment extends MyVideosBaseFragment {

    protected final Logger logger = new Logger(getClass().getName());
    static public String TAG = CourseOutlineFragment.class.getCanonicalName();

    private CourseOutlineAdapter adapter;
    private PinnedSectionListView listView;
    private GetCourseOutlineTask getHierarchyTask;
    private TaskProcessCallback taskProcessCallback;
    private boolean isTaskRunning;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_outline, container,
                false);
        listView = (PinnedSectionListView)view.findViewById(R.id.outline_list);
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
                courseData = (EnrolledCoursesResponse) bundle
                    .getSerializable(Router.EXTRA_COURSE_DATA);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        loadData(getView());
    }

    public void setTaskProcessCallback(TaskProcessCallback callback){
        this.taskProcessCallback = callback;
    }

    //Loading data to the Adapter
    private void loadData(final View view) {
        if ( courseData == null )
            return;

        if (isTaskRunning) {
            logger.debug("skipping a call to loadData, task is already running");

            getHierarchyTask.setTaskProcessCallback(taskProcessCallback);
            return;
        }

        getHierarchyTask = new GetCourseOutlineTask(getActivity()) {

            @Override
            public void onFinish(ICourse aCourse) {
                // display these chapters
                if (aCourse != null) {
                    logger.debug("Start displaying on UI "+ DateUtil.getCurrentTimeStamp());
                    course = aCourse;
                    adapter.setData(course);
                    if(adapter.getCount()==0){
                        view.findViewById(R.id.no_chapter_tv).setVisibility(View.VISIBLE);
                    }
                }

                if (adapter.getCount() == 0) {
                    //TODO - if we have startDate , showCourseNotStartedMessage
                    //otherwise , show empty view
                }

                logger.debug("Completed displaying data on UI "+ DateUtil.getCurrentTimeStamp());
                isTaskRunning = false;
            }

            @Override
            public void onException(Exception ex) {
                if(adapter.getCount()==0) {
                    //TODO - if we have startDate , showCourseNotStartedMessage
                    //otherwise , show empty view
                }
                isTaskRunning = false;
            }
        };


        getHierarchyTask.setTaskProcessCallback(taskProcessCallback);
        //Initializing task call
        logger.debug("Initializing Chapter Task" + DateUtil.getCurrentTimeStamp());
        isTaskRunning = true;
        getHierarchyTask.execute(courseData.getCourse().getId());
    }


    private void initializeAdapter(){
        if (adapter == null) {
            // creating adapter just once
            adapter = new CourseOutlineAdapter(getActivity(), db, storage) {

                @Override
                public void rowClicked(SectionRow row) {
                    // handle click
                    try {
                        Router.getInstance().showCourseSequentialDetail(getActivity(),
                            courseData, course, (ISequential) row.component);
                    } catch (Exception ex) {
                        logger.error(ex);
                    }
                }

                public void download(List<VideoResponseModel> models){
                    DownloadManager.getSharedInstance().downloadVideos(
                        models, (FragmentActivity)getActivity(), (DownloadManager.DownloadManagerCallback)getActivity());
                }

                public  void download(DownloadEntry videoData){

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
