package org.edx.mobile.launch;

import org.edx.mobile.R;
import org.edx.mobile.base.RuntimeApplication;
import org.edx.mobile.view.PresenterActivityTest;
import org.edx.mobile.view.launch.LaunchActivity;
import org.edx.mobile.view.launch.LaunchPresenter;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.android.api.Assertions.assertThat;


/**
 * Created by cleeedx on 5/20/16.
 */
public class LaunchActivityTest extends PresenterActivityTest<LaunchActivity, LaunchPresenter, LaunchPresenter.LaunchViewInterface> {

    @Before
    public void setup() {
        startActivity(LaunchActivity.newIntent(RuntimeApplication.application));
    }

    @Test
    public void testSetCourseDiscoveryButton_withCourseDiscoverOnLaunchEnabled_courseDiscoverButtonIsVisible() {
        view.setCourseDiscoveryButton(true);
        assertThat(activity.findViewById(R.id.course_discovery_button)).isVisible();
        assertThat(activity.findViewById(R.id.launch_sign_in_button)).isVisible();
        assertThat(activity.findViewById(R.id.launch_sign_up_button)).isVisible();
    }

    @Test
    public void testSetCourseDiscoveryButton_withCourseDiscoverOnLaunchDisabled_courseDiscoverButtonIsNotVisible() {
        view.setCourseDiscoveryButton(false);
        assertThat(activity.findViewById(R.id.course_discovery_button)).isNotVisible();
        assertThat(activity.findViewById(R.id.launch_sign_in_button)).isVisible();
        assertThat(activity.findViewById(R.id.launch_sign_up_button)).isVisible();
    }
}
