package org.edx.mobile.view;

import android.os.Bundle;

import com.google.inject.Inject;

import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.view.common.PageViewStateCallback;

public abstract class CourseUnitFragment extends BaseFragment implements PageViewStateCallback {
    public interface HasComponent {
        CourseComponent getComponent();

        void navigateNextComponent();

        void navigatePreviousComponent();
    }

    protected CourseComponent unit;
    protected HasComponent hasComponentCallback;

    @Inject
    IEdxEnvironment environment;

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

    public void setHasComponentCallback(HasComponent callback) {
        hasComponentCallback = callback;
    }
}
