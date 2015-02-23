package org.edx.mobile.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.base.CourseDetailBaseFragment;
import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.task.GetAnnouncementTask;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.adapters.AnnouncementAdapter;

import java.util.List;

public class CourseAnnouncementFragment extends CourseDetailBaseFragment {

    private AnnouncementAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (!(NetworkUtil.isConnected(getActivity()))) {
            AppConstants.offline_flag = true;
        }else{
            AppConstants.offline_flag = false;
        }

        View view = inflater.inflate(R.layout.fragment_announcement, container,
                false);

        ListView announcementList = (ListView) view
                .findViewById(R.id.announcement_list);
        adapter = new AnnouncementAdapter(getActivity()) {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                // nothing to do here
            }
        };
        announcementList.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            final Bundle bundle = getArguments();
            EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) bundle
                    .getSerializable(BaseFragmentActivity.EXTRA_ENROLLMENT);
            if(courseData!=null){
                loadData(courseData);
            }

        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() instanceof BaseFragmentActivity) {
            ((BaseFragmentActivity)getActivity()).setApplyPrevTransitionOnRestart(true);        
        }
    }

    private void loadData(EnrolledCoursesResponse enrollment) {
        GetAnnouncementTask task = new GetAnnouncementTask(getActivity()) {

            @Override
            public void onException(Exception ex) {
                showEmptyAnnouncementMessage();
            }

            @Override
            public void onFinish(List<AnnouncementsModel> model) {
                try {
                    if(model!=null&& model.size()>0){
                        hideEmptyAnnouncementMessage();
                        adapter.clear();
                        for (AnnouncementsModel m : model) {
                            adapter.add(m);
                        }
                        adapter.notifyDataSetChanged();
                    }else{
                        showEmptyAnnouncementMessage();
                    }
                } catch (Exception ex) {
                    logger.error(ex);
                    showEmptyAnnouncementMessage();
                }
            }
        };
        ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.api_spinner);
        task.setProgressDialog(progressBar);
        task.execute(enrollment);

        try{
            segIO.screenViewsTracking(enrollment.getCourse().getName() 
                    + " - Announcements");
        }catch(Exception e){
            logger.error(e);
        }
    }

    public void showEmptyAnnouncementMessage(){
        try{
            if(adapter!=null){
                adapter.clear();
                adapter.notifyDataSetChanged();
            }
            if(getView()!=null){
                getView().findViewById(R.id.no_announcement_tv).setVisibility(View.VISIBLE);
            }
        }catch(Exception e){
            logger.error(e);
        }

    }

    private void hideEmptyAnnouncementMessage(){
        try{
            if(getView()!=null){
                getView().findViewById(R.id.no_announcement_tv).setVisibility(View.GONE);
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

}
