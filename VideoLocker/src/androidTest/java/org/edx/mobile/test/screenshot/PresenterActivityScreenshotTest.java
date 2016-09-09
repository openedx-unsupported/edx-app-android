package org.edx.mobile.test.screenshot;

import com.facebook.testing.screenshot.Screenshot;

import org.edx.mobile.view.Presenter;
import org.edx.mobile.view.PresenterActivity;
import org.edx.mobile.view.PresenterActivityInstrumentationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * Extend this class to create an instrumentation test that automatically:
 * Starts your activity with a mock presenter (see {@link PresenterActivityInstrumentationTest}).
 * Snaps a screenshot of your activity at the end of each test.
 */
public abstract class PresenterActivityScreenshotTest<ActivityT extends PresenterActivity<PresenterT, ViewT>, PresenterT extends Presenter<ViewT>, ViewT> extends PresenterActivityInstrumentationTest<ActivityT, PresenterT, ViewT> {

    @Rule
    public TestName testName = new TestName();

    @Before
    public void before() {
        startActivity(null);
    }

    @After
    public void after() {
        Screenshot.snap(activity.findViewById(android.R.id.content)).setName(getClass().getName() + "_" + testName.getMethodName()).record();
    }
}
