package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.discussion.CourseTopics;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionThreadPostedEvent;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.discussion.DiscussionTopicDepth;
import org.edx.mobile.discussion.ThreadBody;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.task.CreateThreadTask;
import org.edx.mobile.task.GetTopicListTask;
import org.edx.mobile.view.adapters.TopicSpinnerAdapter;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class DiscussionAddPostFragment extends RoboFragment {

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
    private Spinner topicsSpinner;

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
    ISegment segIO;


    private ViewGroup container;

    private GetTopicListTask getTopicListTask;
    private CreateThreadTask createThreadTask;

    private int selectedTopicIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        segIO.trackScreenView(courseData.getCourse().getName() + " - AddPost");
    }

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

        ViewCompat.setBackgroundTintList(topicsSpinner, getResources().getColorStateList(R.color.edx_grayscale_neutral_dark));

        addPostButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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
        if (createThreadTask != null) {
            createThreadTask.cancel(true);
        }
        createThreadTask = new CreateThreadTask(getActivity(), threadBody) {
            @Override
            public void onSuccess(DiscussionThread courseTopics) {
                EventBus.getDefault().post(new DiscussionThreadPostedEvent(courseTopics));
                getActivity().finish();
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
                addPostButton.setEnabled(true);
            }
        };
        createThreadTask.setTaskProcessCallback(null);
        createThreadTask.setProgressDialog(addPostProgressBar);
        createThreadTask.execute();
    }

    protected void getTopicList() {
        if (getTopicListTask != null) {
            getTopicListTask.cancel(true);
        }
        getTopicListTask = new GetTopicListTask(getActivity(), courseData.getCourse().getId()) {
            @Override
            public void onSuccess(CourseTopics courseTopics) {
                final ArrayList<DiscussionTopic> allTopics = new ArrayList<>();
                allTopics.addAll(courseTopics.getCoursewareTopics());
                allTopics.addAll(courseTopics.getNonCoursewareTopics());

                final TopicSpinnerAdapter adapter = new TopicSpinnerAdapter(container.getContext(), DiscussionTopicDepth.createFromDiscussionTopics(allTopics));
                topicsSpinner.setAdapter(adapter);

                {
                    // Attempt to select the topic that we navigated from
                    // Otherwise, leave the default option, which is "Choose a topic..."
                    int selectedTopicIndex = adapter.getPosition(discussionTopic);
                    if (selectedTopicIndex >= 0) {
                        topicsSpinner.setSelection(selectedTopicIndex);
                    }
                }
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
            }
        };
        getTopicListTask.setMessageCallback(null);
        getTopicListTask.execute();
    }

}
