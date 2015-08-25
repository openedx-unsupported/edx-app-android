package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.http.Api;
import org.edx.mobile.interfaces.NetworkObserver;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;


public class CourseDetailInfoActivity extends CourseBaseActivity {

    private CourseCombinedInfoFragment fragment;


    public static String TAG = CourseDetailInfoActivity.class.getCanonicalName();

    private View offlineBar;


    Bundle bundle;
    String activityTitle;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setApplyPrevTransitionOnRestart(true);

        bundle = getIntent().getBundleExtra(Router.EXTRA_BUNDLE);
        offlineBar = findViewById(R.id.offline_bar);
        if (!(NetworkUtil.isConnected(this))) {
            AppConstants.offline_flag = true;
            invalidateOptionsMenu();
            if(offlineBar!=null){
                offlineBar.setVisibility(View.VISIBLE);
            }
        }

        try{
            EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) bundle
                    .getSerializable(Router.EXTRA_ENROLLMENT);

            //check courseData again, it may be fetched from local cache
            if ( courseData != null ) {
                activityTitle = courseData.getCourse().getName();

                try{
                    environment.getSegment().screenViewsTracking(courseData.getCourse().getName());
                }catch(Exception e){
                    logger.error(e);
                }
            } else {

                boolean handleFromNotification = handleIntentFromNotification();
                //this is not from notification
                if (!handleFromNotification) {
                    //it is a good idea to go to the my course page. as loading of my courses
                    //take a while to load. that the only way to get anouncement link
                    environment.getRouter().showMyCourses(this);
                    finish();
                }
            }

        }catch(Exception ex){
            environment.getRouter().showMyCourses(this);
            finish();
            logger.error(ex);
        }

    }

    /**
     * @return <code>true</code> if handle intent from notification successfully
     */
    private boolean handleIntentFromNotification(){
        if ( bundle != null ){
            String courseId = bundle.getString(Router.EXTRA_COURSE_ID);
            //this is from notification
            if (!TextUtils.isEmpty(courseId)){
                try{
                    bundle.remove(Router.EXTRA_COURSE_ID);
                    Api api = new Api(this);
                    EnrolledCoursesResponse courseData = api.getCourseById(courseId);
                    if (courseData != null && courseData.getCourse() != null ) {
                        bundle.putSerializable(Router.EXTRA_ENROLLMENT, courseData);
                        activityTitle = courseData.getCourse().getName();
                        return true;
                    }
                }catch (Exception ex){
                    logger.error(ex);
                }
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleIntentFromNotification();
        invalidateOptionsMenu();
    }

    @Override
    protected void onOffline() {
        AppConstants.offline_flag = true;
        if(offlineBar!=null){
            offlineBar.setVisibility(View.VISIBLE);
        }

        for (Fragment fragment : getSupportFragmentManager().getFragments()){
            if (fragment instanceof NetworkObserver){
                ((NetworkObserver) fragment).onOffline();
            }
        }
        invalidateOptionsMenu();
    }

    @Override
    protected void onOnline() {
        AppConstants.offline_flag = false;
        if(offlineBar!=null){
            offlineBar.setVisibility(View.GONE);
        }

        for (Fragment fragment : getSupportFragmentManager().getFragments()){
            if (fragment instanceof NetworkObserver){
                ((NetworkObserver) fragment).onOnline();
            }
        }
        invalidateOptionsMenu();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null){
            try {

                fragment = new CourseCombinedInfoFragment();

                if (courseData != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Router.EXTRA_ENROLLMENT, courseData);
                    fragment.setArguments(bundle);

                }
                //this activity will only ever hold this lone fragment, so we
                // can afford to retain the instance during activity recreation
                fragment.setRetainInstance(true);

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container, fragment);
                fragmentTransaction.disallowAddToBackStack();
                fragmentTransaction.commit();

            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    protected boolean createOptionMenu(Menu menu) {
        return false;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected String getUrlForWebView() {
        if ( courseData != null && courseData.getCourse() != null ){
            return courseData.getCourse().getCourse_url();
        }
        return "";
    }
}
