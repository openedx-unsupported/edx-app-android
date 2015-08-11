package org.edx.mobile.view.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.APICallback;
import com.qualcomm.qlearn.sdk.discussion.DiscussionAPI;
import com.qualcomm.qlearn.sdk.discussion.DiscussionTopic;
import com.qualcomm.qlearn.sdk.discussion.TopicThreads;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.view.Router;
import org.edx.mobile.view.adapters.DiscussionPostsAdapter;
import org.edx.mobile.view.custom.popup.menu.PopupMenu;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseDiscussionPostsFragment extends RoboFragment {

    @InjectView(R.id.discussion_posts_refine_layout)
    RelativeLayout discussionPostsRefineLayout;

    @InjectView(R.id.discussion_posts_filter_layout)
    RelativeLayout discussionPostsFilterLayout;

    @InjectView(R.id.discussion_posts_filter_text_view)
    TextView discussionPostsFilterTextView;

    @InjectView(R.id.discussion_posts_sort_layout)
    RelativeLayout discussionPostsSortLayout;

    @InjectView(R.id.discussion_posts_sort_text_view)
    TextView discussionPostsSortTextView;

    @InjectView(R.id.discussion_posts_listview)
    private ListView discussionPostsListView;

    @InjectExtra(Router.EXTRA_COURSE_DATA)
    private EnrolledCoursesResponse courseData;

    @InjectExtra(value = Router.EXTRA_SEARCH_QUERY, optional = true)
    private String searchQuery;

    @InjectExtra(value = Router.EXTRA_DISCUSSION_TOPIC, optional = true)
    private DiscussionTopic discussionTopic;

    @Inject
    DiscussionPostsAdapter discussionPostsAdapter;

    @Inject
    DiscussionAPI discussionAPI;

    DiscussionAPI.DiscussionPostsFilter postsFilter = DiscussionAPI.DiscussionPostsFilter.All;
    DiscussionAPI.DiscussionPostsSort postsSort = DiscussionAPI.DiscussionPostsSort.None;

    private final Logger logger = new Logger(getClass().getName());

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_posts, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        discussionPostsListView.setAdapter(discussionPostsAdapter);

        if (searchQuery != null) {
            setIsFilterSortVisible(false);
            populateListFromSearch();
            return;
        }

        if (discussionTopic != null) {
            setIsFilterSortVisible(true);
            populateListFromThread();
            createFilterPopupMenu();
            createSortPopupMenu();
        }
    }

    private void createFilterPopupMenu() {
        final PopupMenu filterPopup = new PopupMenu(getActivity(), discussionPostsFilterLayout);
        filterPopup.inflate(R.menu.discussion_posts_filter_menu);
        filterPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                CharSequence title = item.getTitle();

                switch (itemId) {
                    case R.id.filter_item_all_posts:
                        if (postsFilter == DiscussionAPI.DiscussionPostsFilter.All) {
                            return false;
                        }
                        postsFilter = DiscussionAPI.DiscussionPostsFilter.All;
                        break;

                    case R.id.filter_item_unanswered_posts:
                        if (postsFilter == DiscussionAPI.DiscussionPostsFilter.Unanswered) {
                            return false;
                        }
                        postsFilter = DiscussionAPI.DiscussionPostsFilter.Unanswered;
                        break;

                    case R.id.filter_item_unread_posts:
                        if (postsFilter == DiscussionAPI.DiscussionPostsFilter.Unread) {
                            return false;
                        }
                        postsFilter = DiscussionAPI.DiscussionPostsFilter.Unread;
                        break;
                }

                discussionPostsFilterTextView.setText(title);
                populateListFromThread();

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
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                CharSequence title = item.getTitle();

                switch (itemId) {
                    case R.id.sort_item_recent_activity:
                        if (postsSort == DiscussionAPI.DiscussionPostsSort.None) {
                            return false;
                        }
                        postsSort = DiscussionAPI.DiscussionPostsSort.None;
                        break;

                    case R.id.sort_item_most_activity:
                        if (postsSort == DiscussionAPI.DiscussionPostsSort.LastActivityAt) {
                            return false;
                        }
                        postsSort = DiscussionAPI.DiscussionPostsSort.LastActivityAt;
                        break;

                    case R.id.sort_item_most_votes:
                        if (postsSort == DiscussionAPI.DiscussionPostsSort.VoteCount) {
                            return false;
                        }
                        postsSort = DiscussionAPI.DiscussionPostsSort.VoteCount;
                        break;
                }

                discussionPostsSortTextView.setText(title);
                populateListFromThread();

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

    private void populateListFromThread() {
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

    private void populateListFromSearch() {
        discussionAPI.searchThreadList(courseData.getCourse().getId(), searchQuery,
                new APICallback<TopicThreads>() {
                    @Override
                    public void success(TopicThreads topicThreads) {
                        discussionPostsAdapter.setItems(topicThreads.getResults());
                    }

                    @Override
                    public void failure(Exception e) {

                    }
                });
    }

    private void setIsFilterSortVisible(boolean isVisible) {
        int refineLayoutVisibility = isVisible ? View.VISIBLE : View.GONE;
        discussionPostsRefineLayout.setVisibility(refineLayoutVisibility);
    }
}
