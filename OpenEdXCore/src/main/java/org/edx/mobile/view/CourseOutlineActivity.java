package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import org.edx.mobile.R;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.services.LastAccessManager;

import javax.inject.Inject;


/**
 *  Top level outline for the Course
 */
public class CourseOutlineActivity extends CourseVideoListActivity {

    private CourseOutlineFragment fragment;

    @Inject
    CourseManager courseManager;

    @Inject
    LastAccessManager lastAccessManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isOnCourseOutline()) {
            environment.getSegment().trackScreenView(
                    ISegment.Screens.COURSE_OUTLINE, courseData.getCourse().getId(), null);
        }
    }

    public void onResume(){
        super.onResume();

        if (isOnCourseOutline()) {
            setTitle(courseData.getCourse().getName());
            lastAccessManager.fetchLastAccessed(this, courseData.getCourse().getId());
        }
    }

    @Override
    protected void onLoadData() {
        CourseComponent courseComponent = courseManager.getComponentById(
                courseData.getCourse().getId(), courseComponentId);
        setTitle(courseComponent.getDisplayName());

        if (fragment == null) {
            fragment = new CourseOutlineFragment();
            fragment.setTaskProcessCallback(this);

            Bundle bundle = new Bundle();
            bundle.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
            bundle.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId);
            bundle.putString(Router.EXTRA_LAST_ACCESSED_ID
                    , getIntent().getStringExtra(Router.EXTRA_LAST_ACCESSED_ID));
            fragment.setArguments(bundle);
            //this activity will only ever hold this lone fragment, so we
            // can afford to retain the instance during activity recreation
            fragment.setRetainInstance(true);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, fragment, CourseOutlineFragment.TAG);
            fragmentTransaction.disallowAddToBackStack();
            fragmentTransaction.commitAllowingStateLoss();
        }

        if (isOnCourseOutline()) {
            lastAccessManager.fetchLastAccessed(this, courseData.getCourse().getId());
        } else {
            environment.getSegment().trackScreenView(
                    ISegment.Screens.SECTION_OUTLINE, courseData.getCourse().getId(), courseComponent.getInternalName());

            // Update the last accessed item reference if we are in the course subsection view
            lastAccessManager.setLastAccessed(courseComponent.getCourseId(), courseComponent.getId());
        }
    }

    @Override
    protected String getUrlForWebView() {
        if ( courseComponentId == null ) return "";
        CourseComponent courseComponent = courseManager.getComponentById(courseData.getCourse().getId(), courseComponentId);
        return courseComponent.getWebUrl();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState != null){
             fragment = (CourseOutlineFragment)
                 getSupportFragmentManager().findFragmentByTag(CourseOutlineFragment.TAG);
        }
    }

    @Override
    public void updateListUI() {
        if( fragment != null ) {
            fragment.reloadList();
            fragment.updateMessageView(null);
        }
    }
}
