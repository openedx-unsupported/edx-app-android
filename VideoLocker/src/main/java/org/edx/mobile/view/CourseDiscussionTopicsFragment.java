package org.edx.mobile.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.discussion.CourseTopics;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.discussion.DiscussionTopicDepth;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.task.GetTopicListTask;
import org.edx.mobile.view.adapters.DiscussionTopicsAdapter;

import java.util.ArrayList;
import java.util.List;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseDiscussionTopicsFragment extends RoboFragment {

    @InjectView(R.id.discussion_topics_searchview)
    SearchView discussionTopicsSearchView;

    @InjectView(R.id.discussion_topics_listview)
    ListView discussionTopicsListView;


    @InjectExtra(Router.EXTRA_COURSE_DATA)
    private EnrolledCoursesResponse courseData;


    @Inject
    DiscussionTopicsAdapter discussionTopicsAdapter;

    @Inject
    Router router;

    GetTopicListTask getTopicListTask;

    private static final Logger logger = new Logger(CourseDiscussionTopicsFragment.class.getName());


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_topics, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final LayoutInflater inflater = LayoutInflater.from(getActivity());

        // Add "All posts" item
        {
            final TextView header = (TextView) inflater.inflate(R.layout.row_discussion_topic, discussionTopicsListView, false);
            header.setText(R.string.discussion_posts_filter_all_posts);

            final DiscussionTopic discussionTopic = new DiscussionTopic();
            discussionTopic.setIdentifier(DiscussionTopic.ALL_TOPICS_ID);
            discussionTopic.setName(getString(R.string.discussion_posts_filter_all_posts));
            discussionTopicsListView.addHeaderView(header, new DiscussionTopicDepth(discussionTopic, 0, true), true);
        }

        // Add "Posts I'm following" item
        {
            final TextView header = (TextView) inflater.inflate(R.layout.row_discussion_topic, discussionTopicsListView, false);
            header.setText(R.string.forum_post_i_am_following);
            Context context = getActivity();
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(header,
                    new IconDrawable(context, FontAwesomeIcons.fa_star)
                            .colorRes(context, R.color.edx_grayscale_neutral_dark)
                            .sizeRes(context, R.dimen.edx_base),
                    null, null, null);
            final DiscussionTopic discussionTopic = new DiscussionTopic();
            discussionTopic.setIdentifier(DiscussionTopic.FOLLOWING_TOPICS_ID);
            discussionTopic.setName(getString(R.string.forum_post_i_am_following));
            discussionTopicsListView.addHeaderView(header, new DiscussionTopicDepth(discussionTopic, 0, true), true);
        }

        discussionTopicsListView.setAdapter(discussionTopicsAdapter);

        discussionTopicsSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query == null || query.trim().length() == 0)
                    return false;

                router.showCourseDiscussionPostsForSearchQuery(getActivity(), query, courseData);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        discussionTopicsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                router.showCourseDiscussionPostsForDiscussionTopic(
                        getActivity(),
                        ((DiscussionTopicDepth) parent.getItemAtPosition(position)).getDiscussionTopic(),
                        courseData);
            }
        });

        // TODO: Find a better way to hide the keyboard AND take the focus away from the SearchView
        discussionTopicsSearchView.requestFocus();
        discussionTopicsSearchView.clearFocus();

        getTopicList();

    }

    private void getTopicList() {
        if (getTopicListTask != null) {
            getTopicListTask.cancel(true);
        }
        getTopicListTask = new GetTopicListTask(getActivity(), courseData.getCourse().getId()) {
            @Override
            public void onSuccess(CourseTopics courseTopics) {
                if (courseTopics != null) {
                    logger.debug("GetTopicListTask success=" + courseTopics);
                    //  hideProgress();
                    ArrayList<DiscussionTopic> allTopics = new ArrayList<>();
                    allTopics.addAll(courseTopics.getCoursewareTopics());
                    allTopics.addAll(courseTopics.getNonCoursewareTopics());

                    List<DiscussionTopicDepth> allTopicsWithDepth = DiscussionTopicDepth.createFromDiscussionTopics(allTopics);
                    discussionTopicsAdapter.setItems(allTopicsWithDepth);
                    discussionTopicsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
                //  hideProgress();
            }
        };
        getTopicListTask.execute();
    }
}