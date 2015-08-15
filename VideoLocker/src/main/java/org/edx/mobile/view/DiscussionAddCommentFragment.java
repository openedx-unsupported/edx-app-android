package org.edx.mobile.view;

import android.os.Bundle;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.qualcomm.qlearn.sdk.discussion.APICallback;
import com.qualcomm.qlearn.sdk.discussion.CommentBody;
import com.qualcomm.qlearn.sdk.discussion.DiscussionAPI;
import com.qualcomm.qlearn.sdk.discussion.DiscussionComment;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class DiscussionAddCommentFragment extends RoboFragment {

    static public String TAG = DiscussionAddCommentFragment.class.getCanonicalName();

    @InjectExtra(value = Router.EXTRA_DISCUSSION_COMMENT, optional = true)
    DiscussionComment discussionComment;

    @InjectExtra(Router.EXTRA_DISCUSSION_TOPIC_ID)
    String discussionTopicId;

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

        // TODO: replace with real data
        textViewResponse.setText("new response from Android new response from Android new response from Android new response from Android new response from Android new response from Android ");
        textViewTimeAuthor.setText("16 hours ago by jeffxtang");

        buttonAddComment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String newComment = editTextNewComment.getText().toString();

                CommentBody commentBody = new CommentBody();

                commentBody.setRawBody(newComment);
                commentBody.setThreadId(isResponse ? discussionTopicId : discussionComment.getThreadId());
                commentBody.setParentId(isResponse ? null : discussionComment.getParentId());

                new DiscussionAPI().createComment(commentBody, new APICallback<DiscussionComment>() {
                    @Override
                    public void success(DiscussionComment thread) {
                        // TODO: Go back to the comment screen?
                    }

                    @Override
                    public void failure(Exception e) {
                        // TODO: Handle failure
                    }
                });
            }
        });
    }

}
