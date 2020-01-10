package org.edx.mobile.view;

import android.os.Bundle;

import com.google.inject.Inject;

import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.model.course.CourseComponent;

public abstract class CourseUnitFragment extends BaseFragment {
    public interface HasComponent {
        CourseComponent getComponent();

        void navigateNextComponent();

        void navigatePreviousComponent();
    }

    protected CourseComponent unit;
    protected HasComponent hasComponentCallback;

    @Inject
    protected IEdxEnvironment environment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unit = getArguments() == null ? null :
                (CourseComponent) getArguments().getSerializable(Router.EXTRA_COURSE_UNIT);
    }

    public void setHasComponentCallback(HasComponent callback) {
        hasComponentCallback = callback;
    }

    /**
     * This method contains the status that screen has the Casting supported video content or not.
     *
     * @return true if screen has casting supported video content, else false
     */
    public boolean hasCastSupportedVideoContent() {
        return false;
    }
}
