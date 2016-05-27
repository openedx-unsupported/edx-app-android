package org.edx.mobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.edx.mobile.R;
import org.edx.mobile.base.MyVideosBaseFragment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.task.GetAllDownloadedVideosTask;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.view.adapters.MyAllVideoCourseAdapter;

import java.util.List;

public class MyAllVideosFragment extends MyVideosBaseFragment {
    private MyAllVideoCourseAdapter myCoursesAdaptor;
    protected final Logger logger = new Logger(getClass().getName());
    private GetAllDownloadedVideosTask getAllDownloadedVideosTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        environment.getSegment().trackScreenView(ISegment.Screens.MY_VIDEOS_ALL);
    }

    @Override
    public void reloadList() {
        addMyAllVideosData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view       =   inflater.inflate(R.layout.fragment_my_all_videos, container, false);

        ListView myCourseList = (ListView) view.findViewById(R.id.my_video_course_list);
        myCourseList.setEmptyView(view.findViewById(R.id.empty_list_view));

        myCoursesAdaptor = new MyAllVideoCourseAdapter(getActivity(), environment) {
            @Override
            public void onItemClicked(EnrolledCoursesResponse model) {
                AppConstants.myVideosDeleteMode = false;
                
                Intent videoIntent = new Intent(getActivity(), VideoListActivity.class);
                videoIntent.putExtra(Router.EXTRA_ENROLLMENT, model);
                videoIntent.putExtra("FromMyVideos", true);
                startActivity(videoIntent);
            }
        };

        addMyAllVideosData();
        myCourseList.setAdapter(myCoursesAdaptor);
        myCourseList.setOnItemClickListener(myCoursesAdaptor);
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        addMyAllVideosData();
        myCoursesAdaptor.notifyDataSetChanged();
    }
    
    @Override
    public void onStop() {
        super.onStop();
        if (getAllDownloadedVideosTask != null) {
            getAllDownloadedVideosTask.cancel(true);
            getAllDownloadedVideosTask = null;
        }
    }

    private void addMyAllVideosData() {
        if (myCoursesAdaptor != null) {
            myCoursesAdaptor.clear();

            if (getAllDownloadedVideosTask != null) {
                getAllDownloadedVideosTask.cancel(true);
            } else {
                getAllDownloadedVideosTask = new GetAllDownloadedVideosTask(getActivity()) {

                    @Override
                    protected void onSuccess(List<EnrolledCoursesResponse> enrolledCoursesResponses) throws Exception {
                        super.onSuccess(enrolledCoursesResponses);
                        if (enrolledCoursesResponses != null) {
                            for (EnrolledCoursesResponse m : enrolledCoursesResponses) {
                                if (m.isIs_active()) {
                                    myCoursesAdaptor.add(m);
                                }
                            }
                        }
                    }
                };
            }
            getAllDownloadedVideosTask.execute();
        }
    }
}