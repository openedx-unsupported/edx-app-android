package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.base.MyVideosBaseFragment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.ISequential;
import org.edx.mobile.model.IUnit;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.third_party.view.PinnedSectionListView;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.adapters.CourseSequentialAdapter;
import org.edx.mobile.view.common.TaskProcessCallback;

public class CourseSequentialOutlineFragment extends MyVideosBaseFragment {

    protected final Logger logger = new Logger(getClass().getName());
    static public String TAG = CourseSequentialOutlineFragment.class.getCanonicalName();

    private CourseSequentialAdapter adapter;
    private PinnedSectionListView listView;
    private TaskProcessCallback taskProcessCallback;
    private ISequential sequentialData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("SetJavaScriptEnabled")
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
        restore( savedInstanceState );

        try {
            if ( sequentialData == null ) {
                final Bundle bundle = getArguments();
                courseData = (EnrolledCoursesResponse) bundle
                    .getSerializable(Router.EXTRA_COURSE_DATA);
                sequentialData = (ISequential) bundle.getSerializable(Router.EXTRA_SEQUENTIAL);
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
        if ( sequentialData == null )
            return;

        adapter.setData(sequentialData);
        if(adapter.getCount()==0){
            view.findViewById(R.id.no_chapter_tv).setVisibility(View.VISIBLE);
        }
        if (adapter.getCount() == 0) {
            //TODO - if we have startDate , showCourseNotStartedMessage
            //otherwise , show empty view
        }
    }


    private void initializeAdapter(){
        if (adapter == null) {
            // creating adapter just once
            adapter = new CourseSequentialAdapter(getActivity()) {
                @Override
                public void rowClicked(SectionRow row) {
                    try {
                        Router.getInstance().showCourseUnitDetail(getActivity(),courseData,
                            course, sequentialData, (IUnit)row.component);
                    } catch (Exception ex) {
                        logger.error(ex);
                    }
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
    public void reloadList(){

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if ( sequentialData != null)
            outState.putSerializable(Router.EXTRA_SEQUENTIAL, sequentialData);
        super.onSaveInstanceState(outState);
    }

    protected void restore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
             sequentialData = (ISequential) savedInstanceState.getSerializable(Router.EXTRA_SEQUENTIAL);
        }
        super.restore(savedInstanceState);
    }
}
