package org.edx.mobile.tta.ui.course;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.course.view_model.CourseHandoutsViewModel;

public class CourseHandoutsTab extends TaBaseFragment {

    private CourseHandoutsViewModel viewModel;

    private Content content;
    private EnrolledCoursesResponse course;

    public static CourseHandoutsTab newInstance(Content content, EnrolledCoursesResponse course) {
        CourseHandoutsTab courseHandoutsTab = new CourseHandoutsTab();
        courseHandoutsTab.content = content;
        courseHandoutsTab.course = course;
        return courseHandoutsTab;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new CourseHandoutsViewModel(getActivity(), this, content, course);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_course_handouts, viewModel).getRoot();

        return view;
    }

}
