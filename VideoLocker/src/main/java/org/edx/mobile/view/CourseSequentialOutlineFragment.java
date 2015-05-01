package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.ISequential;
import org.edx.mobile.model.IUnit;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.adapters.CourseSequentialAdapter;
import org.edx.mobile.view.common.TaskProcessCallback;
import org.edx.mobile.third_party.view.PinnedSectionListView;

import java.util.ArrayList;

public class CourseSequentialOutlineFragment extends Fragment {

    protected final Logger logger = new Logger(getClass().getName());
    static public String TAG = CourseHandoutFragment.class.getCanonicalName();

    private CourseSequentialAdapter adapter;
    private PinnedSectionListView listView;
    private TaskProcessCallback taskProcessCallback;
    private ISequential sequentialData;
    private EnrolledCoursesResponse courseData;

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
//Initialize the adapter
        initializeAdapter();

        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        try {
            //TODO - should we get data from callback at activity level?
            final Bundle bundle = getArguments();
            courseData = (EnrolledCoursesResponse) bundle
                    .getSerializable(Router.EXTRA_COURSE_OUTLINE);
            sequentialData = (ISequential) bundle.getSerializable(Router.EXTRA_SEQUENTIAL);
            CourseManager.getSharedInstance().setSequentialInView(sequentialData);

            if ( courseData == null || sequentialData == null)
                return;
          //  CourseManager.fromEnrollment(courseData.)
            loadData(getView());

        } catch (Exception ex) {
            logger.error(ex);
        }
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
                    // handle click
                    try {
                        Router.getInstance().showCourseUnitDetail(getActivity(),courseData, (IUnit)row.component);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<SectionEntry> saveEntries = new ArrayList<SectionEntry>();
        if(adapter!=null){
            //FIXME - we need to save data into the outState
        }
    }
}
