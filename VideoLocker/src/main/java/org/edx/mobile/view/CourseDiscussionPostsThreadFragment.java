package org.edx.mobile.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionCommentPostedEvent;
import org.edx.mobile.discussion.DiscussionPostsFilter;
import org.edx.mobile.discussion.DiscussionPostsSort;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionThreadFollowedEvent;
import org.edx.mobile.discussion.DiscussionThreadPostedEvent;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.discussion.TopicThreads;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.task.GetFollowingThreadListTask;
import org.edx.mobile.task.GetThreadListTask;
import org.edx.mobile.view.common.MessageType;
import org.edx.mobile.view.common.TaskProcessCallback;
import org.edx.mobile.view.custom.popup.menu.PopupMenu;

import java.util.HashMap;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseDiscussionPostsThreadFragment extends CourseDiscussionPostsBaseFragment {

    @InjectView(R.id.discussion_posts_filter_layout)
    RelativeLayout discussionPostsFilterLayout;

    @InjectView(R.id.discussion_posts_filter_text_view)
    TextView discussionPostsFilterTextView;

    @InjectView(R.id.discussion_posts_sort_layout)
    RelativeLayout discussionPostsSortLayout;

    @InjectView(R.id.discussion_posts_sort_text_view)
    TextView discussionPostsSortTextView;

    @InjectView(R.id.create_new_item_text_view)
    TextView createNewPostTextView;

    @InjectView(R.id.create_new_item_relative_layout)
    RelativeLayout createNewPostRelativeLayout;

    @InjectExtra(value = Router.EXTRA_DISCUSSION_TOPIC, optional = true)
    private DiscussionTopic discussionTopic;

    DiscussionPostsFilter postsFilter = DiscussionPostsFilter.All;
    DiscussionPostsSort postsSort = DiscussionPostsSort.None;

    private HashMap<Integer, DiscussionPostsFilter> filterOptions = new HashMap<>();
    private HashMap<Integer, DiscussionPostsSort> sortOptions = new HashMap<>();

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

        filterOptions.put(R.id.filter_item_all_posts, DiscussionPostsFilter.All);
        filterOptions.put(R.id.filter_item_unread_posts, DiscussionPostsFilter.Unread);
        filterOptions.put(R.id.filter_item_unanswered_posts, DiscussionPostsFilter.Unanswered);

        sortOptions.put(R.id.sort_item_recent_activity, DiscussionPostsSort.None);
        sortOptions.put(R.id.sort_item_most_activity, DiscussionPostsSort.LastActivityAt);
        sortOptions.put(R.id.sort_item_most_votes, DiscussionPostsSort.VoteCount);

        return inflater.inflate(R.layout.fragment_discussion_thread_posts, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (this.isFollowingTopics()) {
            createNewPostRelativeLayout.setVisibility(View.GONE);
        } else {
            createNewPostTextView.setText(R.string.discussion_post_create_new_post);

            createNewPostRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    router.showCourseDiscussionAddPost(getActivity(), discussionTopic, courseData);
                }
            });
        }

        // TODO: Add some UI polish to make the popups more closely match the wireframes
        createFilterPopupMenu();
        createSortPopupMenu();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void createFilterPopupMenu() {
        final PopupMenu filterPopup = new PopupMenu(getActivity(), discussionPostsFilterLayout);
        filterPopup.inflate(R.menu.discussion_posts_filter_menu);
        filterPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(android.view.MenuItem item) {
                int itemId = item.getItemId();
                CharSequence title = item.getTitle();

                if (postsFilter == filterOptions.get(itemId)) {
                    return false;
                }
                postsFilter = filterOptions.get(itemId);

                discussionPostsFilterTextView.setText(title);
                populateThreadList(true);

                return false;
            }
        });

        discussionPostsFilterLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterPopup.show();
            }
        });
    }

    private void createSortPopupMenu() {
        final PopupMenu sortPopup = new PopupMenu(getActivity(), discussionPostsSortLayout);
        sortPopup.inflate(R.menu.discussion_posts_sort_menu);
        sortPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(android.view.MenuItem item) {
                int itemId = item.getItemId();
                CharSequence title = item.getTitle();

                if (postsSort == sortOptions.get(itemId)) {
                    return false;
                }
                postsSort = sortOptions.get(itemId);

                discussionPostsSortTextView.setText(title);
                populateThreadList(true);

                return false;
            }
        });

        discussionPostsSortLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortPopup.show();
            }
        });
    }

    private boolean isFollowingTopics() {
        return DiscussionTopic.FOLLOWING_TOPICS.equalsIgnoreCase(discussionTopic.getName());
    }

    public void onEventMainThread(DiscussionThreadFollowedEvent event) {
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
        if (!isFollowingTopics() && event.getDiscussionThread().getTopicId().equalsIgnoreCase(discussionTopic.getIdentifier())) {
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

        getThreadListTask = new GetThreadListTask(getActivity(), courseData.getCourse().getId(), discussionTopic.getIdentifier(),
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
