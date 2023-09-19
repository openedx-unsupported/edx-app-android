package org.edx.mobile.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;

import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentDiscussionTopicsBinding;
import org.edx.mobile.discussion.DiscussionService;
import org.edx.mobile.event.CourseDashboardRefreshEvent;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.discussion.CourseTopics;
import org.edx.mobile.model.discussion.DiscussionTopic;
import org.edx.mobile.model.discussion.DiscussionTopicDepth;
import org.edx.mobile.util.SoftKeyboardUtil;
import org.edx.mobile.util.UiUtils;
import org.edx.mobile.view.adapters.DiscussionTopicsAdapter;
import org.edx.mobile.view.common.TaskProgressCallback;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;

@AndroidEntryPoint
public class CourseDiscussionTopicsFragment extends OfflineSupportBaseFragment
        implements RefreshListener {
    private static final Logger logger = new Logger(CourseDiscussionTopicsFragment.class.getName());

    private EnrolledCoursesResponse courseData;

    @Inject
    DiscussionService discussionService;

    @Inject
    Router router;

    private Call<CourseTopics> getTopicListCall;

    private FullScreenErrorNotification errorNotification;
    private FragmentDiscussionTopicsBinding binding;
    private DiscussionTopicsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        courseData = (EnrolledCoursesResponse) getArguments().getSerializable(Router.EXTRA_COURSE_DATA);
        binding = FragmentDiscussionTopicsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        errorNotification = new FullScreenErrorNotification((View) binding.discussionTopicsRv.getParent());
        DividerItemDecoration itemDecorator = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(UiUtils.INSTANCE.getDrawable(requireContext(), R.drawable.list_item_divider));
        binding.discussionTopicsRv.addItemDecoration(itemDecorator);
        adapter = new DiscussionTopicsAdapter(
                item -> router.showCourseDiscussionPostsForDiscussionTopic(
                        requireActivity(),
                        item.getDiscussionTopic(),
                        courseData)
        );
        binding.discussionTopicsRv.setAdapter(adapter);

        binding.discussionTopicsSearchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query == null || query.trim().isEmpty())
                    return false;
                router.showCourseDiscussionPostsForSearchQuery(requireActivity(), query, courseData);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        getTopicList();
        showCourseDiscussionTopic();
    }

    /**
     * Method to return custom headers to the topics list
     */
    private List<DiscussionTopicDepth> getTopicsHeaders() {
        final List<DiscussionTopicDepth> headerList = new ArrayList<>();
        // Add "All posts" item
        {
            final DiscussionTopic discussionTopic = new DiscussionTopic();
            discussionTopic.setIdentifier(DiscussionTopic.ALL_TOPICS_ID);
            discussionTopic.setName(getString(R.string.discussion_posts_filter_all_posts));
            headerList.add(new DiscussionTopicDepth(discussionTopic, 0, true));
        }

        // Add "Posts I'm following" item
        {
            final DiscussionTopic discussionTopic = new DiscussionTopic();
            discussionTopic.setIdentifier(DiscussionTopic.FOLLOWING_TOPICS_ID);
            discussionTopic.setName(getString(R.string.forum_post_i_am_following));
            headerList.add(new DiscussionTopicDepth(discussionTopic, 0, true));
        }
        return headerList;
    }

    private void getTopicList() {
        if (getTopicListCall != null) {
            getTopicListCall.cancel();
        }
        final TaskProgressCallback.ProgressViewController progressViewController =
                new TaskProgressCallback.ProgressViewController(binding.loadingIndicator.loadingIndicator);
        getTopicListCall = discussionService.getCourseTopics(courseData.getCourse().getId());
        getTopicListCall.enqueue(new ErrorHandlingCallback<CourseTopics>(
                requireActivity(), progressViewController, errorNotification, null, this) {
            @Override
            protected void onResponse(@NonNull final CourseTopics courseTopics) {
                logger.debug("GetTopicListTask success=" + courseTopics);
                ArrayList<DiscussionTopic> allTopics = new ArrayList<>();
                allTopics.addAll(courseTopics.getNonCoursewareTopics());
                allTopics.addAll(courseTopics.getCoursewareTopics());
                List<DiscussionTopicDepth> allTopicsWithDepth = getTopicsHeaders();
                allTopicsWithDepth.addAll(DiscussionTopicDepth.createFromDiscussionTopics(allTopics));
                adapter.submitList(allTopicsWithDepth);
            }

            @Override
            protected void onFinish() {
                if (!EventBus.getDefault().isRegistered(CourseDiscussionTopicsFragment.this)) {
                    EventBus.getDefault().register(CourseDiscussionTopicsFragment.this);
                }
            }
        });
    }

    private void showCourseDiscussionTopic() {
        final String topicId = getArguments().getString(Router.EXTRA_DISCUSSION_TOPIC_ID);
        if (!TextUtils.isEmpty(topicId)) {
            router.showCourseDiscussionPostsForDiscussionTopic(
                    requireActivity(),
                    getArguments().getString(Router.EXTRA_DISCUSSION_TOPIC_ID),
                    getArguments().getString(Router.EXTRA_DISCUSSION_THREAD_ID),
                    courseData);

            // Setting this to null, so that upon recreation of the fragment, relevant activity
            // shouldn't be auto-created again (e.g. due to a deep link).
            getArguments().putString(Router.EXTRA_DISCUSSION_TOPIC_ID, null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SoftKeyboardUtil.clearViewFocus(binding.discussionTopicsSearchview);
    }

    @Subscribe(sticky = true)
    @SuppressWarnings("unused")
    public void onEvent(CourseDashboardRefreshEvent event) {
        errorNotification.hideError();
        getTopicList();
    }

    @Override
    public void onRefresh() {
        EventBus.getDefault().post(new CourseDashboardRefreshEvent());
    }

    @Override
    protected boolean isShowingFullScreenError() {
        return errorNotification != null && errorNotification.isShowing();
    }

    @Subscribe(sticky = true)
    @SuppressWarnings("unused")
    public void onEvent(NetworkConnectivityChangeEvent event) {
        onNetworkConnectivityChangeEvent(event);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }
}
