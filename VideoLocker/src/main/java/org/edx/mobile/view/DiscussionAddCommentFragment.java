package org.edx.mobile.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.APICallback;
import com.qualcomm.qlearn.sdk.discussion.CommentBody;
import com.qualcomm.qlearn.sdk.discussion.CourseTopics;
import com.qualcomm.qlearn.sdk.discussion.DiscussionAPI;
import com.qualcomm.qlearn.sdk.discussion.DiscussionComment;
import com.qualcomm.qlearn.sdk.discussion.ThreadComments;
import com.qualcomm.qlearn.sdk.discussion.TopicThreads;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.ISegment;

import roboguice.fragment.RoboFragment;

public class DiscussionAddCommentFragment extends RoboFragment {

    static public String TAG = DiscussionAddCommentFragment.class.getCanonicalName();
    static public String ENROLLMENT = TAG + ".enrollment";
    static public String IS_RESPONSE = TAG + ".isResponse";

    protected final Logger logger = new Logger(getClass().getName());
    private EditText editTextNewComment;
    private Button buttonAddComment;
    private TextView textViewAnswer;
    private TextView textViewResponse;
    private TextView textViewTimeAuthor;

    private EnrolledCoursesResponse courseData;
    private Boolean isResponse;

    @Inject
    ISegment segIO;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle bundle = getArguments();
        courseData = (EnrolledCoursesResponse) bundle.getSerializable(ENROLLMENT);
        isResponse = bundle.getBoolean(IS_RESPONSE);

        try{
            segIO.screenViewsTracking(courseData.getCourse().getName() +
                    " - AddComment");
        }catch(Exception e){
            logger.error(e);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View fragment = inflater.inflate(R.layout.fragment_add_comment, container,
                false);

        editTextNewComment = (EditText) fragment.findViewById(R.id.etNewComment);
        editTextNewComment.setHint(getString(isResponse ? R.string.discussion_add_your_response : R.string.discussion_add_your_comment));
        buttonAddComment = (Button) fragment.findViewById(R.id.btnAddComment);
        buttonAddComment.setText(getString(isResponse ? R.string.discussion_add_response : R.string.discussion_add_comment));

        textViewAnswer = (TextView) fragment.findViewById(R.id.tvAnswer);
        textViewResponse = (TextView) fragment.findViewById(R.id.tvResponse);
        textViewTimeAuthor = (TextView) fragment.findViewById(R.id.tvTimeAuthor);

        // TODO: replace with real data
        textViewResponse.setText("new response from Android new response from Android new response from Android new response from Android new response from Android new response from Android ");
        textViewTimeAuthor.setText("16 hours ago by jeffxtang");

        buttonAddComment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonAddComment.setEnabled(false);
                final String newComment = editTextNewComment.getText().toString();

                // This is actually API test code - after the other screens are done (topics, threads, responses, comments) only one API call (createComment) needs to happen here
                // TODO: move the first 3 API calls elsewhere and pass the response object here
                new DiscussionAPI().getTopicList(courseData.getCourse().getId(), new APICallback<CourseTopics>() {
                    @Override
                    public void success(CourseTopics courseTopics) {
                        new DiscussionAPI().searchThreadList(courseData.getCourse().getId(), "critic", new APICallback<TopicThreads>() {
                            @Override
                            public void success(TopicThreads threads) {
                                // get responses of a thread (post)
                                new DiscussionAPI().getCommentList(threads.getResults().get(0).getIdentifier(), new APICallback<ThreadComments>() {
                                    @Override
                                    public void success(ThreadComments comments) {
                                        System.out.println(comments);

                                        DiscussionComment response = comments.getResults().get(0);
                                        CommentBody commentBody = new CommentBody();
                                        commentBody.setRawBody(newComment);
                                        commentBody.setThreadId(response.getThreadId());
                                        commentBody.setParentId(response.getIdentifier());
                                        new DiscussionAPI().createComment(commentBody, new APICallback<DiscussionComment>() {
                                            @Override
                                            public void success(DiscussionComment thread) {
                                                buttonAddComment.setEnabled(true);
                                            }

                                            @Override
                                            public void failure(Exception e) {
                                                buttonAddComment.setEnabled(true);
                                            }
                                        });
                                    }

                                    @Override
                                    public void failure(Exception e) {
                                        buttonAddComment.setEnabled(true);
                                    }
                                });

                            }

                            @Override
                            public void failure(Exception e) {
                                buttonAddComment.setEnabled(true);
                            }
                        });

                    }

                    @Override
                    public void failure(Exception e) {
                        buttonAddComment.setEnabled(true);
                    }
                });
            }
        });

        return fragment;
    }

}
