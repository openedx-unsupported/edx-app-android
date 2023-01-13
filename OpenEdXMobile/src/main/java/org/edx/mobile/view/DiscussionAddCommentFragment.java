package org.edx.mobile.view;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.databinding.FragmentAddResponseOrCommentBinding;
import org.edx.mobile.discussion.DiscussionCommentPostedEvent;
import org.edx.mobile.discussion.DiscussionService;
import org.edx.mobile.discussion.DiscussionTextUtils;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.http.notifications.DialogErrorNotification;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.discussion.CommentBody;
import org.edx.mobile.model.discussion.DiscussionComment;
import org.edx.mobile.model.discussion.DiscussionThread;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.SoftKeyboardUtil;
import org.edx.mobile.view.common.TaskProgressCallback.ProgressViewController;
import org.edx.mobile.view.view_holders.AuthorLayoutViewHolder;
import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;

@AndroidEntryPoint
public class DiscussionAddCommentFragment extends BaseFragment {

    private DiscussionComment discussionResponse;
    private DiscussionThread discussionThread;

    protected final Logger logger = new Logger(getClass().getName());

    @Inject
    DiscussionService discussionService;

    @Inject
    Router router;

    @Inject
    AnalyticsRegistry analyticsRegistry;

    @Inject
    Config config;

    private Call<DiscussionComment> createCommentCall;
    private FragmentAddResponseOrCommentBinding binding;

    @Inject
    public DiscussionAddCommentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseExtras();

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddResponseOrCommentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.discussionRenderBody.setBody(discussionResponse.getRenderedBody());

        AuthorLayoutViewHolder authorLayoutViewHolder =
                new AuthorLayoutViewHolder(binding.rowDiscussionUserProfile.discussionUserProfileRow);
        authorLayoutViewHolder.populateViewHolder(config, discussionResponse, discussionResponse,
                System.currentTimeMillis(),
                () -> router.showUserProfile(requireActivity(), discussionResponse.getAuthor()));
        DiscussionTextUtils.setEndorsedState(authorLayoutViewHolder.answerTextView,
                discussionThread, discussionResponse);

        binding.btnAddCommentText.setText(R.string.discussion_add_comment_button_label);
        binding.btnAddComment.setOnClickListener(v -> createComment());
        binding.btnAddComment.setEnabled(false);
        binding.btnAddComment.setContentDescription(getString(R.string.discussion_add_comment_button_description));
        binding.etNewComment.setHint(R.string.discussion_add_comment_hint);
        binding.etNewComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.btnAddComment.setEnabled(s.toString().trim().length() > 0);
            }
        });
    }

    private void parseExtras() {
        discussionResponse = (DiscussionComment) getArguments().getSerializable(Router.EXTRA_DISCUSSION_COMMENT);
        discussionThread = (DiscussionThread) getArguments().getSerializable(Router.EXTRA_DISCUSSION_THREAD);
    }

    private void createComment() {
        binding.btnAddComment.setEnabled(false);

        if (createCommentCall != null) {
            createCommentCall.cancel();
        }

        createCommentCall = discussionService.createComment(new CommentBody(
                discussionResponse.getThreadId(), binding.etNewComment.getText().toString(),
                discussionResponse.getIdentifier()));
        createCommentCall.enqueue(new ErrorHandlingCallback<DiscussionComment>(
                requireActivity(),
                new ProgressViewController(binding.buttonProgressIndicator.progressIndicator),
                new DialogErrorNotification(this)) {
            @Override
            protected void onResponse(@NonNull final DiscussionComment thread) {
                logger.debug(thread.toString());
                EventBus.getDefault().post(new DiscussionCommentPostedEvent(thread, discussionResponse));
                requireActivity().finish();
            }

            @Override
            protected void onFailure(@NonNull final Throwable error) {
                binding.btnAddComment.setEnabled(true);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            SoftKeyboardUtil.clearViewFocus(binding.etNewComment);
        }
    }
}
