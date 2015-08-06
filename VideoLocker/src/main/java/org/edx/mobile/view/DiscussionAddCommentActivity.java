package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;

public class DiscussionAddCommentActivity extends BaseSingleFragmentActivity {
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blockDrawerFromOpening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(R.string.discussion_comment));
    }

    @Override
    public Fragment getFirstFragment() {

        fragment = new DiscussionAddCommentFragment();
        EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) getIntent()
                .getSerializableExtra(DiscussionAddCommentFragment.ENROLLMENT);
        if (courseData != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(DiscussionAddCommentFragment.ENROLLMENT, courseData);
            fragment.setArguments(bundle);
        }

        return fragment;
    }
}
