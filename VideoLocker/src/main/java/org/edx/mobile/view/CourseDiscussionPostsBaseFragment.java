package org.edx.mobile.view;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.task.SetThreadReadTask;
import org.edx.mobile.view.adapters.DiscussionPostsAdapter;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;


public abstract class CourseDiscussionPostsBaseFragment extends BaseFragment implements InfiniteScrollUtils.PageLoader<DiscussionThread> {

    @InjectView(R.id.discussion_posts_listview)
    ListView discussionPostsListView;

    @InjectExtra(Router.EXTRA_COURSE_DATA)
    EnrolledCoursesResponse courseData;

    @Inject
    DiscussionPostsAdapter discussionPostsAdapter;

    @Inject
    Router router;

    InfiniteScrollUtils.InfiniteListController controller;

    /**
     * Callback to match the restart behaviour with the parent activity.
     */
    public abstract void onRestart();

    /**
     * Extension for the child classes to add more functionality to the ListView's onItemClick
     * function.
     */
    public abstract void onItemClick(DiscussionThread thread, AdapterView<?> parent,
                                     View view, int position, long id);

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

                new SetThreadReadTask(context, thread, true).execute();
                if (!thread.isRead()) {
                    // Refresh the row to mark it as read immediately,
                    // pending the response from the server.
                    thread.setRead(true);
                    discussionPostsAdapter.getView(position, view, parent);
                }

                CourseDiscussionPostsBaseFragment.this.onItemClick(thread, parent, view,
                        position, id);
            }
        });
    }
}
