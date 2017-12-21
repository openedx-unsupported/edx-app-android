package org.edx.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.TextViewCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.inject.Inject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
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
import org.edx.mobile.view.adapters.DiscussionPostsSpinnerAdapter;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;
import org.edx.mobile.view.common.TaskProgressCallback;
import org.edx.mobile.view.common.TaskProgressCallback.ProgressViewController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseDiscussionPostsThreadFragment extends CourseDiscussionPostsBaseFragment {
    public static final String ARG_DISCUSSION_HAS_TOPIC_NAME = "discussion_has_topic_name";

    @InjectView(R.id.spinners_container)
    private ViewGroup spinnersContainerLayout;

    @InjectView(R.id.discussion_posts_filter_spinner)
    private Spinner discussionPostsFilterSpinner;

    @InjectView(R.id.discussion_posts_sort_spinner)
    private Spinner discussionPostsSortSpinner;

    @InjectView(R.id.create_new_item_text_view)
    private TextView createNewPostTextView;

    @InjectView(R.id.create_new_item_layout)
    private ViewGroup createNewPostLayout;

    @InjectView(R.id.center_message_box)
    private TextView centerMessageBox;

    @InjectView(R.id.loading_indicator)
    private ProgressBar loadingIndicator;

    @InjectExtra(value = Router.EXTRA_DISCUSSION_TOPIC, optional = true)
    private DiscussionTopic discussionTopic;

    @Inject
    private DiscussionService discussionService;

    private FullScreenErrorNotification errorNotification;

    private DiscussionPostsFilter postsFilter = DiscussionPostsFilter.ALL;
    private DiscussionPostsSort postsSort = DiscussionPostsSort.LAST_ACTIVITY_AT;

    private Call<Page<DiscussionThread>> getThreadListCall;

    /**
     * Runnable for deferring the fetching of threads for a topic, until we
     * have fetched the {@link #discussionTopic} object.
     */
    @Nullable
    private Runnable populatePostListRunnable;

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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_discussion_thread_posts, container, false);
        // Initializing errorNotification here, so that its non-null value can be used in the parent class's `onViewCreated` callback
        errorNotification = new FullScreenErrorNotification(view.findViewById(R.id.content));
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        checkIfDiscussionsBlackedOut();

        if (discussionTopic == null) {
            // Either we are coming from a deep link or courseware's inline discussion
            fetchDiscussionTopic();
        } else {
            getActivity().setTitle(discussionTopic.getName());
        }

        createNewPostTextView.setText(R.string.discussion_post_create_new_post);
        Context context = getActivity();
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(createNewPostTextView,
                new IconDrawable(context, FontAwesomeIcons.fa_plus_circle)
                        .sizeRes(context, R.dimen.small_icon_size)
                        .colorRes(context, R.color.white),
                null, null, null
        );
        createNewPostLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                router.showCourseDiscussionAddPost(getActivity(), discussionTopic, courseData);
            }
        });

        discussionPostsFilterSpinner.setAdapter(new DiscussionPostsSpinnerAdapter(
                discussionPostsFilterSpinner, DiscussionPostsFilter.values(),
                FontAwesomeIcons.fa_filter));
        discussionPostsSortSpinner.setAdapter(new DiscussionPostsSpinnerAdapter(
                discussionPostsSortSpinner, DiscussionPostsSort.values(),
                // Since we can't define IconDrawable in XML resources, we'll have to define
                // this constructed dynamically in code. This is far more efficient than the
                // alternative option of defining multiple IconView items in the layout.
                new DiscussionPostsSpinnerAdapter.IconDrawableFactory() {
                    @Override
                    @NonNull
                    public Drawable createIcon() {
                        Context context = getActivity();
                        LayerDrawable layeredIcon = new LayerDrawable(new Drawable[]{
                                new IconDrawable(context, FontAwesomeIcons.fa_long_arrow_up)
                                        .colorRes(context, R.color.edx_brand_primary_base),
                                new IconDrawable(context, FontAwesomeIcons.fa_long_arrow_down)
                                        .colorRes(context, R.color.edx_brand_primary_base)
                        });
                        Resources resources = context.getResources();
                        final int width = resources.getDimensionPixelSize(
                                R.dimen.small_icon_size);
                        final int verticalPadding = resources.getDimensionPixelSize(
                                R.dimen.discussion_posts_filter_popup_icon_margin);
                        final int height = width + verticalPadding;
                        final float halfWidth = width / 2f;
                        final int leftIconWidth = (int) Math.ceil(halfWidth);
                        final int rightIconWidth = (int) halfWidth;
                        layeredIcon.setLayerInset(0, 0, 0, rightIconWidth, verticalPadding);
                        layeredIcon.setLayerInset(1, leftIconWidth, verticalPadding, 0, 0);
                        layeredIcon.setBounds(0, 0, width, height);
                        return layeredIcon;
                    }
                }));

        discussionPostsFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        discussionPostsSortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
    }

    private void fetchDiscussionTopic() {
        String topicId = getArguments().getString(Router.EXTRA_DISCUSSION_TOPIC_ID);
        final Activity activity = getActivity();
        discussionService.getSpecificCourseTopics(courseData.getCourse().getId(),
                Collections.singletonList(topicId))
                .enqueue(new ErrorHandlingCallback<CourseTopics>(activity,
                        new ProgressViewController(loadingIndicator), errorNotification) {
                    @Override
                    protected void onResponse(@NonNull final CourseTopics courseTopics) {
                        discussionTopic = courseTopics.getCoursewareTopics().get(0).getChildren().get(0);
                        if (activity != null &&
                                !getArguments().getBoolean(ARG_DISCUSSION_HAS_TOPIC_NAME)) {
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
                    }
                });
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
            discussionPostsListView.setSelection(i);
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
        discussionPostsListView.setVisibility(View.INVISIBLE);
        centerMessageBox.setVisibility(View.GONE);
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
                        new ProgressViewController(loadingIndicator),
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
            spinnersContainerLayout.setVisibility(View.VISIBLE);
        } else {
            spinnersContainerLayout.setVisibility(View.GONE);
        }

        centerMessageBox.setText(resultsText);
        centerMessageBox.setVisibility(View.VISIBLE);
        discussionPostsListView.setVisibility(View.INVISIBLE);
    }

    private void setScreenStateUponResult() {
        errorNotification.hideError();
        centerMessageBox.setVisibility(View.GONE);
        spinnersContainerLayout.setVisibility(View.VISIBLE);
        discussionPostsListView.setVisibility(View.VISIBLE);
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
                        createNewPostLayout.setEnabled(!isBlackedOut);
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
            createNewPostLayout.setVisibility(visibility);
        } else {
            createNewPostLayout.setVisibility(View.GONE);
        }
    }
}
