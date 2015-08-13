package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.APICallback;
import com.qualcomm.qlearn.sdk.discussion.DiscussionAPI;
import com.qualcomm.qlearn.sdk.discussion.DiscussionPostsFilter;
import com.qualcomm.qlearn.sdk.discussion.DiscussionPostsSort;
import com.qualcomm.qlearn.sdk.discussion.DiscussionTopic;
import com.qualcomm.qlearn.sdk.discussion.TopicThreads;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.view.custom.popup.menu.PopupMenu;

import java.util.HashMap;

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

    @InjectExtra(value = Router.EXTRA_DISCUSSION_TOPIC, optional = true)
    private DiscussionTopic discussionTopic;

    @Inject
    DiscussionAPI discussionAPI;

    DiscussionPostsFilter postsFilter = DiscussionPostsFilter.All;
    DiscussionPostsSort postsSort = DiscussionPostsSort.None;

    private HashMap<Integer, DiscussionPostsFilter> filterOptions = new HashMap<>();
    private HashMap<Integer, DiscussionPostsSort> sortOptions = new HashMap<>();

    private final Logger logger = new Logger(getClass().getName());

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

        // TODO: Add some UI polish to make the popups more closely match the wireframes
        createFilterPopupMenu();
        createSortPopupMenu();
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
                populateThreadList();

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
                populateThreadList();

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

    @Override
    protected void populateThreadList() {
        // TODO: Add a progress indicator (spinner?) while waiting for callback
        discussionAPI.getThreadList(courseData.getCourse().getId(), discussionTopic.getIdentifier(),
                postsFilter,
                postsSort,
                new APICallback<TopicThreads>() {
                    @Override
                    public void success(TopicThreads topicThreads) {
                        if (topicThreads.getResults() != null && topicThreads.getResults().isEmpty()) {
                            // TODO: Add text to listview to indicate if there are no results
                        }
                        discussionPostsAdapter.setItems(topicThreads.getResults());
                    }

                    @Override
                    public void failure(Exception e) {
                        // TODO: Handle failure gracefully
                    }
                });
    }

}
