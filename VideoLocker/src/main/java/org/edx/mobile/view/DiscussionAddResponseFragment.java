package org.edx.mobile.view;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.discussion.CommentBody;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionCommentPostedEvent;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.task.CreateCommentTask;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.SoftKeyboardUtil;
import org.edx.mobile.view.view_holders.AuthorLayoutViewHolder;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class DiscussionAddResponseFragment extends BaseFragment {

    static public String TAG = DiscussionAddResponseFragment.class.getCanonicalName();

    @InjectExtra(value = Router.EXTRA_DISCUSSION_THREAD, optional = true)
    private DiscussionThread discussionThread;

    protected final Logger logger = new Logger(getClass().getName());

    @InjectView(R.id.etNewComment)
    private EditText editTextNewComment;

    @InjectView(R.id.btnAddComment)
    private ViewGroup buttonAddComment;

    @InjectView(R.id.progress_indicator)
    private ProgressBar createCommentProgressBar;

    @InjectView(R.id.tvTitle)
    private TextView textViewTitle;

    @InjectView(R.id.tvResponse)
    private TextView textViewResponse;

    private CreateCommentTask createCommentTask;

    @Inject
    private Router router;

    @Inject
    private ISegment segIO;

    @Inject
    private Config config;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Map<String, String> values = new HashMap<>();
        values.put(ISegment.Keys.TOPIC_ID, discussionThread.getTopicId());
        values.put(ISegment.Keys.THREAD_ID, discussionThread.getIdentifier());
        segIO.trackScreenView(ISegment.Screens.FORUM_ADD_RESPONSE,
                discussionThread.getCourseId(), discussionThread.getTitle(), values);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_response_or_comment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textViewTitle.setVisibility(View.VISIBLE);
        textViewTitle.setText(discussionThread.getTitle());
        textViewResponse.setText(Html.fromHtml(discussionThread.getRenderedBody()));

        AuthorLayoutViewHolder authorLayoutViewHolder =
                new AuthorLayoutViewHolder(getView().findViewById(R.id.discussion_user_profile_row));
        authorLayoutViewHolder.populateViewHolder(config, discussionThread, discussionThread,
                System.currentTimeMillis(),
                new Runnable() {
                    @Override
                    public void run() {
                        router.showUserProfile(getActivity(), discussionThread.getAuthor());
                    }
                });

        buttonAddComment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createComment();
            }
        });
        buttonAddComment.setEnabled(false);
        editTextNewComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                buttonAddComment.setEnabled(s.toString().trim().length() > 0);
            }
        });
    }

    private void createComment() {
        buttonAddComment.setEnabled(false);

        if (createCommentTask != null) {
            createCommentTask.cancel(true);
        }

        final CommentBody commentBody = new CommentBody();
        commentBody.setRawBody(editTextNewComment.getText().toString());
        commentBody.setThreadId(discussionThread.getIdentifier());
        commentBody.setParentId(null);

        createCommentTask = new CreateCommentTask(getActivity(), commentBody) {
            @Override
            public void onSuccess(@NonNull DiscussionComment thread) {
                logger.debug(thread.toString());
                EventBus.getDefault().post(new DiscussionCommentPostedEvent(thread, null));
                getActivity().finish();
            }

            @Override
            public void onException(Exception ex) {
                super.onException(ex);
                buttonAddComment.setEnabled(true);
            }
        };
        createCommentTask.setProgressDialog(createCommentProgressBar);
        createCommentTask.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            SoftKeyboardUtil.clearViewFocus(editTextNewComment);
        }
    }
}
