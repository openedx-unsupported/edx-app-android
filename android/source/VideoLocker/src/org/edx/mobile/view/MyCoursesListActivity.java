package org.edx.mobile.view;

import java.util.ArrayList;

import org.edx.mobile.R;
import org.edx.mobile.exception.AuthException;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.adapters.MyCourseAdapter;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.task.GetEnrolledCoursesTask;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.LogUtil;
import org.edx.mobile.view.custom.ETextView;
import org.edx.mobile.view.dialog.FindCoursesDialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MyCoursesListActivity extends BaseFragmentActivity {
    private View offlineBar;
    private MyCourseAdapter adapter;
    private LinearLayout offlinePanel;
    private SwipeRefreshLayout swipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        // configure slider layout. This should be called only once and
        // hence is shifted to onCreate() function
        configureDrawer();

        try{
            segIO.screenViewsTracking(getString(R.string.label_my_courses));
        }catch(Exception e){
            e.printStackTrace();
        }
        offlineBar = findViewById(R.id.offline_bar);
        offlinePanel = (LinearLayout) findViewById(R.id.offline_panel);

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        if (!(NetworkUtil.isConnected(this))) {
            showOfflinePanel();
            AppConstants.offline_flag = true;
            invalidateOptionsMenu();
            offlineBar.setVisibility(View.VISIBLE);
            swipeLayout.setEnabled(false);
        }else{
            hideOfflinePanel();
            AppConstants.offline_flag = false;
            invalidateOptionsMenu();
            offlineBar.setVisibility(View.GONE);
            swipeLayout.setEnabled(true);
        }

        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                R.color.grey_act_background , R.color.grey_act_background ,
                R.color.grey_act_background);

        ListView myCourseList = (ListView) findViewById(R.id.my_course_list);
        setupFooter(myCourseList);

        adapter = new MyCourseAdapter(this) {

            @Override
            public void onItemClicked(EnrolledCoursesResponse model) {
                try {
                    Bundle courseBundle = new Bundle();
                    courseBundle.putSerializable("enrollment", model);
                    courseBundle.putBoolean("announcemnts", false);

                    Intent courseDetail = new Intent(MyCoursesListActivity.this,
                            CourseDetailTabActivity.class);
                    courseDetail.putExtra("bundle", courseBundle);
                    startActivity(courseDetail);

                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onAnnouncementClicked(EnrolledCoursesResponse model) {
                Bundle courseBundle = new Bundle();
                courseBundle.putBoolean("announcemnts", true);
                courseBundle.putSerializable("CourseDetail", model);

                Intent courseDetail = new Intent(MyCoursesListActivity.this,
                        CourseDetailTabActivity.class);
                courseDetail.putExtra("CourseDetail", courseBundle);
                context.startActivity(courseDetail);                
            }
        };
        myCourseList.setAdapter(adapter);
        myCourseList.setOnItemClickListener(adapter);

        loadData();
    }

    public void showCourseNotListedDialog() {
        showWebDialog(getString(R.string.course_not_listed_file_name), false, 
                null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // GetEnrolledCoursesTask();
        setTitle(getString(R.string.label_my_courses));
    }

    private void loadData() {
        GetEnrolledCoursesTask task = new GetEnrolledCoursesTask(this) {
            @Override
            public void onFinish(ArrayList<EnrolledCoursesResponse> list) {
                if(list!=null && list.size()>0){
                    hideEmptyCourseMessage();
                    //update all videos in the DB as Deactivated
                    db.updateAllVideosAsDeactivated(dataCallback);

                    for(int i=0;i<list.size();i++){
                        //Check if the flag of isIs_active is marked to true,
                        //then activate all videos
                        if(list.get(i).isIs_active()){
                            //update all videos for a course fetched in the API as Activated
                            db.updateVideosActivatedForCourse(list.get(i).getCourse().getId(), 
                                    dataCallback);
                        }else{
                            list.remove(i);
                        }

                    }
                    //Delete all videos which are marked as Deactivated in the database
                    storage.deleteAllUnenrolledVideos();
                    adapter.setItems(list);
                    adapter.notifyDataSetChanged();
                    invalidateSwipeFunctionality();
                }else{
                    invalidateSwipeFunctionality();
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
                invalidateSwipeFunctionality();
                /*if(adapter.getCount()<=0){
                    showEmptyCourseMessage();
                }*/

                if (ex instanceof AuthException) {
                    // there is some authentication error
                    // clear auth tokens
                    PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
                    pref.clearAuth();

                    // end now
                    LogUtil.error(getClass().getName(), "finishing due to auth error: " + ex.getMessage());
                    finish();
                }
            }
        };

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.api_spinner);
        task.setProgressDialog(progressBar);
        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem checkBox_menuItem = menu.findItem(R.id.delete_checkbox);
        checkBox_menuItem.setVisible(false);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    public void showOfflinePanel() {
        animateLayouts(offlinePanel);
    }

    public void hideOfflinePanel() {
        offlinePanel = (LinearLayout) findViewById(R.id.offline_panel);
        stopAnimation(offlinePanel);
        if(offlinePanel.getVisibility()==View.VISIBLE){
            offlinePanel.setVisibility(View.GONE);
        }
        //offlinePanel.setVisibility(View.GONE);
    }

    @Override
    protected void onOffline() {
        AppConstants.offline_flag = true;
        offlineBar.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
        showOfflinePanel();
        swipeLayout.setEnabled(false);
    }

    @Override
    protected void onOnline() {
        AppConstants.offline_flag = false;
        offlineBar.setVisibility(View.GONE);
        hideOfflinePanel();
        invalidateOptionsMenu();
        swipeLayout.setEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!AppConstants.offline_flag){
            hideOfflinePanel();
        }
        invalidateOptionsMenu();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideOfflinePanel();
    }

    private void invalidateSwipeFunctionality(){
        swipeLayout.setRefreshing(false);
    }

    private void hideEmptyCourseMessage(){
        try{
            TextView empty_tv = (TextView)findViewById(R.id.no_course_tv);
            if(empty_tv!=null){
                empty_tv.setVisibility(View.GONE);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Adds a footer view to the list, which has "FIND A COURSE" button.
     * @param myCourseList - ListView
     */
    private void setupFooter(ListView myCourseList) {
        try {
            View footer = LayoutInflater.from(this).inflate(R.layout.panel_find_course, null);
            myCourseList.addFooterView(footer, null, false);
            Button course_btn = (Button) footer.findViewById(R.id.course_btn);
            course_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        segIO.trackUserFindsCourses();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //Show the dialog only if the activity is started. This is to avoid Illegal state
                    //exceptions if the dialog fragment tries to show even if the application is not in foreground
                    if(isActivityStarted()){
                        FindCoursesDialogFragment findCoursesFragment = new FindCoursesDialogFragment();
                        findCoursesFragment.setStyle(DialogFragment.STYLE_NORMAL,
                                android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                        findCoursesFragment.setCancelable(false);
                        findCoursesFragment.show(getSupportFragmentManager(), "dialog");
                    }
                }
            });

            ETextView courseNotListedTv = (ETextView) findViewById(R.id.course_not_listed_tv);
            courseNotListedTv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCourseNotListedDialog();
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private DataCallback<Integer> dataCallback = new DataCallback<Integer>() {
        @Override
        public void onResult(Integer result) {
        }
        @Override
        public void onFail(Exception ex) {
            ex.printStackTrace();
        }
    };
}
