package org.edx.mobile.view;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.discussion.DiscussionThread;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.util.UiUtils;
import org.edx.mobile.view.adapters.DiscussionPostsAdapter;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;

import javax.inject.Inject;


public abstract class CourseDiscussionPostsBaseFragment extends BaseFragment implements InfiniteScrollUtils.PageLoader<DiscussionThread> {

    protected DiscussionPostsAdapter discussionPostsAdapter;

    @Inject
    protected Router router;

    @Inject
    AnalyticsRegistry analyticsRegistry;

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
        discussionPostsAdapter = new DiscussionPostsAdapter(item -> {
            router.showCourseDiscussionResponses(requireContext(), item, courseData);
        });
        controller = InfiniteScrollUtils.configureRecyclerViewWithInfiniteList(getDiscussionPostsRecyclerView(), discussionPostsAdapter, this);
        DividerItemDecoration itemDecorator = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(UiUtils.INSTANCE.getDrawable(requireContext(), R.drawable.list_item_divider));
        getDiscussionPostsRecyclerView().addItemDecoration(itemDecorator);
        getDiscussionPostsRecyclerView().setAdapter(discussionPostsAdapter);
    }

    protected abstract RecyclerView getDiscussionPostsRecyclerView();

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
