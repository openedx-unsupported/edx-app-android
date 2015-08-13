package org.edx.mobile.view.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qualcomm.qlearn.sdk.discussion.DiscussionComment;
import com.qualcomm.qlearn.sdk.discussion.DiscussionThread;

import org.edx.mobile.R;
import org.edx.mobile.view.custom.ETextView;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

public class CourseDiscussionResponsesAdapter extends RecyclerView.Adapter {

    private DiscussionThread discussionThread;
    private List<DiscussionComment> discussionResponses = new ArrayList<>();

    static class RowType {
        static final int THREAD = 0;
        static final int RESPONSE = 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == RowType.THREAD) {
            View discussionThreadRow = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.discussion_responses_thread_row, parent, false);

            return new DiscussionThreadViewHolder(discussionThreadRow);
        }

        View discussionResponseRow = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.discussion_responses_response_row, parent, false);

        return new DiscussionResponseViewHolder(discussionResponseRow);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            DiscussionThreadViewHolder discussionThreadHolder = (DiscussionThreadViewHolder) holder;
            discussionThreadHolder.threadTitleTextView.setText(discussionThread.getTitle());
            return;
        }

        DiscussionComment response = discussionResponses.get(position - 1);
        DiscussionResponseViewHolder responseViewHolder = (DiscussionResponseViewHolder) holder;
        responseViewHolder.responseCommentTextView.setText(response.getRawBody());
    }

    @Override
    public int getItemCount() {
        if (discussionThread == null) {
            return 0;
        }
        return 1 + discussionResponses.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return RowType.THREAD;
        }

        return RowType.RESPONSE;
    }


    public void setDiscussionThread(DiscussionThread discussionThread) {
        this.discussionThread = discussionThread;
        notifyDataSetChanged();
    }

    public void setDiscussionResponses(List<DiscussionComment> discussionResponses) {
        this.discussionResponses = discussionResponses;
        notifyDataSetChanged();
    }

    public static class RoboViewHolder extends RecyclerView.ViewHolder {
        public RoboViewHolder(View itemView) {
            super(itemView);
            RoboGuice.getInjector(itemView.getContext()).injectViewMembers(this);
        }
    }

    public static class DiscussionThreadViewHolder extends RecyclerView.ViewHolder {
//        @InjectView(R.id.discussion_responses_thread_title)
        ETextView threadTitleTextView;

        public DiscussionThreadViewHolder(View itemView) {
            super(itemView);
//            RoboGuice.getInjector(itemView.getContext()).injectViewMembers(DiscussionThreadViewHolder.this);
            threadTitleTextView = (ETextView) itemView.findViewById(R.id.discussion_responses_thread_title);
        }
    }

    public static class DiscussionResponseViewHolder extends RecyclerView.ViewHolder {
        ETextView responseCommentTextView;

        public DiscussionResponseViewHolder(View itemView) {
            super(itemView);
            responseCommentTextView = (ETextView) itemView.findViewById(R.id.discussion_responses_comment_text_view);
        }
    }

}
