package org.edx.mobile.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.APICallback;
import com.qualcomm.qlearn.sdk.discussion.CourseTopics;
import com.qualcomm.qlearn.sdk.discussion.DiscussionAPI;
import com.qualcomm.qlearn.sdk.discussion.DiscussionThread;
import com.qualcomm.qlearn.sdk.discussion.DiscussionTopic;
import com.qualcomm.qlearn.sdk.discussion.DiscussionTopicDepth;
import com.qualcomm.qlearn.sdk.discussion.ThreadBody;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.ISegment;

import java.util.ArrayList;
import java.util.List;

import info.hoang8f.android.segmented.SegmentedGroup;
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
    private SegmentedGroup discussionQuestionSegmentedGroup;

    @InjectView(R.id.topics_spinner)
    private Spinner topicsSpinner;

    @InjectView(R.id.title_edit_text)
    private EditText titleEditText;

    @InjectView(R.id.body_edit_text)
    private EditText bodyEditText;

    @InjectView(R.id.add_post_button)
    private Button addPostButton;

    @Inject
    ISegment segIO;

    @Inject
    DiscussionAPI discussionAPI;

    private ViewGroup container;

    private CourseTopics allCourseTopics;
    private List<DiscussionTopicDepth> allTopicsWithDepth;
    private int selectedTopicIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{
            segIO.screenViewsTracking(courseData.getCourse().getName() +
                    " - AddPost");
        }catch(Exception e){
            logger.error(e);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        this.container = container;
        return inflater.inflate(R.layout.fragment_add_post, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        discussionQuestionSegmentedGroup.check(R.id.discussion_radio_button);
        discussionQuestionSegmentedGroup.setTintColor(this.getResources().getColor(R.color.edx_grayscale_neutral_base),
                this.getResources().getColor(R.color.black));

        discussionAPI.getTopicList(courseData.getCourse().getId(), new APICallback<CourseTopics>() {
            @Override
            public void success(CourseTopics courseTopics) {
                allCourseTopics = courseTopics;
                ArrayList<DiscussionTopic> allTopics = new ArrayList<>();
                allTopics.addAll(courseTopics.getCoursewareTopics());
                allTopics.addAll(courseTopics.getNonCoursewareTopics());

                allTopicsWithDepth = DiscussionTopicDepth.createFromDiscussionTopics(allTopics);
                ArrayList<String> topicList = new ArrayList<String>();
                int i = 0;
                for (DiscussionTopicDepth topic : allTopicsWithDepth) {
                    topicList.add((topic.getDepth() == 0 ? "" : "  ") + topic.getDiscussionTopic().getName());
                    if (discussionTopic.getName().equalsIgnoreCase(topic.getDiscussionTopic().getName()))
                        selectedTopicIndex = i;
                    i++;
                }

                String[] topics = new String[topicList.size()];
                topics = topicList.toArray(topics);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(container.getContext(), android.R.layout.simple_spinner_item, topics);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                topicsSpinner.setAdapter(adapter);
            }

            @Override
            public void failure(Exception e) {
                logger.error(e, false);
                // TODO: Handle error gracefully
            }
        });
        topicsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // if a top-level topic is selected, go back to previous selected position
                DiscussionTopicDepth topic = allTopicsWithDepth.get(position);
                if (topic.getDepth() == 0) {
                    topicsSpinner.setSelection(selectedTopicIndex);
                    Toast.makeText(container.getContext(), "Top level topic cannot be selected.", Toast.LENGTH_SHORT).show();
                }
                else
                    selectedTopicIndex = position;
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        titleEditText.setHint(getString(R.string.discussion_post_title));
        bodyEditText.setHint(getString(R.string.discussion_add_your_post));
        addPostButton.setText(getString(R.string.discussion_add_post));

        addPostButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addPostButton.setEnabled(false);
                final String title = titleEditText.getText().toString();
                final String body = bodyEditText.getText().toString();
                if (title.trim().length() == 0 || body.trim().length() == 0) return;

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
                threadBody.setTopicId(allTopicsWithDepth.get(selectedTopicIndex).getDiscussionTopic().getIdentifier());
                threadBody.setType(discussionQuestion);

                new DiscussionAPI().createThread(threadBody, new APICallback<DiscussionThread>() {
                    @Override
                    public void success(DiscussionThread thread) {
                        addPostButton.setEnabled(true);
                    }

                    @Override
                    public void failure(Exception e) {
                        addPostButton.setEnabled(true);
                    }
                });

            }
        });
    }

}
