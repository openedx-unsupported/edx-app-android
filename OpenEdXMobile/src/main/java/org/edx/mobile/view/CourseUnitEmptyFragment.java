package org.edx.mobile.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.model.course.CourseComponent;

public class CourseUnitEmptyFragment extends CourseUnitFragment {
    public static CourseUnitEmptyFragment newInstance(CourseComponent unit) {
        CourseUnitEmptyFragment f = new CourseUnitEmptyFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_unit_empty, container, false);
    }
}
