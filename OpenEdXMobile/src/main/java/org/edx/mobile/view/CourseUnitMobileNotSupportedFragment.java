package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.util.BrowserUtil;

public class CourseUnitMobileNotSupportedFragment extends CourseUnitFragment {
    public static CourseUnitMobileNotSupportedFragment newInstance(@NonNull CourseComponent unit) {
        final CourseUnitMobileNotSupportedFragment fragment = new CourseUnitMobileNotSupportedFragment();
        final Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View layout = inflater.inflate(R.layout.fragment_course_unit_grade, container, false);
        ((TextView) layout.findViewById(R.id.not_available_message)).setText(
                unit.getType() == BlockType.VIDEO ? R.string.video_only_on_web_short : R.string.assessment_not_available);
        layout.findViewById(R.id.view_on_web_button).setOnClickListener(v -> {
            BrowserUtil.open(getActivity(), unit.getWebUrl());
            environment.getAnalyticsRegistry().trackOpenInBrowser(unit.getId(),unit.getCourseId(),
                    unit.isMultiDevice(), unit.getBlockId());
        });
        return layout;
    }
}
