package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.third_party.iconify.Iconify;
import org.edx.mobile.view.common.PageViewStateCallback;

/**
 *
 */
public class CourseUnitEmptyFragment extends Fragment implements PageViewStateCallback {
    CourseComponent unit;

    /**
     * Create a new instance of fragment
     */
    static CourseUnitEmptyFragment newInstance(CourseComponent unit) {
        CourseUnitEmptyFragment f = new CourseUnitEmptyFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        f.setArguments(args);

        return f;
    }

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unit = getArguments() == null ? null :
            (CourseComponent) getArguments().getSerializable(Router.EXTRA_COURSE_UNIT);
    }

    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_unit_empty, container, false);
        TextView laptop = (TextView)v.findViewById(R.id.watch_on_web_icon);
        Iconify.setIcon(laptop, Iconify.IconValue.fa_laptop);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPageShow() {

    }

    @Override
    public void onPageDisappear() {

    }
}
