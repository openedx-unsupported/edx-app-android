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

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
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


    private ViewGroup container;

    private CourseTopics allCourseTopics;
    private List<DiscussionTopicDepth> allTopicsWithDepth;
    private int selectedTopicIndex;
    private GetTopicListTask getTopicListTask;
    private CreateThreadTask createThreadTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            segIO.screenViewsTracking(courseData.getCourse().getName() +
                    " - AddPost");
        } catch (Exception e) {
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

        getTopicList();

        topicsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // if a top-level topic is selected, go back to previous selected position
                DiscussionTopicDepth topic = allTopicsWithDepth.get(position);
                if (topic.getDepth() == 0) {
                    topicsSpinner.setSelection(selectedTopicIndex);
                    Toast.makeText(container.getContext(), "Top level topic cannot be selected.", Toast.LENGTH_SHORT).show();
                } else
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

                addPostButton.setEnabled(false);
                createThread(threadBody);
            }
        });
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
                //  hideProgress();
                addPostButton.setEnabled(true);
            }
        };
        createThreadTask.execute();
    }

    protected void getTopicList() {
        if (getTopicListTask != null) {
            getTopicListTask.cancel(true);
        }
        getTopicListTask = new GetTopicListTask(getActivity(), courseData.getCourse().getId()) {
            @Override
            public void onSuccess(CourseTopics courseTopics) {
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
            public void onException(Exception ex) {
                logger.error(ex);
                //  hideProgress();
            }
        };
        getTopicListTask.execute();
    }

}
