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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionCommentPostedEvent;
import org.edx.mobile.discussion.DiscussionPostsFilter;
import org.edx.mobile.discussion.DiscussionPostsSort;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionThreadPostedEvent;
import org.edx.mobile.discussion.DiscussionThreadUpdatedEvent;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.model.Page;
import org.edx.mobile.task.GetThreadListTask;
import org.edx.mobile.view.adapters.DiscussionPostsSpinnerAdapter;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;
import org.edx.mobile.view.common.MessageType;
import org.edx.mobile.view.common.TaskProcessCallback;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseDiscussionPostsThreadFragment extends CourseDiscussionPostsBaseFragment {
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

    @InjectExtra(value = Router.EXTRA_DISCUSSION_TOPIC, optional = true)
    private DiscussionTopic discussionTopic;

    private DiscussionPostsFilter postsFilter = DiscussionPostsFilter.ALL;
    private DiscussionPostsSort postsSort = DiscussionPostsSort.LAST_ACTIVITY_AT;

    private GetThreadListTask getThreadListTask;
    private int nextPage = 1;
    private int mSelectedItem = -1;

    private enum EmptyQueryResultsFor {
        FOLLOWING,
        CATEGORY,
        COURSE
    }

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
        Context context = getActivity();
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(createNewPostTextView,
                new IconDrawable(context, FontAwesomeIcons.fa_plus_circle)
                        .sizeRes(context, R.dimen.icon_view_standard_width_height)
                        .colorRes(context, R.color.edx_grayscale_neutral_white_t),
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
                                R.dimen.icon_view_standard_width_height);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onRestart() {
        if (postsSort == DiscussionPostsSort.LAST_ACTIVITY_AT && mSelectedItem != -1) {
            // Move the last viewed thread to the top
            discussionPostsAdapter.moveToTop(mSelectedItem);
            mSelectedItem = -1;
        }

        /*
        If the activity/fragment needs to be reinstantiated upon restoration,
        then in some cases the onRestart() callback maybe invoked before view
        initialization, and thus the controller might not be initialized, and
        therefore we need to guard this with a null check.
         */
        if (controller != null) {
            nextPage = 1;
            controller.resetSilently();
        }
    }

    @Override
    public void onItemClick(DiscussionThread thread, AdapterView<?> parent, View view,
                            int position, long id) {
        mSelectedItem = position;
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
            ((TaskProcessCallback) getActivity()).onMessage(MessageType.EMPTY, "");
            // And show the filter and sort spinners
            spinnersContainerLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void loadNextPage(@NonNull final InfiniteScrollUtils.PageLoadCallback<DiscussionThread> callback) {
        populatePostList(callback);
    }

    private void clearListAndLoadFirstPage() {
        nextPage = 1;
        discussionPostsListView.setVisibility(View.INVISIBLE);
        ((TaskProcessCallback) getActivity()).onMessage(MessageType.EMPTY, "");
        discussionPostsAdapter.setVoteCountsEnabled(postsSort == DiscussionPostsSort.VOTE_COUNT);
        controller.reset();
    }

    private void populatePostList(@NonNull final InfiniteScrollUtils.PageLoadCallback<DiscussionThread> callback) {
        if (getThreadListTask != null) {
            getThreadListTask.cancel(true);
        }

        getThreadListTask = new GetThreadListTask(getActivity(), courseData.getCourse().getId(),
                discussionTopic, postsFilter, postsSort, nextPage) {
            @Override
            public void onSuccess(Page<DiscussionThread> threadsPage) {
                ++nextPage;
                callback.onPageLoaded(threadsPage);
                if (discussionTopic.isAllType()) {
                    checkNoResultView(EmptyQueryResultsFor.COURSE);
                } else if (discussionTopic.isFollowingType()) {
                    checkNoResultView(EmptyQueryResultsFor.FOLLOWING);
                } else {
                    checkNoResultView(EmptyQueryResultsFor.CATEGORY);
                }
            }

            @Override
            protected void onException(Exception ex) {
                // Don't display any error message if we're doing a silent
                // refresh, as that would be confusing to the user.
                if (!callback.isRefreshingSilently()) {
                    super.onException(ex);
                }
                callback.onError();
                nextPage = 1;
            }
        };
        if (nextPage > 1 || callback.isRefreshingSilently()) {
            getThreadListTask.setProgressCallback(null);
        }
        getThreadListTask.execute();
    }

    private void checkNoResultView(EmptyQueryResultsFor query) {
        Activity activity = getActivity();
        if (activity instanceof TaskProcessCallback) {
            if (discussionPostsAdapter.getCount() == 0) {
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
                }
                ((TaskProcessCallback) activity).onMessage(MessageType.ERROR, resultsText);
            } else {
                ((TaskProcessCallback) activity).onMessage(MessageType.EMPTY, "");
                spinnersContainerLayout.setVisibility(View.VISIBLE);
                discussionPostsListView.setVisibility(View.VISIBLE);
            }
        }
    }

}
