package org.edx.mobile.view;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.view.adapters.DiscussionPostsAdapter;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;

import roboguice.inject.InjectView;


public abstract class CourseDiscussionPostsBaseFragment extends BaseFragment implements InfiniteScrollUtils.PageLoader<DiscussionThread> {
    @InjectView(R.id.discussion_posts_listview)
    protected ListView discussionPostsListView;

    @Inject
    protected DiscussionPostsAdapter discussionPostsAdapter;

    @Inject
    protected Router router;

    @Inject
    protected IEdxEnvironment environment;

    protected EnrolledCoursesResponse courseData;

    protected InfiniteScrollUtils.InfiniteListController controller;

    protected int nextPage = 1;

    private boolean isRestart = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        courseData = (EnrolledCoursesResponse) getArguments().getSerializable(Router.EXTRA_COURSE_DATA);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller = InfiniteScrollUtils.configureListViewWithInfiniteList(discussionPostsListView, discussionPostsAdapter, this);
        discussionPostsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Context context = getContext();
                DiscussionThread thread = discussionPostsAdapter.getItem(position);
                router.showCourseDiscussionResponses(context, thread, courseData);

                if (!thread.isRead()) {
                    // Refresh the row to mark it as read immediately.
                    // There will be a silent refresh upon return to this Activity.
                    thread.setRead(true);
                    discussionPostsAdapter.getView(position, view, parent);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        if (isRestart) {
        /*
         * If the activity/fragment needs to be reinstantiated upon restoration,
         * then in some cases the onStart() callback maybe invoked before view
         * initialization, and thus the controller might not be initialized, and
         * therefore we need to guard this with a null check.
         */
            if (controller != null) {
                nextPage = 1;
                controller.resetSilently();
            }
        }
        isRestart = true;
    }
}
