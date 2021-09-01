package org.edx.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentDiscussionThreadPostsBinding;
import org.edx.mobile.discussion.CourseDiscussionInfo;
import org.edx.mobile.discussion.CourseTopics;
import org.edx.mobile.discussion.DiscussionCommentPostedEvent;
import org.edx.mobile.discussion.DiscussionPostsFilter;
import org.edx.mobile.discussion.DiscussionPostsSort;
import org.edx.mobile.discussion.DiscussionRequestFields;
import org.edx.mobile.discussion.DiscussionService;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionThreadPostedEvent;
import org.edx.mobile.discussion.DiscussionThreadUpdatedEvent;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.discussion.TimePeriod;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.model.Page;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.util.UiUtils;
import org.edx.mobile.view.adapters.DiscussionPostsSpinnerAdapter;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;
import org.edx.mobile.view.common.TaskProgressCallback;
import org.edx.mobile.view.common.TaskProgressCallback.ProgressViewController;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import retrofit2.Call;

public class CourseDiscussionPostsThreadFragment extends CourseDiscussionPostsBaseFragment {
    public static final String ARG_DISCUSSION_HAS_TOPIC_NAME = "discussion_has_topic_name";

    @Inject
    private DiscussionService discussionService;

    private FullScreenErrorNotification errorNotification;

    private DiscussionTopic discussionTopic;
    private DiscussionPostsFilter postsFilter = DiscussionPostsFilter.ALL;
    private DiscussionPostsSort postsSort = DiscussionPostsSort.LAST_ACTIVITY_AT;

    private Call<Page<DiscussionThread>> getThreadListCall;

    /**
     * Runnable for deferring the fetching of threads for a topic, until we
     * have fetched the {@link #discussionTopic} object.
     */
    @Nullable
    private Runnable populatePostListRunnable;
    private FragmentDiscussionThreadPostsBinding binding;

    private enum EmptyQueryResultsFor {
        FOLLOWING,
        CATEGORY,
        COURSE
    }

    public static CourseDiscussionPostsThreadFragment newInstance(@NonNull String topicId,
                                                                  @NonNull Serializable courseData,
                                                                  boolean hasTopicName) {
        CourseDiscussionPostsThreadFragment f = new CourseDiscussionPostsThreadFragment();
        Bundle args = new Bundle();
        args.putString(Router.EXTRA_DISCUSSION_TOPIC_ID, topicId);
        args.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
        args.putBoolean(ARG_DISCUSSION_HAS_TOPIC_NAME, hasTopicName);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        parseExtras();
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDiscussionThreadPostsBinding.inflate(inflater, container, false);
        // Initializing errorNotification here, so that its non-null value can be used in the parent class's `onViewCreated` callback
        errorNotification = new FullScreenErrorNotification(binding.content);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        checkIfDiscussionsBlackedOut();

        if (discussionTopic == null) {
            // Either we are coming from a deep link or courseware's inline discussion
            fetchDiscussionTopic();
        } else {
            requireActivity().setTitle(discussionTopic.getName());
            trackScreenView();
        }

        binding.createNewItem.createNewItemTextView.setText(R.string.discussion_post_create_new_post);
        Context context = getActivity();
        UiUtils.INSTANCE.setTextViewDrawableStart(requireContext(), binding.createNewItem.createNewItemTextView,
                R.drawable.ic_add_comment, R.dimen.small_icon_size);
        binding.createNewItem.createNewItemLayout.setOnClickListener(v ->
                router.showCourseDiscussionAddPost(requireActivity(), discussionTopic, courseData)
        );

        binding.discussionPostsFilterSpinner.setAdapter(new DiscussionPostsSpinnerAdapter(
                binding.discussionPostsFilterSpinner, DiscussionPostsFilter.values(),
                R.drawable.ic_filter_alt));
        binding.discussionPostsSortSpinner.setAdapter(new DiscussionPostsSpinnerAdapter(
                binding.discussionPostsSortSpinner, DiscussionPostsSort.values(),
                R.drawable.ic_swap_vert));

        binding.discussionPostsFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id) {
                DiscussionPostsFilter selectedPostsFilter =
                        (DiscussionPostsFilter) parent.getItemAtPosition(position);
                if (postsFilter != selectedPostsFilter) {
                    postsFilter = selectedPostsFilter;
                    clearListAndLoadFirstPage();
                }
            }

