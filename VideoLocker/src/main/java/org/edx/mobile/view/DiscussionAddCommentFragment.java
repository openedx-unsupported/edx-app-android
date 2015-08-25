package org.edx.mobile.view;

import android.os.Bundle;

import android.support.annotation.NonNull;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.edx.mobile.discussion.CommentBody;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionThread;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionCommentPostedEvent;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.task.CreateCommentTask;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class DiscussionAddCommentFragment extends RoboFragment {

    static public String TAG = DiscussionAddCommentFragment.class.getCanonicalName();

    @InjectExtra(value = Router.EXTRA_DISCUSSION_COMMENT, optional = true)
    DiscussionComment discussionComment;

    @InjectExtra(value = Router.EXTRA_DISCUSSION_TOPIC_OBJ, optional = true)
    DiscussionThread discussionTopic;

    protected final Logger logger = new Logger(getClass().getName());

    @InjectView(R.id.etNewComment)
    private EditText editTextNewComment;

    @InjectView(R.id.btnAddComment)
    private Button buttonAddComment;

    @InjectView(R.id.tvAnswer)
    private TextView textViewAnswer;

    @InjectView(R.id.tvResponse)
    private TextView textViewResponse;

    @InjectView(R.id.tvTimeAuthor)
    private TextView textViewTimeAuthor;
    private CreateCommentTask createCommentTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_comment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final boolean isResponse = (discussionComment == null);

        editTextNewComment.setHint(getString(isResponse ? R.string.discussion_add_your_response : R.string.discussion_add_your_comment));
        buttonAddComment.setText(getString(isResponse ? R.string.discussion_add_response : R.string.discussion_add_comment));


        if ( discussionComment != null ) {
            CharSequence charSequence = Html.fromHtml(discussionComment.getRenderedBody());
            textViewResponse.setText(charSequence  );
            CharSequence formattedDate = DateUtils.getRelativeTimeSpanString(
                    discussionComment.getCreatedAt().getTime(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE);
            textViewTimeAuthor.setText(formattedDate + " by " + discussionComment.getAuthor());
        } else if ( discussionTopic != null ){
            CharSequence charSequence = Html.fromHtml(discussionTopic.getRenderedBody());
            textViewResponse.setText(charSequence  );
            CharSequence formattedDate = DateUtils.getRelativeTimeSpanString(
                    discussionTopic.getCreatedAt().getTime(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE);
            textViewTimeAuthor.setText(formattedDate + " by " + discussionTopic.getAuthor());
        }

        buttonAddComment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createComment();
            }
        });
    }

    protected void createComment() {

        if ( createCommentTask != null ){
            createCommentTask.cancel(true);
        }
        String newComment = editTextNewComment.getText().toString();
        final boolean isResponse = (discussionComment == null);

        final CommentBody commentBody = new CommentBody();

        commentBody.setRawBody(newComment);
        commentBody.setThreadId(isResponse ? discussionTopic.getIdentifier() : discussionComment.getThreadId());
        commentBody.setParentId(isResponse ? null : discussionComment.getIdentifier());

        createCommentTask = new CreateCommentTask(getActivity(), commentBody) {
            @Override
            public void onSuccess(@NonNull DiscussionComment thread) {
                logger.debug(thread.toString());
                EventBus.getDefault().post(new DiscussionCommentPostedEvent(thread, discussionComment));
                getActivity().finish();
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
            }
        };
        createCommentTask.execute();
    }
}
