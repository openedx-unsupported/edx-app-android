package org.edx.mobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.model.course.BlockPath;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.services.LastAccessManager;


/**
 *  Top level outline for the Course
 */
public class CourseOutlineActivity extends CourseVideoListActivity {

    static final int REQUEST_SHOW_COURSE_UNIT_DETAIL = 0;

    private CourseOutlineFragment fragment;

    @Inject
    CourseManager courseManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        setApplyPrevTransitionOnRestart(true);
        try{
            environment.getSegment().screenViewsTracking(getString(R.string.course_outline));
        }catch(Exception e){
            logger.error(e);
        }

    }

    public void onResume(){
        super.onResume();

        if ( courseData != null && courseData.getCourse() != null ){
            CourseComponent courseComponent = courseManager.getComponentById(courseData.getCourse().getId(), courseComponentId);
            if ( courseComponent == null)
                setTitle( courseData.getCourse().getName() );
            else
                setTitle( courseComponent.getName() );

            if (isOnCourseOutline())
                LastAccessManager.getSharedInstance().fetchLastAccessed(this, courseData.getCourse().getId());
        }
    }

    @Override
    protected String getUrlForWebView() {
        if ( courseData == null )
            return "";
        CourseComponent courseComponent = courseManager.getComponentById(courseData.getCourse().getId(), courseComponentId);
        return courseComponent == null ? "" : courseComponent.getWebUrl();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null){
            try {
                fragment = new CourseOutlineFragment();
                fragment.setTaskProcessCallback(this);

                if (courseData != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Router.EXTRA_ENROLLMENT, courseData);
                    bundle.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId);
                    fragment.setArguments(bundle);
                }
                //this activity will only ever hold this lone fragment, so we
                // can afford to retain the instance during activity recreation
                fragment.setRetainInstance(true);

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container, fragment, CourseOutlineFragment.TAG);
                fragmentTransaction.disallowAddToBackStack();
                fragmentTransaction.commit();

            } catch (Exception e) {
                logger.error(e);
            }
        } else {
             fragment = (CourseOutlineFragment)
                 getSupportFragmentManager().findFragmentByTag(CourseOutlineFragment.TAG);
        }

        // We need to update LastAccessed Item if we are in CourseSubsection View
        if (!isOnCourseOutline()) {
            CourseComponent courseComponent = courseManager.getComponentById(courseData.getCourse().getId(), courseComponentId);
            String prefName = PrefManager.getPrefNameForLastAccessedBy(getProfile()
                    .username, courseComponent.getCourseId());
            final PrefManager prefManager = new PrefManager(MainApplication.instance(), prefName);
            prefManager.putLastAccessedSubsection(courseComponent.getId(), false);
        }
    }

    @Override
    public void updateListUI() {
        if( fragment != null )
            fragment.reloadList();
        fragment.updateMessageView(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // If user has navigated to a different unit, then we need to rearrange
            // the activity stack to point to it.
            case REQUEST_SHOW_COURSE_UNIT_DETAIL: {
                switch (resultCode) {
                    case RESULT_OK: {
                        CourseComponent outlineComp = courseManager.getComponentById(
                                courseData.getCourse().getId(), courseComponentId);
                        String leafCompId = (String) data.getSerializableExtra(Router.EXTRA_COURSE_COMPONENT_ID);
                        CourseComponent leafComp = courseManager.getComponentById(
                                courseData.getCourse().getId(), leafCompId);
                        BlockPath outlinePath = outlineComp.getPath();
                        BlockPath leafPath = leafComp.getPath();
                        int outlinePathSize = outlinePath.getPath().size();
                        if (!leafPath.get(outlinePathSize - 1).equals(outlineComp)) {
                            setResult(RESULT_OK, data);
                            finish();
                        } else {
                            for (int i = outlinePathSize + 1;; i += 2) {
                                CourseComponent nextComp = leafPath.get(i);
                                if (nextComp == null || !nextComp.isContainer()) {
                                    break;
                                }
                                environment.getRouter().showCourseContainerOutline(this,
                                        REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, nextComp.getId());
                            }
                            overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
                        }
                    }
                }
                break;
            }
        }
    }


}
