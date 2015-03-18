package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;

public class CourseHandoutActivity extends BaseSingleFragmentActivity {
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Handouts activity should not contain the drawer(Navigation Fragment).
        blockDrawerFromOpening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(R.string.tab_label_handouts));
    }

    @Override
    public Fragment getFirstFragment() {

        fragment = new CourseHandoutFragment();
        EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) getIntent()
                .getSerializableExtra(CourseHandoutFragment.ENROLLMENT);
        if (courseData != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(CourseHandoutFragment.ENROLLMENT, courseData);
            fragment.setArguments(bundle);
        }

        return fragment;
    }

    @Override
    protected void onOffline() {
        super.onOffline();
        //If the Handouts screen goes offline, we need to show Offline message
        if(fragment!=null && fragment instanceof CourseHandoutFragment){
            ((CourseHandoutFragment) fragment).showHandoutsOffline();
        }
    }
}
