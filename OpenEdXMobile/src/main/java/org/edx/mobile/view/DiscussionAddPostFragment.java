package org.edx.mobile.view;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;
import androidx.appcompat.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.discussion.CourseTopics;
import org.edx.mobile.discussion.DiscussionService;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionThreadPostedEvent;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.discussion.DiscussionTopicDepth;
import org.edx.mobile.discussion.ThreadBody;
import org.edx.mobile.http.callback.CallTrigger;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.http.notifications.DialogErrorNotification;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.util.SoftKeyboardUtil;
import org.edx.mobile.view.adapters.TopicSpinnerAdapter;
import org.edx.mobile.view.common.TaskProgressCallback.ProgressViewController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class DiscussionAddPostFragment extends BaseFragment {

    static public String TAG = DiscussionAddPostFragment.class.getCanonicalName();
    static public String ENROLLMENT = TAG + ".enrollment";
    static public String TOPIC = TAG + ".topic";

    protected final Logger logger = new Logger(getClass().getName());

    @InjectExtra(Router.EXTRA_COURSE_DATA)
    private EnrolledCoursesResponse courseData;

    @InjectExtra(Router.EXTRA_DISCUSSION_TOPIC)
    private DiscussionTopic discussionTopic;

    @InjectView(R.id.discussion_question_segmented_group)
    private RadioGroup discussionQuestionSegmentedGroup;

    @InjectView(R.id.topics_spinner)
    private AppCompatSpinner topicsSpinner;

    @InjectView(R.id.title_edit_text)
    private EditText titleEditText;

    @InjectView(R.id.body_edit_text)
    private EditText bodyEditText;

    @InjectView(R.id.add_post_button)
    private ViewGroup addPostButton;

    @InjectView(R.id.add_post_button_text)
    private TextView addPostButtonText;

    @InjectView(R.id.progress_indicator)
    private ProgressBar addPostProgressBar;

    @Inject
    private DiscussionService discussionService;

    @Inject
    AnalyticsRegistry analyticsRegistry;

    private ViewGroup container;

    private Call<CourseTopics> getTopicListCall;
    private Call<DiscussionThread> createThreadCall;

    private int selectedTopicIndex;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        this.container = container;
        return inflater.inflate(R.layout.fragment_add_post, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        discussionQuestionSegmentedGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                @StringRes final int bodyHint;
                @StringRes final int submitLabel;
                @StringRes final int submitDescription;
                if (discussionQuestionSegmentedGroup.getCheckedRadioButtonId() == R.id.discussion_radio_button) {
                    bodyHint = R.string.discussion_body_hint_discussion;
                    submitLabel = R.string.discussion_add_post_button_label;
                    submitDescription = R.string.discussion_add_post_button_description;
                } else {
                    bodyHint = R.string.discussion_body_hint_question;
                    submitLabel = R.string.discussion_add_question_button_label;
                    submitDescription = R.string.discussion_add_question_button_description;
                }
                bodyEditText.setHint(bodyHint);
                addPostButtonText.setText(submitLabel);
                addPostButton.setContentDescription(getText(submitDescription));
            }
        });
        discussionQuestionSegmentedGroup.check(R.id.discussion_radio_button);

        getTopicList();

        topicsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Even though we disabled topics that aren't supposed to be selected, Android still allows you to select them using keyboard or finger-dragging
                // So, we have to revert the user's selection if they select a topic that cannot be posted to
                final DiscussionTopicDepth item = (DiscussionTopicDepth) parent.getItemAtPosition(position);
                if (null == item || item.isPostable()) {
                    selectedTopicIndex = position;
                } else {
                    // Revert selection
                    parent.setSelection(selectedTopicIndex);
                }
                setPostButtonEnabledState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setPostButtonEnabledState();
            }
        });

        ViewCompat.setBackgroundTintList(topicsSpinner, getResources().getColorStateList(R.color.primaryBaseColor));

        addPostButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null) {
                    SoftKeyboardUtil.hide(activity);
                }

                final String title = titleEditText.getText().toString();
                final String body = bodyEditText.getText().toString();

                final DiscussionThread.ThreadType discussionQuestion;
                if (discussionQuestionSegmentedGroup.getCheckedRadioButtonId() == R.id.discussion_radio_button) {
                    discussionQuestion = DiscussionThread.ThreadType.DISCUSSION;
                } else {
                    discussionQuestion = DiscussionThread.ThreadType.QUESTION;
                }

                ThreadBody threadBody = new ThreadBody();
                threadBody.setCourseId(courseData.getCourse().getId());
                threadBody.setTitle(title);
                threadBody.setRawBody(body);
                threadBody.setTopicId(((DiscussionTopicDepth) topicsSpinner.getSelectedItem()).getDiscussionTopic().getIdentifier());
                threadBody.setType(discussionQuestion);

                addPostButton.setEnabled(false);
                createThread(threadBody);
            }
        });
        addPostButton.setEnabled(false);
        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setPostButtonEnabledState();
            }
        };
        titleEditText.addTextChangedListener(textWatcher);
        bodyEditText.addTextChangedListener(textWatcher);
    }

    private void setPostButtonEnabledState() {
        final String title = titleEditText.getText().toString();
        final String body = bodyEditText.getText().toString();
        final boolean topicSelected = null != topicsSpinner.getSelectedItem();
        addPostButton.setEnabled(topicSelected && title.trim().length() > 0 && body.trim().length() > 0);
    }

    protected void createThread(ThreadBody threadBody) {
        if (createThreadCall != null) {
            createThreadCall.cancel();
        }
        createThreadCall = discussionService.createThread(threadBody);
        createThreadCall.enqueue(new ErrorHandlingCallback<DiscussionThread>(
                getActivity(),
                new ProgressViewController(addPostProgressBar),
                new DialogErrorNotification(this)) {
            @Override
            protected void onResponse(@NonNull final DiscussionThread courseTopics) {
                EventBus.getDefault().post(new DiscussionThreadPostedEvent(courseTopics));
                getActivity().finish();
            }

            @Override
            protected void onFailure(@NonNull final Throwable error) {
                addPostButton.setEnabled(true);
            }
        });
    }

    protected void getTopicList() {
        if (getTopicListCall != null) {
            getTopicListCall.cancel();
        }
        getTopicListCall = discussionService.getCourseTopics(courseData.getCourse().getId());
        getTopicListCall.enqueue(new ErrorHandlingCallback<CourseTopics>(
                getActivity(), null, null, CallTrigger.LOADING_CACHED) {
            @Override
            protected void onResponse(@NonNull final CourseTopics courseTopics) {
                final ArrayList<DiscussionTopic> allTopics = new ArrayList<>();
                allTopics.addAll(courseTopics.getNonCoursewareTopics());
                allTopics.addAll(courseTopics.getCoursewareTopics());

                final TopicSpinnerAdapter adapter = new TopicSpinnerAdapter(container.getContext(), DiscussionTopicDepth.createFromDiscussionTopics(allTopics));
                topicsSpinner.setAdapter(adapter);

                {
                    // Attempt to select the topic that we navigated from
                    // Otherwise, leave the default option, which is the first non-courseware topic
                    if (!discussionTopic.isAllType() && !discussionTopic.isFollowingType()) {
                        int selectedTopicIndex = -1;
                        if (discussionTopic.getIdentifier() == null) {
                            // In case of a parent topic, we need to select the first child topic
                            if (!discussionTopic.getChildren().isEmpty()) {
                                selectedTopicIndex = adapter.getPosition(discussionTopic.getChildren().get(0));
                            }
                        } else {
                            selectedTopicIndex = adapter.getPosition(discussionTopic);
                        }
                        if (selectedTopicIndex >= 0) {
                            topicsSpinner.setSelection(selectedTopicIndex);
                        }
                    }
                }

                DiscussionTopic selectedTopic = ((DiscussionTopicDepth) topicsSpinner.getSelectedItem()).getDiscussionTopic();
                Map<String, String> values = new HashMap<>();
                values.put(Analytics.Keys.TOPIC_ID, selectedTopic.getIdentifier());
                analyticsRegistry.trackScreenView(Analytics.Screens.FORUM_CREATE_TOPIC_THREAD,
                        courseData.getCourse().getId(), selectedTopic.getName(), values);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            SoftKeyboardUtil.clearViewFocus(titleEditText);
        }
    }
}
