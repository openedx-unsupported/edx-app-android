package org.edx.mobile.test.screenshot;

import android.test.InstrumentationTestCase;
import android.view.LayoutInflater;
import android.view.View;

import com.facebook.testing.screenshot.Screenshot;
import com.facebook.testing.screenshot.ViewHelpers;

import org.edx.mobile.R;

public class LaunchScreenshotTests extends InstrumentationTestCase {
    public void test_takeScreenshotOf_launchScreen() throws Throwable {
        LayoutInflater inflater = LayoutInflater.from(getInstrumentation().getTargetContext());
        View view = inflater.inflate(R.layout.activity_launch, null, false);

        ViewHelpers.setupView(view)
                .setExactWidthDp(500)
                .layout();

        Screenshot.snap(view)
                .record();
    }
}
