package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.view.common.PageViewStateCallback;
import org.edx.mobile.view.common.RunnableCourseComponent;

/**
 * Created by hanning on 6/9/15.
 */
public class CourseUnitFragment  extends Fragment implements PageViewStateCallback, RunnableCourseComponent {

    public static interface HasComponent {
        CourseComponent getComponent();
    }

    protected CourseComponent unit;
    protected HasComponent hasComponentCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unit = getArguments() == null ? null :
            (CourseComponent) getArguments().getSerializable(Router.EXTRA_COURSE_UNIT);
    }

    @Override
    public void onPageShow() {

    }

    @Override
    public void onPageDisappear() {

    }

    @Override
    public CourseComponent getCourseComponent() {
        return unit;
    }

    @Override
    public void run() {

    }

    public void setHasComponentCallback(HasComponent callback){
        hasComponentCallback = callback;
    }
}
