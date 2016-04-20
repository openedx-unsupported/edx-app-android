package org.edx.mobile.test;

import android.os.Bundle;
import android.support.test.runner.AndroidJUnitRunner;

import com.facebook.testing.screenshot.ScreenshotRunner;

/**
 * Created by cleeedx on 4/20/16.
 */

public class EdXTestRunner extends AndroidJUnitRunner {
    @Override
    public void onCreate(Bundle args) {
        ScreenshotRunner.onCreate(this, args);
        super.onCreate(args);
    }

    @Override
    public void finish(int resultCode, Bundle results) {
        ScreenshotRunner.onDestroy();
        super.finish(resultCode, results);
    }

}
