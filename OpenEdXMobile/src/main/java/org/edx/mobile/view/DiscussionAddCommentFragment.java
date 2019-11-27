package org.edx.mobile.view;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.Editable;
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
import org.edx.mobile.discussion.DiscussionService;
import org.edx.mobile.discussion.DiscussionTextUtils;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.http.notifications.DialogErrorNotification;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.SoftKeyboardUtil;
import org.edx.mobile.view.common.TaskProgressCallback.ProgressViewController;
import org.edx.mobile.view.view_holders.AuthorLayoutViewHolder;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class DiscussionAddCommentFragment extends BaseFragment {

    static public String TAG = DiscussionAddCommentFragment.class.getCanonicalName();

    @InjectExtra(value = Router.EXTRA_DISCUSSION_COMMENT, optional = true)
    DiscussionComment discussionResponse;

    @InjectExtra(Router.EXTRA_DISCUSSION_THREAD)
    private DiscussionThread discussionThread;

    protected final Logger logger = new Logger(getClass().getName());

    @InjectView(R.id.etNewComment)
    private EditText editTextNewComment;

    @InjectView(R.id.btnAddComment)
    private ViewGroup buttonAddComment;

    @InjectView(R.id.btnAddCommentText)
    private TextView textViewAddComment;

    @InjectView(R.id.progress_indicator)
    private ProgressBar createCommentProgressBar;

    @InjectView(R.id.tvResponse)
    private TextView textViewResponse;

    @Inject
    private DiscussionService discussionService;

    @Inject
    private Router router;

    @Inject
    private AnalyticsRegistry analyticsRegistry;

    @Inject
    private Config config;

    private Call<DiscussionComment> createCommentCall;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.TOPIC_ID, discussionThread.getTopicId());
        values.put(Analytics.Keys.THREAD_ID, discussionThread.getIdentifier());
        values.put(Analytics.Keys.RESPONSE_ID, discussionResponse.getIdentifier());
        if (!discussionResponse.isAuthorAnonymous()) {
            values.put(Analytics.Keys.AUTHOR, discussionResponse.getAuthor());
        }
        analyticsRegistry.trackScreenView(Analytics.Screens.FORUM_ADD_RESPONSE_COMMENT,
                discussionThread.getCourseId(), discussionThread.getTitle(), values);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_response_or_comment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DiscussionTextUtils.renderHtml(textViewResponse, discussionResponse.getRenderedBody());

        AuthorLayoutViewHolder authorLayoutViewHolder =
                new AuthorLayoutViewHolder(getView().findViewById(R.id.discussion_user_profile_row));
        authorLayoutViewHolder.populateViewHolder(config, discussionResponse, discussionResponse,
                System.currentTimeMillis(),
                new Runnable() {
                    @Override
                    public void run() {
                        router.showUserProfile(getActivity(), discussionResponse.getAuthor());
                    }
                });
        DiscussionTextUtils.setEndorsedState(authorLayoutViewHolder.answerTextView,
                discussionThread, discussionResponse);

        textViewAddComment.setText(R.string.discussion_add_comment_button_label);
        buttonAddComment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createComment();
            }
        });
        buttonAddComment.setEnabled(false);
        buttonAddComment.setContentDescription(getString(R.string.discussion_add_comment_button_description));
        editTextNewComment.setHint(R.string.discussion_add_comment_hint);
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

        if (createCommentCall != null) {
            createCommentCall.cancel();
        }

        createCommentCall = discussionService.createComment(new CommentBody(
                discussionResponse.getThreadId(), editTextNewComment.getText().toString(),
                discussionResponse.getIdentifier()));
        createCommentCall.enqueue(new ErrorHandlingCallback<DiscussionComment>(
                getActivity(),
                new ProgressViewController(createCommentProgressBar),
                new DialogErrorNotification(this)) {
            @Override
            protected void onResponse(@NonNull final DiscussionComment thread) {
                logger.debug(thread.toString());
                EventBus.getDefault().post(new DiscussionCommentPostedEvent(thread, discussionResponse));
                getActivity().finish();
            }

            @Override
            protected void onFailure(@NonNull final Throwable error) {
                buttonAddComment.setEnabled(true);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            SoftKeyboardUtil.clearViewFocus(editTextNewComment);
        }
    }
}
