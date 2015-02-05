package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;

import org.edx.mobile.base.BaseFragmentActivity;

/**
 * Created by aleffert on 1/30/15.
 */
public class Router {

    static private Router sInstance;

    // Note that this is not thread safe. The expectation is that this only happens
    // immediately when the app launches or synchronously at the start of a test.
    public static void setInstance(Router router) {
        sInstance = router;
    }

    public static Router getInstance() {
        return sInstance;
    }

    public void showDownloads(Activity sourceActivity) {
        Intent downloadIntent = new Intent(sourceActivity, DownloadListActivity.class);
        sourceActivity.startActivity(downloadIntent);
    }

    public void showLaunchScreen(Activity sourceActivity) {
        Intent launchIntent = new Intent(sourceActivity, LaunchActivity.class);
        sourceActivity.startActivity(launchIntent);
    }

    public void showLogin(Activity sourceActivity) {
        Intent launchIntent = new Intent(sourceActivity, LoginActivity.class);
        sourceActivity.startActivity(launchIntent);
    }

    public void showRegistration(Activity sourceActivity) {
        Intent launchIntent = new Intent(sourceActivity, LoginActivity.class);
        sourceActivity.startActivity(launchIntent);
    }
}
