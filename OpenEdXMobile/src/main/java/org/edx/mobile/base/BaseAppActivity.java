package org.edx.mobile.base;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastStateListener;

import org.edx.mobile.R;
import org.edx.mobile.event.NewRelicEvent;
import org.edx.mobile.googlecast.GoogleCastDelegate;
import org.edx.mobile.logger.Logger;

import de.greenrobot.event.EventBus;

public abstract class BaseAppActivity extends RoboAppCompatActivity implements CastStateListener {

    private GoogleCastDelegate googleCastDelegate;
    private MenuItem mediaRouteMenuItem;
    private final Logger logger = new Logger(BaseAppActivity.class.getName());

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
                if (isInForeground() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    googleCastDelegate.showIntroductoryOverlay(this, mediaRouteMenuItem);
                }
            }
        } catch (Exception e) {
            logger.error(e, true);
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
        /* App throws `IllegalArgumentException` when showing the Introductory Overlay in some cases.
         * Check the following issue for more details (still open).
         * Ref: https://issuetracker.google.com/issues/36191274
         * TODO: Replace the try-catch block with more appropriate logic / by updating the cast library
         * as part of the Jira story: https://openedx.atlassian.net/browse/LEARNER-7722
         */
        try {
            if (isInForeground()) {
                if (mediaRouteMenuItem != null) {
                    googleCastDelegate.showIntroductoryOverlay(this, mediaRouteMenuItem);
                }
                invalidateOptionsMenu();
            }
        } catch (Exception e) {
            logger.error(e, true);
        }
    }
}
