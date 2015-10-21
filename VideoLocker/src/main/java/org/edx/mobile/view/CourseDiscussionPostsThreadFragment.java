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
import android.util.FloatMath;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionCommentPostedEvent;
import org.edx.mobile.discussion.DiscussionPostsFilter;
import org.edx.mobile.discussion.DiscussionPostsSort;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionThreadPostedEvent;
import org.edx.mobile.discussion.DiscussionThreadUpdatedEvent;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.discussion.TopicThreads;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.task.GetFollowingThreadListTask;
import org.edx.mobile.task.GetThreadListTask;
import org.edx.mobile.third_party.iconify.IconDrawable;
import org.edx.mobile.third_party.iconify.Iconify;
import org.edx.mobile.view.adapters.DiscussionPostsSpinnerAdapter;
import org.edx.mobile.view.common.MessageType;
import org.edx.mobile.view.common.TaskProcessCallback;

import java.util.Collections;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseDiscussionPostsThreadFragment extends CourseDiscussionPostsBaseFragment {

    @InjectView(R.id.discussion_posts_filter_spinner)
    Spinner discussionPostsFilterSpinner;

    @InjectView(R.id.discussion_posts_sort_spinner)
    Spinner discussionPostsSortSpinner;

    @InjectView(R.id.create_new_item_text_view)
    private TextView createNewPostTextView;

    @InjectView(R.id.create_new_item_layout)
    private ViewGroup createNewPostLayout;

    @InjectExtra(value = Router.EXTRA_DISCUSSION_TOPIC, optional = true)
    private DiscussionTopic discussionTopic;

    private DiscussionPostsFilter postsFilter = DiscussionPostsFilter.ALL;
    private DiscussionPostsSort postsSort = DiscussionPostsSort.NONE;

    private final Logger logger = new Logger(getClass().getName());

    private GetThreadListTask getThreadListTask;
    private GetFollowingThreadListTask getFollowingThreadListTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_thread_posts, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        createNewPostTextView.setText(R.string.discussion_post_create_new_post);
        TextViewCompat.setCompoundDrawablesRelative(createNewPostTextView,
                new IconDrawable(getActivity(), Iconify.IconValue.fa_plus_circle)
                        .sizeRes(R.dimen.icon_view_standard_width_height)
                        .colorRes(R.color.edx_grayscale_neutral_white_t),
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
                Iconify.IconValue.fa_filter));
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
                                new IconDrawable(context, Iconify.IconValue.fa_long_arrow_up)
                                        .colorRes(R.color.edx_brand_primary_base),
                                new IconDrawable(context, Iconify.IconValue.fa_long_arrow_down)
                                        .colorRes(R.color.edx_brand_primary_base)
                        });
                        Resources resources = context.getResources();
                        final int width = resources.getDimensionPixelSize(
                                R.dimen.icon_view_standard_width_height);
                        final int verticalPadding = resources.getDimensionPixelSize(
                                R.dimen.discussion_posts_filter_popup_icon_margin);
                        final int height = width + verticalPadding;
                        final float halfWidth = width / 2f;
                        final int leftIconWidth = (int) FloatMath.ceil(halfWidth);
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
                    populateThreadList(true);
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
                    populateThreadList(true);
                }
            }

            @Override
            public void onNothingSelected(@NonNull AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private boolean isAllTopics() {
        return DiscussionTopic.ALL_TOPICS_ID.equals(discussionTopic.getIdentifier());
    }

    private boolean isFollowingTopics() {
        return DiscussionTopic.FOLLOWING_TOPICS_ID.equals(discussionTopic.getIdentifier());
    }

    public void onEventMainThread(DiscussionThreadUpdatedEvent event) {
        // If a listed thread's following status has changed, we need to replace it to show/hide the "following" label
        for (int i = 0; i < discussionPostsAdapter.getCount(); ++i) {
            if (discussionPostsAdapter.getItem(i).hasSameId(event.getDiscussionThread())) {
                discussionPostsAdapter.replace(event.getDiscussionThread(), i);
                break;
            }
        }
    }

    public void onEventMainThread(DiscussionCommentPostedEvent event) {
        // If a new comment was posted in a listed thread, increment its comment count
        for (int i = 0; i < discussionPostsAdapter.getCount(); ++i) {
            final DiscussionThread discussionThread = discussionPostsAdapter.getItem(i);
            if (discussionThread.containsComment(event.getComment())) {
                discussionThread.incrementCommentCount();
                discussionPostsAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    public void onEventMainThread(DiscussionThreadPostedEvent event) {
        // If a new post is created in this topic, insert it at the top of the list, after any pinned posts
        if (!isFollowingTopics() && !isAllTopics() && discussionTopic.containsThread(event.getDiscussionThread())) {
            int i = 0;
            for (; i < discussionPostsAdapter.getCount(); ++i) {
                if (!discussionPostsAdapter.getItem(i).isPinned()) {
                    break;
                }
            }
            discussionPostsAdapter.insert(event.getDiscussionThread(), i);
        }
    }

    @Override
    protected void populateThreadList(boolean refreshView) {

        if (refreshView) {
            discussionPostsAdapter.clear();
        }
        if (isFollowingTopics()) {
            populateFollowingThreadList();
        } else {
            populatePostList();
        }
    }


    private void populateFollowingThreadList() {
        if (getFollowingThreadListTask != null) {
            getFollowingThreadListTask.cancel(true);
        }

        getFollowingThreadListTask = new GetFollowingThreadListTask(getActivity(), courseData.getCourse().getId(), postsFilter,
                postsSort, discussionPostsAdapter.getPagination()) {
            @Override
            public void onSuccess(TopicThreads topicThreads) {
                if (topicThreads != null) {
                    logger.debug("registration success=" + topicThreads);
                    boolean hasMore = topicThreads.next != null && topicThreads.next.length() > 0;
                    discussionPostsAdapter.addPage(topicThreads.getResults(), hasMore);
                    refreshListViewOnDataChange();
                }

                discussionPostsAdapter.setVoteCountsEnabled(postsSort == DiscussionPostsSort.VOTE_COUNT);
                discussionPostsAdapter.notifyDataSetChanged();
                checkNoResultView();
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
                //  hideProgress();
            }
        };
        getFollowingThreadListTask.execute();
    }

    private void populatePostList() {
        if (getThreadListTask != null) {
            getThreadListTask.cancel(true);
        }

        getThreadListTask = new GetThreadListTask(
                getActivity(),
                courseData.getCourse().getId(),
                isAllTopics() ? Collections.EMPTY_LIST : discussionTopic.getAllTopicIds(),
                postsFilter,
                postsSort,
                discussionPostsAdapter.getPagination()
        ) {
            @Override
            public void onSuccess(TopicThreads topicThreads) {
                if (topicThreads != null) {
                    logger.debug("registration success=" + topicThreads);
                    boolean hasMore = topicThreads.next != null && topicThreads.next.length() > 0;
                    discussionPostsAdapter.addPage(topicThreads.getResults(), hasMore);
                    refreshListViewOnDataChange();
                }

                discussionPostsAdapter.setVoteCountsEnabled(postsSort == DiscussionPostsSort.VOTE_COUNT);
                discussionPostsAdapter.notifyDataSetChanged();
                checkNoResultView();
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
                //  hideProgress();
            }
        };
        getThreadListTask.execute();
    }

    private void checkNoResultView() {
        Activity activity = getActivity();
        if (activity instanceof TaskProcessCallback) {
            if (discussionPostsAdapter.getCount() == 0) {
                String resultsText = getActivity().getResources().getString(R.string.forum_empty);
                ((TaskProcessCallback) activity).onMessage(MessageType.ERROR, resultsText);
            } else {
                ((TaskProcessCallback) activity).onMessage(MessageType.EMPTY, "");
            }
        }
    }
}
