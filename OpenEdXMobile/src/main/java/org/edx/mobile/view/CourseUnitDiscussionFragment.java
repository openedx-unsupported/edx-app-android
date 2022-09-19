package org.edx.mobile.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.edx.mobile.R;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.http.callback.Callback;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.DiscussionBlockModel;
import org.json.JSONObject;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CourseUnitDiscussionFragment extends CourseUnitFragment {

    @Inject
    CourseAPI courseApi;

    public static CourseUnitDiscussionFragment newInstance(CourseComponent unit, EnrolledCoursesResponse courseData) {
        CourseUnitDiscussionFragment f = new CourseUnitDiscussionFragment();
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        args.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
        f.setArguments(args);
        return f;
    }

    /**
     * The Fragment's UI is just a FrameLayout which nests the CourseDiscussionPostsThreadFragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_unit_discussion, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            markComponentCompletion(true);
            courseApi.markBlocksCompletion(unit.getCourseId(), List.of(unit.getId())).enqueue(new Callback<>() {
                @Override
                protected void onResponse(@NonNull JSONObject responseBody) {
                    // Nothing to do here
                }
            });
            // First we need to get the discussion topic id to send to the posts fragment
            String topicId = ((DiscussionBlockModel) unit).getData().topicId;

            Fragment fragment = CourseDiscussionPostsThreadFragment.newInstance(topicId,
                    getArguments().getSerializable(Router.EXTRA_COURSE_DATA),
                    !TextUtils.isEmpty(unit.getDisplayName()));
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.content, fragment);
            fragmentTransaction.commit();
        }
    }
}
