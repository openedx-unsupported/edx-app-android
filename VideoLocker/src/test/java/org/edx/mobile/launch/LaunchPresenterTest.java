package org.edx.mobile.launch;

import org.edx.mobile.test.PresenterTest;
import org.edx.mobile.util.Config;
import org.edx.mobile.view.launch.LaunchPresenter;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created by cleeedx on 5/20/16.
 */
public class LaunchPresenterTest extends PresenterTest<LaunchPresenter, LaunchPresenter.LaunchViewInterface> {

    @Mock
    Config config;

    @Test
    public void testSetCourseDiscoveryButton_withCourseDiscoverOnLaunchEnabled_courseDiscoverButtonIsSet() {
        when(config.isCourseDiscoveryOnLaunchEnabled()).thenReturn(false);
        startPresenter(new LaunchPresenter(config));
        verify(view).setCourseDiscoveryButton(false);
    }

    @Test
    public void testSetCourseDiscoveryButton_withCourseDiscoverOnLaunchDisabled_courseDiscoverButtonIsSet() {
        when(config.isCourseDiscoveryOnLaunchEnabled()).thenReturn(true);
        startPresenter(new LaunchPresenter(config));
        verify(view).setCourseDiscoveryButton(true);
    }
}
