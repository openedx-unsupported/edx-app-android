package org.edx.mobile.tta.ui.course.discussion;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.course.discussion.view_model.CourseDiscussionViewModel;

public class CourseDiscussionTab extends TaBaseFragment {

    private CourseDiscussionViewModel viewModel;

    private Content content;
    private EnrolledCoursesResponse course;

    public static CourseDiscussionTab newInstance(Content content, EnrolledCoursesResponse course) {
        CourseDiscussionTab courseDiscussionTab = new CourseDiscussionTab();
        courseDiscussionTab.content = content;
        courseDiscussionTab.course = course;
        return courseDiscussionTab;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new CourseDiscussionViewModel(getActivity(), this, content, course);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_course_discussion, viewModel).getRoot();

        return view;
    }

}
