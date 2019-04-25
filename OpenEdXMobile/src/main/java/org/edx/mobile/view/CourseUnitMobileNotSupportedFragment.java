package org.edx.mobile.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.util.BrowserUtil;

public class CourseUnitMobileNotSupportedFragment extends CourseUnitFragment {
    /**
     * Create a new instance of fragment
     */
    public static CourseUnitMobileNotSupportedFragment newInstance(CourseComponent unit) {
        CourseUnitMobileNotSupportedFragment f = new CourseUnitMobileNotSupportedFragment();
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_unit_grade, container, false);
        ((TextView) v.findViewById(R.id.not_available_message)).setText(
                unit.getType() == BlockType.VIDEO ? R.string.video_only_on_web_short : R.string.assessment_not_available);
        v.findViewById(R.id.view_on_web_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BrowserUtil.open(getActivity(), unit.getWebUrl());
                environment.getAnalyticsRegistry().trackOpenInBrowser(unit.getId()
                        , unit.getCourseId(), unit.isMultiDevice(), unit.getBlockId());
            }
        });
        return v;
    }
}
