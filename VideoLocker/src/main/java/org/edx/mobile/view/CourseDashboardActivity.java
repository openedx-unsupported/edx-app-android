package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.task.GetCourseStructureTask;
import org.edx.mobile.util.DateUtil;


/**
 * TODO - it is just a place holder for now. as we need to use it
 * to navigation to new views.
 */
public class CourseDashboardActivity extends CourseBaseActivity implements CourseDashboardFragment.ShowCourseOutlineCallback {

    protected Logger logger = new Logger(getClass().getSimpleName());

    private CourseDashboardFragment fragment;
    private GetCourseStructureTask getHierarchyTask;
    private boolean isTaskRunning;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        setApplyPrevTransitionOnRestart(true);
        // configure slider layout. This should be called only once and
        // hence is shifted to onCreate() function
      //  configureDrawer();

        try{
            environment.getSegment().screenViewsTracking(getString(R.string.course_home));
        }catch(Exception e){
            logger.error(e);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if ( courseData != null && courseData.getCourse() != null ){
            setTitle(courseData.getCourse().getName());
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null){
            try {

                fragment = new CourseDashboardFragment();

                if (courseData != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(CourseDashboardFragment.CourseData, courseData);
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

    @Override
    public void showCourseOutline() {
        if ( isTaskRunning )
            return;

        getHierarchyTask = new GetCourseStructureTask(this, courseData.getCourse().getId()) {

            @Override
            public void onSuccess(CourseComponent aCourse) {
                isTaskRunning = false;
                if (aCourse != null) {
                    logger.debug("Start displaying on UI "+ DateUtil.getCurrentTimeStamp());
                    environment.getRouter().showCourseContainerOutline(CourseDashboardActivity.this, courseData, aCourse.getId());
                }
                logger.debug("Completed displaying data on UI "+ DateUtil.getCurrentTimeStamp());
            }

            @Override
            public void onException(Exception ex) {
                isTaskRunning = false;
                showInfoMessage(getString(R.string.no_connectivity));
            }
        };
        getHierarchyTask.setTaskProcessCallback(this);
        getHierarchyTask.setProgressDialog(progressWheel);
        //Initializing task call
        logger.debug("Initializing Chapter Task" + DateUtil.getCurrentTimeStamp());
        isTaskRunning = true;
        getHierarchyTask.execute();

    }
}
