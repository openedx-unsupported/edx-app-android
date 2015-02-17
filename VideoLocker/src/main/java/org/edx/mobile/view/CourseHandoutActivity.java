package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;

public class CourseHandoutActivity extends BaseSingleFragmentActivity {

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(R.string.tab_label_handouts));
    }

    @Override
    public Fragment getFirstFragment() {

        Fragment frag = new CourseHandoutFragment();

        EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) getIntent().getSerializableExtra(CourseHandoutFragment.ENROLLMENT);
        if (courseData != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(CourseHandoutFragment.ENROLLMENT, courseData);
            frag.setArguments(bundle);
        }
        return frag;
    }
}


