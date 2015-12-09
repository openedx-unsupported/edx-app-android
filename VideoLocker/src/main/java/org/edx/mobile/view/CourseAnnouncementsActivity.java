package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.http.IApi;
import org.edx.mobile.interfaces.NetworkObserver;
import org.edx.mobile.model.api.EnrolledCoursesResponse;


public class CourseAnnouncementsActivity extends BaseFragmentActivity {

    @Inject
    IApi api;

    private CourseCombinedInfoFragment fragment;
    private EnrolledCoursesResponse courseData;


    public static String TAG = CourseAnnouncementsActivity.class.getCanonicalName();

    private View offlineBar;


    Bundle bundle;
    String activityTitle;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setApplyPrevTransitionOnRestart(true);

        bundle = savedInstanceState != null ? savedInstanceState :
                getIntent().getBundleExtra(Router.EXTRA_BUNDLE);
        offlineBar = findViewById(R.id.offline_bar);

        courseData = (EnrolledCoursesResponse) bundle
                .getSerializable(Router.EXTRA_ENROLLMENT);

        //check courseData again, it may be fetched from local cache
        if ( courseData != null ) {
            activityTitle = courseData.getCourse().getName();

            environment.getSegment().trackScreenView(courseData.getCourse().getName());
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
                    courseData = api.getCourseById(courseId);
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
        super.onOffline();
        if (offlineBar != null) {
            offlineBar.setVisibility(View.VISIBLE);
        }

        for (Fragment fragment : getSupportFragmentManager().getFragments()){
            if (fragment instanceof NetworkObserver){
                ((NetworkObserver) fragment).onOffline();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Router.EXTRA_ENROLLMENT, courseData);
    }

    @Override
    protected void onOnline() {
        super.onOnline();
        if (offlineBar != null) {
            offlineBar.setVisibility(View.GONE);
        }

        for (Fragment fragment : getSupportFragmentManager().getFragments()){
            if (fragment instanceof NetworkObserver){
                ((NetworkObserver) fragment).onOnline();
            }
        }
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
                fragmentTransaction.add(android.R.id.content, fragment);
                fragmentTransaction.disallowAddToBackStack();
                fragmentTransaction.commit();

            } catch (Exception e) {
                logger.error(e);
            }
        }
    }
}
