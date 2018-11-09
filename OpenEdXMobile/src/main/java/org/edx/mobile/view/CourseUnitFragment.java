package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.evernote.android.state.State;
import com.google.inject.Inject;
import com.livefront.bridge.Bridge;

import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.view.common.PageViewStateCallback;
import org.edx.mobile.view.common.RunnableCourseComponent;

public abstract class CourseUnitFragment extends BaseFragment implements PageViewStateCallback, RunnableCourseComponent {
    public interface HasComponent {
        CourseComponent getComponent();
        void navigateNextComponent();
        void navigatePreviousComponent();
    }

    @State
    protected CourseComponent unit;
    protected HasComponent hasComponentCallback;

    @Inject
    IEdxEnvironment environment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            final Bundle args = getArguments();
            unit = (CourseComponent) getArguments().getSerializable(Router.EXTRA_COURSE_UNIT);
            if (unit != null) {
                /*
                The size of `unit` object could be very big in some courses that have lots of
                sections/subsections/units in them. So, we need to ensure that this object isn't
                stored in the fragment's extras otherwise we might encounter
                TransactionTooLargeException.
                 */
                args.putSerializable(Router.EXTRA_COURSE_UNIT, null);
                setArguments(args);
            }
        }
        /*
        To retain the `unit` object during fragment recreation, we're relying on the Bridge library
        which'll write the object to disk and allow us to restore while the fragment is being
        recreated. Consequently, avoiding the TransactionTooLargeException.
        More info:
        - https://medium.com/@mdmasudparvez/android-os-transactiontoolargeexception-on-nougat-solved-3b6e30597345
        - https://github.com/livefront/bridge
        - https://openedx.atlassian.net/browse/LEARNER-6680
        */
        Bridge.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bridge.saveInstanceState(this, outState);
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
    public abstract void run();

    public void setHasComponentCallback(HasComponent callback) {
        hasComponentCallback = callback;
    }
}
