package org.edx.mobile.view;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.inject.Inject;

import org.edx.mobile.discussion.DiscussionThread;

import org.edx.mobile.R;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.view.adapters.BaseListAdapter;
import org.edx.mobile.view.adapters.DiscussionPostsAdapter;
import org.edx.mobile.view.adapters.IPagination;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;


public abstract class CourseDiscussionPostsBaseFragment extends RoboFragment implements BaseListAdapter.PaginationHandler{

    @InjectView(R.id.discussion_posts_listview)
    ListView discussionPostsListView;

    @InjectExtra(Router.EXTRA_COURSE_DATA)
    EnrolledCoursesResponse courseData;

    @Inject
    DiscussionPostsAdapter discussionPostsAdapter;

    @Inject
    Router router;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        discussionPostsAdapter.setPaginationHandler(this);
        discussionPostsListView.setAdapter(discussionPostsAdapter);

        discussionPostsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DiscussionThread thread = discussionPostsAdapter.getItem(position);
                router.showCourseDiscussionResponses(getActivity(), thread, courseData);
            }
        });

        populateThreadList(true);
    }

    /**
     * setAdapter calls mRecycler.clear();  which invalidate the recycled views
     */
    protected void refreshListViewOnDataChange(){
        discussionPostsListView.setAdapter(discussionPostsAdapter);
    }

    protected abstract void populateThreadList(boolean refreshView);

    public void loadMoreRecord(IPagination pagination){
        populateThreadList(false);
    }

}
