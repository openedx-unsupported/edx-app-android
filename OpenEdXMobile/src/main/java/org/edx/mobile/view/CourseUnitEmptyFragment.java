package org.edx.mobile.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.edx.mobile.R;
import org.edx.mobile.model.course.CourseComponent;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CourseUnitEmptyFragment extends CourseUnitFragment {
    public static CourseUnitEmptyFragment newInstance(@NonNull CourseComponent unit) {
        final CourseUnitEmptyFragment fragment = new CourseUnitEmptyFragment();
        final Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_unit_empty, container, false);
    }
}
