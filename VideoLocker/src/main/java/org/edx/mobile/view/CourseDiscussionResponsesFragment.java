package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionCommentPostedEvent;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.ThreadComments;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.task.GetCommentListTask;
import org.edx.mobile.view.adapters.CourseDiscussionResponsesAdapter;
import org.edx.mobile.view.adapters.IPagination;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;

public class CourseDiscussionResponsesFragment extends RoboFragment implements CourseDiscussionResponsesAdapter.Listener {

    @Inject
    LinearLayoutManager linearLayoutManager;

    @InjectExtra(Router.EXTRA_DISCUSSION_THREAD)
    DiscussionThread discussionThread;

    @InjectExtra(value = Router.EXTRA_COURSE_DATA, optional = true)
    EnrolledCoursesResponse courseData;

    @Inject
    Router router;

    CourseDiscussionResponsesAdapter courseDiscussionResponsesAdapter;

    private GetCommentListTask getCommentListTask;

    public static class ViewHolder {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_responses, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final RecyclerView discussionResponsesRecyclerView = (RecyclerView) view.findViewById(R.id.discussion_responses_recycler_view);
        final TextView addResponseTextView = (TextView) view.findViewById(R.id.create_new_item_text_view);
        final ViewGroup addResponseLayout = (ViewGroup) view.findViewById(R.id.create_new_item_layout);

        discussionResponsesRecyclerView.setLayoutManager(linearLayoutManager);

        // Using application context to prevent activity leak since adapter is retained across config changes
        courseDiscussionResponsesAdapter = new CourseDiscussionResponsesAdapter(
                getActivity().getApplicationContext(), this, discussionThread);
        discussionResponsesRecyclerView.setAdapter(courseDiscussionResponsesAdapter);

        getCommentList(true);

        addResponseTextView.setText(R.string.discussion_responses_add_response_button);

        addResponseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                router.showCourseDiscussionAddResponse(getActivity(), discussionThread);
            }
        });
    }

    protected void getCommentList(final boolean refresh) {
        if (getCommentListTask != null) {
            getCommentListTask.cancel(true);
        }
        getCommentListTask = new GetCommentListTask(getActivity(),
                discussionThread.getIdentifier(),
                courseDiscussionResponsesAdapter.getPagination()) {
            @Override
            public void onSuccess(ThreadComments threadComments) {
                if (threadComments == null) {
                    logger.debug("GetCommentListTask returns null onSuccess");
                    return;// should not happen?
                }
                if (refresh) {
                    //it clear up details and reset pagination
                    courseDiscussionResponsesAdapter.setDiscussionThread(discussionThread);
                }
                boolean hasMore = threadComments.next != null && threadComments.next.length() > 0;
                courseDiscussionResponsesAdapter.addPage(threadComments.getResults(), hasMore);
                courseDiscussionResponsesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
                //  hideProgress();
            }
        };
        getCommentListTask.execute();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DiscussionCommentPostedEvent event) {
        if (discussionThread.containsComment(event.getComment())) {
            discussionThread.incrementCommentCount();
            courseDiscussionResponsesAdapter.notifyDataSetChanged();
            getCommentList(true);
        }
    }

    /**
     * callback from CourseDiscussionResponsesAdapter
     */
    @Override
    public void loadMoreRecord(@NonNull IPagination pagination) {
        getCommentList(false);
    }

    @Override
    public void onClickAuthor(@NonNull String username) {
        router.showUserProfile(getActivity(), username);
    }

    @Override
    public void onClickAddComment(@NonNull DiscussionComment comment) {
        router.showCourseDiscussionAddComment(getActivity(), comment);
    }

    @Override
    public void onClickViewComments(@NonNull DiscussionComment comment) {
        router.showCourseDiscussionComments(getActivity(), comment);
    }
}
