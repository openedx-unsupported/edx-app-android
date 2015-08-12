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

public class DiscussionAddPostFragment extends RoboFragment {

    static public String TAG = DiscussionAddPostFragment.class.getCanonicalName();
    static public String ENROLLMENT = TAG + ".enrollment";

    protected final Logger logger = new Logger(getClass().getName());
    private SegmentedGroup segmentedGroup;
    private Spinner spinnerTopics;
    private EditText editTextTitle;
    private EditText editTextBody;
    private Button buttonAddPost;

    private EnrolledCoursesResponse courseData;

    @Inject
    ISegment segIO;

    @Inject
    DiscussionAPI discussionAPI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle bundle = getArguments();
        courseData = (EnrolledCoursesResponse) bundle.getSerializable(ENROLLMENT);

        try{
            segIO.screenViewsTracking(courseData.getCourse().getName() +
                    " - AddPost");
        }catch(Exception e){
            logger.error(e);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
            Bundle savedInstanceState) {

        View fragment = inflater.inflate(R.layout.fragment_add_post, container,
                false);

        segmentedGroup = (SegmentedGroup) fragment.findViewById(R.id.segmentedControl);
        segmentedGroup.check(R.id.btnDiscussion);
        segmentedGroup.setTintColor(this.getResources().getColor(R.color.edx_grayscale_neutral_base),
                this.getResources().getColor(R.color.black));

        spinnerTopics = (Spinner) fragment.findViewById(R.id.spinnerTopics);
        discussionAPI.getTopicList(courseData.getCourse().getId(), new APICallback<CourseTopics>() {
            @Override
            public void success(CourseTopics courseTopics) {
                ArrayList<DiscussionTopic> allTopics = new ArrayList<>();
                allTopics.addAll(courseTopics.getCoursewareTopics());
                allTopics.addAll(courseTopics.getNonCoursewareTopics());

                List<DiscussionTopicDepth> allTopicsWithDepth = DiscussionTopicDepth.createFromDiscussionTopics(allTopics);
                ArrayList<String> topicList = new ArrayList<String>();
                for (DiscussionTopicDepth topic : allTopicsWithDepth) {
                    topicList.add((topic.getDepth() == 0 ? "" : "  ") + topic.getDiscussionTopic().getName());
                }

                String[] topics = new String[topicList.size()];
                topics = topicList.toArray(topics);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(container.getContext(), android.R.layout.simple_spinner_item, topics);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerTopics.setAdapter(adapter);
            }

            @Override
            public void failure(Exception e) {
                logger.error(e, false);
                // TODO: Handle error gracefully
            }
        });
        spinnerTopics.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        editTextTitle = (EditText) fragment.findViewById(R.id.etTitle);
        editTextTitle.setHint(getString(R.string.discussion_post_title));
        editTextBody = (EditText) fragment.findViewById(R.id.etBody);
        editTextBody.setHint(getString(R.string.discussion_add_your_post));
        buttonAddPost = (Button) fragment.findViewById(R.id.btnAddPost);
        buttonAddPost.setText(getString(R.string.discussion_add_post));

        buttonAddPost.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonAddPost.setEnabled(false);
                final String title = editTextTitle.getText().toString();
                final String body = editTextBody.getText().toString();
                if (title.trim().length() == 0 || body.trim().length() == 0) return;

                final String discussionQuestion;
                if (segmentedGroup.getCheckedRadioButtonId() == R.id.btnDiscussion) {
                    discussionQuestion = getString(R.string.discussion_title);
                } else { //if (segmentedGroup.getCheckedRadioButtonId() == R.id.btnQuestion) {
                    discussionQuestion = getString(R.string.discussion_question);
                }

                // This is actually API test code - after the topics and threads screens are done, only one API call (createThread) needs to happen here
                // TODO: move the first API calls elsewhere and pass the topic object here
                new DiscussionAPI().getTopicList(courseData.getCourse().getId(), new APICallback<CourseTopics>() {
                    @Override
                    public void success(CourseTopics courseTopics) {

                        ThreadBody threadBody = new ThreadBody();
                        threadBody.setCourseId(courseData.getCourse().getId());
                        threadBody.setTitle(title);
                        threadBody.setRawBody(body);
                        threadBody.setTopicId(courseTopics.getCoursewareTopics().get(0).getChildren().get(0).getIdentifier());
                        threadBody.setType(discussionQuestion);

                        new DiscussionAPI().createThread(threadBody, new APICallback<DiscussionThread>() {
                            @Override
                            public void success(DiscussionThread thread) {
                            }

                            @Override
                            public void failure(Exception e) {
                                buttonAddPost.setEnabled(true);
                            }
                        });
                    }

                    @Override
                    public void failure(Exception e) {
                        buttonAddPost.setEnabled(true);
                    }
                });

            }
        });

        return fragment;
    }

}
