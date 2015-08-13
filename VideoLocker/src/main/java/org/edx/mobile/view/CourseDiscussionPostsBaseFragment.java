package org.edx.mobile.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.DiscussionAPI;

import org.edx.mobile.R;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.view.adapters.DiscussionPostsAdapter;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;


public abstract class CourseDiscussionPostsBaseFragment extends RoboFragment {

    @InjectView(R.id.discussion_posts_listview)
    ListView discussionPostsListView;

    @InjectView(R.id.create_new_item_text_view)
    TextView createNewPostTextView;

    @InjectView(R.id.create_new_layout)
    RelativeLayout discussionPostsCreatePostLayout;

    @InjectExtra(Router.EXTRA_COURSE_DATA)
    EnrolledCoursesResponse courseData;

    @Inject
    DiscussionPostsAdapter discussionPostsAdapter;

    @Inject
    DiscussionAPI discussionAPI;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        discussionPostsListView.setAdapter(discussionPostsAdapter);
        discussionPostsListView.setOnItemClickListener(discussionPostsAdapter);
        createNewPostTextView.setText(R.string.discussion_post_create_new_post);

        discussionPostsCreatePostLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Launch new post activity
            }
        });

        populateThreadList();
    }

    protected abstract void populateThreadList();

}
