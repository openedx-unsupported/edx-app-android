package org.edx.mobile.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.discussion.TopicThreads;

import org.edx.mobile.R;
import org.edx.mobile.task.SearchThreadListTask;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.common.MessageType;
import org.edx.mobile.view.common.TaskProcessCallback;

import roboguice.inject.InjectExtra;

public class CourseDiscussionPostsSearchFragment extends CourseDiscussionPostsBaseFragment {

    @InjectExtra(value = Router.EXTRA_SEARCH_QUERY, optional = true)
    private String searchQuery;
    private SearchThreadListTask searchThreadListTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_search_posts, container, false);
    }


    @Override
    protected void populateThreadList(final boolean refreshView) {

        if ( searchThreadListTask != null ){
            searchThreadListTask.cancel(true);
        }
        searchThreadListTask = new SearchThreadListTask(getActivity(), courseData.getCourse().getId(), searchQuery, discussionPostsAdapter.getPagination()) {
            @Override
            public void onSuccess(TopicThreads topicThreads) {
                if ( refreshView ){
                    discussionPostsAdapter.setItems(null);
                }
                boolean hasMore = topicThreads.next != null && topicThreads.next.length() > 0;
                discussionPostsAdapter.addPage(topicThreads.getResults(), hasMore);
                refreshListViewOnDataChange();
               // discussionPostsAdapter.notifyDataSetChanged();
                if ( discussionPostsAdapter.getCount() == 0 ){
                    Activity activity = getActivity();
                    if ( activity instanceof  TaskProcessCallback ){
                        String escapedTitle = TextUtils.htmlEncode(searchQuery);
                        String resultsText = ResourceUtil.getFormattedString(
                                getContext().getResources(),
                                R.string.forum_no_results_for_search_query,
                                "search_query",
                                escapedTitle
                        ).toString();
                       // CharSequence styledResults = Html.fromHtml(resultsText);
                        ((TaskProcessCallback)activity).onMessage(MessageType.ERROR, resultsText);
                    }
                }
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
                //  hideProgress();

            }
        };

        searchThreadListTask.execute();

    }
}
