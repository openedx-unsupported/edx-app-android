package org.edx.mobile.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentDiscussionTopicsBinding;
import org.edx.mobile.discussion.CourseTopics;
import org.edx.mobile.discussion.DiscussionService;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.discussion.DiscussionTopicDepth;
import org.edx.mobile.event.CourseDashboardRefreshEvent;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.SoftKeyboardUtil;
import org.edx.mobile.util.UiUtils;
import org.edx.mobile.view.adapters.DiscussionTopicsAdapter;
import org.edx.mobile.view.common.TaskProgressCallback;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit2.Call;

public class CourseDiscussionTopicsFragment extends OfflineSupportBaseFragment
        implements RefreshListener {
    private static final Logger logger = new Logger(CourseDiscussionTopicsFragment.class.getName());

    private EnrolledCoursesResponse courseData;

    @Inject
    private DiscussionService discussionService;

    @Inject
    private DiscussionTopicsAdapter discussionTopicsAdapter;

    @Inject
    private Router router;

    private Call<CourseTopics> getTopicListCall;

    private FullScreenErrorNotification errorNotification;
    private FragmentDiscussionTopicsBinding binding;

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
        errorNotification = new FullScreenErrorNotification((View) binding.discussionTopicsListview.getParent());

        final LayoutInflater inflater = LayoutInflater.from(requireActivity());

        // Add "All posts" item
        {
            final TextView header = (TextView) inflater.inflate(R.layout.row_discussion_topic, binding.discussionTopicsListview, false);
            header.setText(R.string.discussion_posts_filter_all_posts);

            final DiscussionTopic discussionTopic = new DiscussionTopic();
            discussionTopic.setIdentifier(DiscussionTopic.ALL_TOPICS_ID);
            discussionTopic.setName(getString(R.string.discussion_posts_filter_all_posts));
            binding.discussionTopicsListview.addHeaderView(header, new DiscussionTopicDepth(discussionTopic, 0, true), true);
        }

        // Add "Posts I'm following" item
        {
            final TextView header = (TextView) inflater.inflate(R.layout.row_discussion_topic,
                    binding.discussionTopicsListview, false);
            header.setText(R.string.forum_post_i_am_following);
            UiUtils.INSTANCE.setTextViewDrawableStart(requireContext(), header, R.drawable.ic_star_rate,
                    R.dimen.edx_base, R.color.primaryBaseColor);
            final DiscussionTopic discussionTopic = new DiscussionTopic();
            discussionTopic.setIdentifier(DiscussionTopic.FOLLOWING_TOPICS_ID);
            discussionTopic.setName(getString(R.string.forum_post_i_am_following));
            binding.discussionTopicsListview.addHeaderView(header, new DiscussionTopicDepth(discussionTopic, 0, true), true);
        }

        binding.discussionTopicsListview.setAdapter(discussionTopicsAdapter);

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

        binding.discussionTopicsListview.setOnItemClickListener(
                (parent, view1, position, id) -> router.showCourseDiscussionPostsForDiscussionTopic(
                        requireActivity(),
                        ((DiscussionTopicDepth) parent.getItemAtPosition(position)).getDiscussionTopic(),
                        courseData));

        getTopicList();
        showCourseDiscussionTopic();
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

                List<DiscussionTopicDepth> allTopicsWithDepth = DiscussionTopicDepth.createFromDiscussionTopics(allTopics);
                discussionTopicsAdapter.setItems(allTopicsWithDepth);
                discussionTopicsAdapter.notifyDataSetChanged();
            }

            @Override
            protected void onFinish() {
                if (!EventBus.getDefault().isRegistered(CourseDiscussionTopicsFragment.this)) {
                    EventBus.getDefault().registerSticky(CourseDiscussionTopicsFragment.this);
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
