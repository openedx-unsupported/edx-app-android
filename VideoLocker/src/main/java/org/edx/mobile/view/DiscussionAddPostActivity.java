package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.qualcomm.qlearn.sdk.discussion.DiscussionTopic;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;

public class DiscussionAddPostActivity extends BaseSingleFragmentActivity {
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blockDrawerFromOpening();
    }

    @Override
    protected void onStart() {
        super.onStart();

        setTitle(getString(R.string.discussion_post));
    }

    @Override
    public Fragment getFirstFragment() {

        fragment = new DiscussionAddPostFragment();
        EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) getIntent()
                .getSerializableExtra(DiscussionAddPostFragment.ENROLLMENT);
        DiscussionTopic discussionTopic = (DiscussionTopic) getIntent()
                .getSerializableExtra(DiscussionAddPostFragment.TOPIC);


        if (courseData != null && discussionTopic != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(DiscussionAddPostFragment.ENROLLMENT, courseData);
            bundle.putSerializable(DiscussionAddPostFragment.TOPIC, discussionTopic);
            fragment.setArguments(bundle);
        }

        return fragment;
    }
}
