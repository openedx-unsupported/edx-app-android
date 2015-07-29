package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;

public class CourseDiscussionActivity extends BaseSingleFragmentActivity {
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blockDrawerFromOpening();
    }

    @Override
    public Fragment getFirstFragment() {
        fragment = new CourseDiscussionFragment();
        EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) getIntent()
                .getSerializableExtra(Router.EXTRA_ENROLLMENT);
        if (courseData != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Router.EXTRA_ENROLLMENT, courseData);
            fragment.setArguments(bundle);
        }
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(R.string.discussion_topics_title));
    }
}