            @Override
            public void onNothingSelected(@NonNull AdapterView<?> parent) {
            }
        });
        binding.discussionPostsSortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id) {
                DiscussionPostsSort selectedPostsSort =
                        (DiscussionPostsSort) parent.getItemAtPosition(position);
                if (postsSort != selectedPostsSort) {
                    postsSort = selectedPostsSort;
                    clearListAndLoadFirstPage();
                }
            }

            @Override
            public void onNothingSelected(@NonNull AdapterView<?> parent) {
            }
        });

        final String threadId = getArguments().getString(Router.EXTRA_DISCUSSION_THREAD_ID);
        if (!TextUtils.isEmpty(threadId)) {
            router.showCourseDiscussionResponses(context, threadId, courseData);

            // Setting this to null, so that upon recreation of the fragment, relevant activity
            // shouldn't be auto-created again (e.g. due to a deep link).
            getArguments().putString(Router.EXTRA_DISCUSSION_THREAD_ID, null);
        }
    }

    @Override
    protected ListView getDiscussionPostsListView() {
        return binding.discussionPostsListview;
    }

    private void parseExtras() {
        discussionTopic = (DiscussionTopic) getArguments().getSerializable(Router.EXTRA_DISCUSSION_TOPIC);
    }

    private void fetchDiscussionTopic() {
        String topicId = getArguments().getString(Router.EXTRA_DISCUSSION_TOPIC_ID);
        final Activity activity = requireActivity();
        discussionService.getSpecificCourseTopics(courseData.getCourse().getId(),
                Collections.singletonList(topicId))
                .enqueue(new ErrorHandlingCallback<CourseTopics>(activity,
                        new ProgressViewController(binding.loadingIndicator.loadingIndicator), errorNotification) {
                    @Override
                    protected void onResponse(@NonNull final CourseTopics courseTopics) {
                        discussionTopic = courseTopics.getCoursewareTopics().get(0).getChildren().get(0);
                        if (!requireArguments().getBoolean(ARG_DISCUSSION_HAS_TOPIC_NAME)) {
                            // We only need to set the title here when coming from a deep link
                            activity.setTitle(discussionTopic.getName());
                        }

                        if (getView() != null) {
                            if (populatePostListRunnable != null) {
                                populatePostListRunnable.run();
                            }

                            // Now that we have the topic data, we can allow the user to add new posts.
                            setCreateNewPostBtnVisibility(View.VISIBLE);
                        }
                        trackScreenView();
                    }
                });
    }

    private void trackScreenView() {
        final String actionItem;
        final Map<String, String> values = new HashMap<>();
        String topicId = discussionTopic.getIdentifier();
        if (DiscussionTopic.ALL_TOPICS_ID.equals(topicId)) {
            topicId = actionItem = Analytics.Values.POSTS_ALL;
        } else if (DiscussionTopic.FOLLOWING_TOPICS_ID.equals(topicId)) {
            topicId = actionItem = Analytics.Values.POSTS_FOLLOWING;
        } else {
            actionItem = discussionTopic.getName();
        }
        values.put(Analytics.Keys.TOPIC_ID, topicId);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FORUM_VIEW_TOPIC_THREADS,
                courseData.getCourse().getId(), actionItem, values);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DiscussionThreadUpdatedEvent event) {
        // If a listed thread's following status has changed, we need to replace it to show/hide the "following" label
        for (int i = 0; i < discussionPostsAdapter.getCount(); ++i) {
            if (discussionPostsAdapter.getItem(i).hasSameId(event.getDiscussionThread())) {
                discussionPostsAdapter.replace(event.getDiscussionThread(), i);
                break;
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DiscussionCommentPostedEvent event) {
        // If a new response/comment was posted in a listed thread, we need to update the list
        for (int i = 0; i < discussionPostsAdapter.getCount(); ++i) {
            final DiscussionThread discussionThread = discussionPostsAdapter.getItem(i);
            if (discussionThread.containsComment(event.getComment())) {
                // No need to update the discussionThread object because its already updated on
                // the responses screen and is shared on both screens, because it's queried via
                // a PATCH call in the responses screen to mark it as read, and the response is
                // broadcasted on the event bus as a DiscussionThreadUpdatedEvent, which is then
                // used to replace the existing model. A better approach may be to not allow
                // sharing of the objects in an unpredictable manner by always cloning or copying
                // from them, or on the other extreme, having a central memory cache with
                // registered observers so that the objects are always shared.
                discussionPostsAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DiscussionThreadPostedEvent event) {
        DiscussionThread newThread = event.getDiscussionThread();
        // If a new post is created in this topic, insert it at the top of the list, after any pinned posts
        if (discussionTopic.containsThread(newThread)) {
            if (postsFilter == DiscussionPostsFilter.UNANSWERED &&
                    newThread.getType() != DiscussionThread.ThreadType.QUESTION) {
                return;
            }

            int i = 0;
            for (; i < discussionPostsAdapter.getCount(); ++i) {
                if (!discussionPostsAdapter.getItem(i).isPinned()) {
                    break;
                }
            }
            discussionPostsAdapter.insert(newThread, i);
            // move the ListView's scroll to that newly added post's position
            binding.discussionPostsListview.setSelection(i);
            // In case this is the first addition, we need to hide the no-item-view
            setScreenStateUponResult();
        }
    }

    @Override
    public void loadNextPage(@NonNull final InfiniteScrollUtils.PageLoadCallback<DiscussionThread> callback) {
        if (discussionTopic == null) {
            populatePostListRunnable = new Runnable() {
                @Override
                public void run() {
                    populatePostList(callback);
                }
            };
        } else {
            populatePostList(callback);
        }
    }

    private void clearListAndLoadFirstPage() {
        nextPage = 1;
        binding.discussionPostsListview.setVisibility(View.INVISIBLE);
        binding.centerMessageBox.setVisibility(View.GONE);
        controller.reset();
    }

    private void populatePostList(@NonNull final InfiniteScrollUtils.PageLoadCallback<DiscussionThread> callback) {
        if (getThreadListCall != null) {
            getThreadListCall.cancel();
        }

        final List<String> requestedFields = Collections.singletonList(
                DiscussionRequestFields.PROFILE_IMAGE.getQueryParamValue());
        if (!discussionTopic.isFollowingType()) {
            getThreadListCall = discussionService.getThreadList(courseData.getCourse().getId(),
                    getAllTopicIds(), postsFilter.getQueryParamValue(),
                    postsSort.getQueryParamValue(), nextPage, requestedFields);
        } else {
            getThreadListCall = discussionService.getFollowingThreadList(
                    courseData.getCourse().getId(), postsFilter.getQueryParamValue(),
                    postsSort.getQueryParamValue(), nextPage, requestedFields);
        }

        final Activity activity = getActivity();
        final boolean isRefreshingSilently = callback.isRefreshingSilently();
        getThreadListCall.enqueue(new ErrorHandlingCallback<Page<DiscussionThread>>(activity,
                // Initially we need to show the spinner at the center of the screen. After that,
                // the ListView will start showing a footer-based loading indicator.
                nextPage > 1 || isRefreshingSilently ? null :
                        new ProgressViewController(binding.loadingIndicator.loadingIndicator),
                // We only require the error to appear if the first server call fails
                nextPage == 1 ? errorNotification : null) {
            @Override
            protected void onResponse(@NonNull final Page<DiscussionThread> threadsPage) {
                if (getView() == null) return;
                ++nextPage;
                callback.onPageLoaded(threadsPage);

                if (discussionPostsAdapter.getCount() == 0) {
                    if (discussionTopic.isAllType()) {
                        setScreenStateUponError(EmptyQueryResultsFor.COURSE);
                    } else if (discussionTopic.isFollowingType()) {
                        setScreenStateUponError(EmptyQueryResultsFor.FOLLOWING);
                    } else {
                        setScreenStateUponError(EmptyQueryResultsFor.CATEGORY);
                    }
                } else {
                    setScreenStateUponResult();
                }
            }

            @Override
            public void onFailure(@NonNull final Call<Page<DiscussionThread>> call,
                                  @NonNull final Throwable error) {
                if (getView() == null || call.isCanceled()) return;
                // Don't display any error message if we're doing a silent
                // refresh, as that would be confusing to the user.
                if (!callback.isRefreshingSilently()) {
                    super.onFailure(call, error);
                }
                callback.onError();
                nextPage = 1;
            }
        });
    }

    private void setScreenStateUponError(@NonNull EmptyQueryResultsFor query) {
        String resultsText = "";
        boolean isAllPostsFilter = (postsFilter == DiscussionPostsFilter.ALL);
        switch (query) {
            case FOLLOWING:
                if (!isAllPostsFilter) {
                    resultsText = getString(R.string.forum_no_results_for_filtered_following);
                } else {
                    resultsText = getString(R.string.forum_no_results_for_following);
                }
                break;
            case CATEGORY:
                resultsText = getString(R.string.forum_no_results_in_category);
                break;
            case COURSE:
                resultsText = getString(R.string.forum_no_results_for_all_posts);
                break;
        }
        if (!isAllPostsFilter) {
            resultsText += " " + getString(R.string.forum_no_results_with_filter);
            binding.spinnersContainer.setVisibility(View.VISIBLE);
        } else {
            binding.spinnersContainer.setVisibility(View.GONE);
        }

        binding.centerMessageBox.setText(resultsText);
        binding.centerMessageBox.setVisibility(View.VISIBLE);
        binding.discussionPostsListview.setVisibility(View.INVISIBLE);
    }

    private void setScreenStateUponResult() {
        errorNotification.hideError();
        binding.centerMessageBox.setVisibility(View.GONE);
        binding.spinnersContainer.setVisibility(View.VISIBLE);
        binding.discussionPostsListview.setVisibility(View.VISIBLE);
    }

    @NonNull
    public List<String> getAllTopicIds() {
        if (discussionTopic.isAllType()) {
            return Collections.EMPTY_LIST;
        } else {
            final List<String> ids = new ArrayList<>();
            appendTopicIds(discussionTopic, ids);
            return ids;
        }
    }

    private void appendTopicIds(@NonNull DiscussionTopic dTopic, @NonNull List<String> ids) {
        String id = dTopic.getIdentifier();
        if (!TextUtils.isEmpty(id)) {
            ids.add(id);
        }
        for (DiscussionTopic child : dTopic.getChildren()) {
            appendTopicIds(child, ids);
        }
    }

    /**
     * Query server to check if discussions on this course are blacked out.
     */
    private void checkIfDiscussionsBlackedOut() {
        setCreateNewPostBtnVisibility(View.GONE);

        discussionService.getCourseDiscussionInfo(courseData.getCourse().getId())
                .enqueue(new ErrorHandlingCallback<CourseDiscussionInfo>(getContext(), (TaskProgressCallback) null) {
                    @Override
                    public void onFailure(@NonNull Call<CourseDiscussionInfo> call, @NonNull Throwable t) {
                        markAsBlackedOut(false);
                    }

                    @Override
                    protected void onResponse(@NonNull CourseDiscussionInfo discussionInfo) {
                        final Date today = new Date();
                        final List<TimePeriod> blackoutTimesList = discussionInfo.getBlackoutList();
                        for (TimePeriod timePeriod : blackoutTimesList) {
                            if (today.after(timePeriod.getStart()) &&
                                    today.before(timePeriod.getEnd())) {
                                markAsBlackedOut(true);
                                return;
                            }
                        }
                        markAsBlackedOut(false);
                    }

                    private void markAsBlackedOut(boolean isBlackedOut) {
                        courseData.setDiscussionBlackedOut(isBlackedOut);
                        binding.createNewItem.createNewItemLayout.setEnabled(!isBlackedOut);
                        setCreateNewPostBtnVisibility(View.VISIBLE);
                    }
                });
    }

    /**
     * Sets the visibility of Create New Post button.
     *
     * @param visibility The visibility to set.
     */
    private void setCreateNewPostBtnVisibility(int visibility) {
        if (discussionTopic != null) {
            binding.createNewItem.createNewItemLayout.setVisibility(visibility);
        } else {
            binding.createNewItem.createNewItemLayout.setVisibility(View.GONE);
        }
    }
}
