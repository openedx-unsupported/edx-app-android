package org.edx.mobile.base;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.event.LogoutEvent;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.interfaces.NetworkObserver;
import org.edx.mobile.interfaces.NetworkSubject;
import org.edx.mobile.interfaces.OnActivityResultListener;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.ViewAnimationUtil;
import org.edx.mobile.view.ICommonUI;
import org.edx.mobile.view.NavigationFragment;
import org.edx.mobile.view.dialog.AlertDialogFragment;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public abstract class BaseFragmentActivity extends BaseAppActivity
        implements NetworkSubject, ICommonUI, OnActivityResultListener {
    private final Handler handler = new Handler();
    protected final Logger logger = new Logger(getClass().getName());

    private boolean isConnectedToWifi = false;
    private boolean isActivityStarted = false;
    protected ActionBarDrawerToggle mDrawerToggle;

    @Inject
    protected IEdxEnvironment environment;
    private List<NetworkObserver> networkObservers = new ArrayList<>();

    public void registerNetworkObserver(NetworkObserver observer) {
        if (observer != null && !networkObservers.contains(observer)) {
            networkObservers.add(observer);
        }
    }

    public void unregisterNetworkObserver(NetworkObserver observer) {
        if (observer != null && networkObservers.contains(observer)) {
            networkObservers.remove(observer);
        }
    }

    @Override
    public void notifyNetworkDisconnect() {
        for (NetworkObserver o : networkObservers) {
            o.onOffline();
        }
    }

    @Override
    public void notifyNetworkConnect() {
        for (NetworkObserver o : networkObservers) {
            o.onOnline();
        }
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowHomeEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setIcon(android.R.color.transparent);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!NetworkUtil.isConnected(this)) {
            // Currently we are sending this event again, so that offline SnackBar can appear
            // when we return to a screen.
            EventBus.getDefault().post(new NetworkConnectivityChangeEvent());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityStarted = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this);
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            Fragment frag = getSupportFragmentManager().findFragmentByTag("NavigationFragment");
            if (frag == null) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.slider_menu,
                        new NavigationFragment(),
                        "NavigationFragment").commit();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityStarted = false;
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            // App crashes on a few devices (mostly 4.0.+) on super method call
            // This is a workaround to avoid app crash, app still works even if Exception occurs
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    //this is configure the Navigation Drawer of the application
    protected void configureDrawer() {
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.slider_menu, new NavigationFragment(),
                            "NavigationFragment").commit();

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                    R.string.label_open_navigation_menu, R.string.label_close_navigation_menu) {
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    invalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    Fragment frag = getSupportFragmentManager().
                            findFragmentByTag("NavigationFragment");
                    if (frag == null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.slider_menu, new NavigationFragment(),
                                        "NavigationFragment").commit();
                    }
                    invalidateOptionsMenu();
                }

                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    super.onDrawerSlide(drawerView, 0); // this disables the animation
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);
        }
    }

    /**
     * Call this function if you do not want to allow
     * opening/showing the drawer(Navigation Fragment) on swiping left to right
     */
    protected void blockDrawerFromOpening() {
        DrawerLayout drawerLayout = (DrawerLayout)
                findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    //Closing the Navigation Drawer
    public void closeDrawer() {
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawers();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Toggle navigation drawer when the app icon or title on the action bar
        // is clicked
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle action bar buttons click
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setActionBarVisible(boolean visible) {
        try {
            ActionBar bar = getSupportActionBar();
            if (bar != null) {
                if (visible)
                    bar.show();
                else
                    bar.hide();
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    public void animateLayouts(View view) {
        if (view == null) {
            logger.warn("Null view cannot be animated!");
            return;
        }
        ViewAnimationUtil.showMessageBar(view);
    }

    public void stopAnimation(View view) {
        if (view != null) {
            ViewAnimationUtil.stopAnimation(view);
        }
    }

    /**
     * Animate / show the download started message
     *
     * @param message - Message to display on the Download Panel
     * @return boolean - Returns true if message shown, false otherwise.
     */
    public boolean showInfoMessage(String message) {
        TextView infoMessageTv = (TextView) findViewById(R.id.flying_message);
        if (infoMessageTv != null) {
            infoMessageTv.setText(message);
            animateLayouts(infoMessageTv);
            return true;
        } else {
            logger.warn("TextView not available, so couldn't show flying message");
        }

        return false;
    }

    /**
     * Hides the info message view if its visible with animation
     *
     * @return <code>true<code/> if the view was hidden successfully otherwise <code>false</code>
     */
    public boolean hideInfoMessage() {
        View messageView = findViewById(R.id.flying_message);
        if (messageView == null) {
            logger.warn("Message view not available, so couldn't hide flying message");
            return false;
        }
        ViewAnimationUtil.hideMessageBar(messageView);
        return true;
    }

    public boolean isActivityStarted() {
        return isActivityStarted;
    }

    /**
     * Returns true if current orientation is LANDSCAPE, false otherwise.
     */
    protected boolean isLandscape() {
        return (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    /**
     * callback from EventBus
     *
     * @param event
     */
    @SuppressWarnings("unused")
    public void onEvent(LogoutEvent event) {
        finish();
    }

    /**
     * callback from EventBus
     *
     * @param event
     */
    @SuppressWarnings("unused")
    public void onEvent(NetworkConnectivityChangeEvent event) {

        logger.debug("network state changed");
        if (NetworkUtil.isConnected(this)) {
            handler.post(new Runnable() {
                public void run() {
                    onOnline();
                    notifyNetworkConnect();
                }
            });

            if (NetworkUtil.isConnectedWifi(this)) {
                if (!isConnectedToWifi) {
                    isConnectedToWifi = true;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onConnectedToWifi();
                        }
                    });
                }
            } else if (NetworkUtil.isConnectedMobile(this)) {
                if (isConnectedToWifi) {
                    isConnectedToWifi = false;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onConnectedToMobile();
                        }
                    });
                }
            }
        } else {
            handler.post(new Runnable() {
                public void run() {
                    onOffline();
                    notifyNetworkDisconnect();
                }
            });

        }
    }

    public boolean showErrorMessage(String header, String message) {
        return showErrorMessage(header, message, true);
    }

    public boolean showErrorMessage(String header, String message, boolean isPersistent) {
        LinearLayout error_layout = (LinearLayout) findViewById(R.id.error_layout);
        if (error_layout == null) {
            logger.warn("Error Layout not available, so couldn't show flying message");
            return false;
        }
        TextView errorHeader = (TextView) findViewById(R.id.error_header);
        TextView errorMessageView = (TextView) findViewById(R.id.error_message);
        if (header == null || header.isEmpty()) {
            errorHeader.setVisibility(View.GONE);
        } else {
            errorHeader.setVisibility(View.VISIBLE);
            errorHeader.setText(header);
        }
        if (message != null) {
            errorMessageView.setText(message);
        }
        ViewAnimationUtil.showMessageBar(error_layout, isPersistent);
        return true;
    }

    /**
     * Sub-classes may override this method to handle connected state.
     */
    protected void onOnline() {
        logger.debug("You are now online");
    }

    /**
     * Sub-classes may override this method to handle disconnected state.
     */
    protected void onOffline() {
        logger.debug("You are now offline");
    }

    /**
     * Gets called whenever network state is changed and device is now connected to mobile data.
     * Sub-classes may override this method to handle when mobile data is connected.
     * This method is called after {@link #onOnline()} method.
     */
    protected void onConnectedToMobile() {
    }

    /**
     * Gets called whenever network state is changed and device is now connected to wifi.
     * Sub-classes may override this method to handle when wifi is connected.
     * This method is called after {@link #onOnline()} method.
     */
    protected void onConnectedToWifi() {
    }

    /**
     * Blocks touch event for this activity.
     * Use {@link #unblockTouch()} method to unblock and activate touch events.
     */
    protected void blockTouch() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    /**
     * Unblocks touch event for this activity.
     * This might should be called to unblock touch events that were blocked by {@link #blockTouch()} method.
     */
    protected void unblockTouch() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    protected void hideSoftKeypad() {
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public void showAlertDialog(@Nullable String title, @NonNull String message) {
        showAlertDialog(title, message, null);
    }

    public void showAlertDialog(@Nullable String title, @NonNull String message, @Nullable DialogInterface.OnClickListener onPositiveClick) {
        if (isInForeground) {
            AlertDialogFragment.newInstance(title, message, onPositiveClick).show(getSupportFragmentManager(), null);
        }
    }

    public void showAlertDialog(@Nullable String title, @NonNull String message,
                                @NonNull String  positiveButtonText,
                                @Nullable DialogInterface.OnClickListener onPositiveClick,
                                @Nullable String  negativeButtonText,
                                @Nullable DialogInterface.OnClickListener onNegativeClick) {
        if (isInForeground) {
            AlertDialogFragment.newInstance(title, message, positiveButtonText, onPositiveClick, negativeButtonText, onNegativeClick)
                    .show(getSupportFragmentManager(), null);
        }
    }

    @Override
    public boolean tryToSetUIInteraction(boolean enable) {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>To conform with the {@link OnActivityResultListener} interface this function has been
     * implemented emptily, making it publicly accessible.</p>
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
