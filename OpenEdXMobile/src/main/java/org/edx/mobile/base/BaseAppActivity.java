package org.edx.mobile.base;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastStateListener;

import org.edx.mobile.R;
import org.edx.mobile.event.NewRelicEvent;
import org.edx.mobile.googlecast.GoogleCastDelegate;

import de.greenrobot.event.EventBus;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class BaseAppActivity extends RoboAppCompatActivity implements CastStateListener {

    private GoogleCastDelegate googleCastDelegate;
    private MenuItem mediaRouteMenuItem;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().post(new NewRelicEvent(getClass().getSimpleName()));
        googleCastDelegate = GoogleCastDelegate.getInstance(MainApplication.getEnvironment(this)
                .getAnalyticsRegistry());
        googleCastDelegate.addCastStateListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO: Replace the try-catch block with more appropriate logic so Travis-ci build get passed.
        // Can't access the CastButtonFactory while executing test cases of "CourseUnitNavigationActivityTest"
        // and throw exception.
        try {
            if (googleCastDelegate != null && (googleCastDelegate.isConnected() || showGoogleCastButton())) {
                getMenuInflater().inflate(R.menu.google_cast_menu_item, menu);
                mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                        menu, R.id.media_route_menu_item);
                // show the introduction overlay.
                if (isInForeground()) {
                    googleCastDelegate.showIntroductoryOverlay(this, mediaRouteMenuItem);
                }
            }
        } catch (Exception ignore) {
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        // refresh the menu items to update the current state of google cast button
        invalidateOptionsMenu();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (googleCastDelegate != null) {
            googleCastDelegate.removeCastStateListener(this);
        }
        super.onDestroy();
    }

    /**
     * @return True if screen needs to show the Google chrome un-casted button otherwise False.
     */
    public boolean showGoogleCastButton() {
        return false;
    }

    @Override
    public void onCastStateChanged(int newState) {
        if (mediaRouteMenuItem != null) {
            googleCastDelegate.showIntroductoryOverlay(this, mediaRouteMenuItem);
        }
        invalidateOptionsMenu();
    }
}
