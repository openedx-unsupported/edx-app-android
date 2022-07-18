package org.edx.mobile.base

import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastStateListener
import org.edx.mobile.R
import org.edx.mobile.event.NewRelicEvent
import org.edx.mobile.googlecast.GoogleCastDelegate
import org.edx.mobile.logger.Logger
import org.greenrobot.eventbus.EventBus

abstract class BaseAppActivity : AppCompatActivity(), CastStateListener {

    private var googleCastDelegate: GoogleCastDelegate? = null
    private var mediaRouteMenuItem: MenuItem? = null
    private val logger = Logger(
        BaseAppActivity::class.java.name
    )
    var isInForeground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().post(NewRelicEvent(javaClass.simpleName))
        googleCastDelegate = GoogleCastDelegate.getInstance(
            MainApplication.getEnvironment(this)
                .analyticsRegistry
        )
        googleCastDelegate?.addCastStateListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // TODO: Replace the try-catch block with more appropriate logic so CI build get passed.
        // Can't access the CastButtonFactory while executing test cases of "CourseUnitNavigationActivityTest"
        // and throw exception.
        try {
            if ((googleCastDelegate?.isConnected == true || showGoogleCastButton())) {
                menuInflater.inflate(R.menu.google_cast_menu_item, menu)
                mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(
                    applicationContext,
                    menu, R.id.media_route_menu_item
                )
                // show the introduction overlay.
                if (isInForeground && resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    googleCastDelegate?.showIntroductoryOverlay(this, mediaRouteMenuItem)
                }
            }
        } catch (e: Exception) {
            logger.error(e, true)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        // refresh the menu items to update the current state of google cast button
        invalidateOptionsMenu()
        super.onResume()
        isInForeground = true
    }

    override fun onPause() {
        super.onPause()
        isInForeground = false
    }

    override fun onDestroy() {
        googleCastDelegate?.removeCastStateListener(this)
        super.onDestroy()
    }

    /**
     * @return True if screen needs to show the Google chrome un-casted button otherwise False.
     */
    open fun showGoogleCastButton(): Boolean {
        return false
    }

    override fun onCastStateChanged(newState: Int) {
        /* App throws `IllegalArgumentException` when showing the Introductory Overlay in some cases.
         * Check the following issue for more details (still open).
         * Ref: https://issuetracker.google.com/issues/36191274
         * TODO: Replace the try-catch block with more appropriate logic / by updating the cast library
         * as part of the Jira story: https://openedx.atlassian.net/browse/LEARNER-7722
         */
        try {
            if (isInForeground) {
                if (mediaRouteMenuItem != null) {
                    googleCastDelegate?.showIntroductoryOverlay(this, mediaRouteMenuItem)
                }
                invalidateOptionsMenu()
            }
        } catch (e: Exception) {
            logger.error(e, true)
        }
    }
}
