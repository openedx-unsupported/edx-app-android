package org.edx.mobile.view;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.DiscussionAPI;
import com.qualcomm.qlearn.sdk.discussion.DiscussionThread;

import org.edx.mobile.R;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.view.adapters.DiscussionPostsAdapter;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;


public abstract class CourseDiscussionPostsBaseFragment extends RoboFragment {

    @InjectView(R.id.discussion_posts_listview)
    ListView discussionPostsListView;

    @InjectExtra(Router.EXTRA_COURSE_DATA)
    EnrolledCoursesResponse courseData;

    @Inject
    DiscussionPostsAdapter discussionPostsAdapter;

    @Inject
    DiscussionAPI discussionAPI;

    @Inject
    Router router;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        discussionPostsListView.setAdapter(discussionPostsAdapter);

        discussionPostsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DiscussionThread thread = discussionPostsAdapter.getItem(position);
                router.showCourseDiscussionResponses(getActivity(), thread, courseData);
            }
        });

        populateThreadList();
    }

    protected abstract void populateThreadList();

}
