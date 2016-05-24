package org.edx.mobile.base;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.event.LogoutEvent;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.event.NewVersionAvailableEvent;
import org.edx.mobile.interfaces.NetworkObserver;
import org.edx.mobile.interfaces.NetworkSubject;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.ViewAnimationUtil;
import org.edx.mobile.view.ICommonUI;
import org.edx.mobile.view.NavigationFragment;
import org.edx.mobile.view.common.BannerDisplayCallback;
import org.edx.mobile.view.common.BannerType;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public abstract class BaseFragmentActivity extends BaseAppActivity
        implements BannerDisplayCallback, NetworkSubject, ICommonUI {

    public static final String ACTION_SHOW_MESSAGE_INFO = "ACTION_SHOW_MESSAGE_INFO";
    public static final String ACTION_SHOW_MESSAGE_ERROR = "ACTION_SHOW_MESSAGE_ERROR";

    /**
     * The banner at the top for indicating offline mode or unsupported app version.
     */
    private View banner;
    /**
     * The banner text view.
     */
    private TextView bannerTextView;
    /**
     * The banner details slide-in popup.
     */
    private TextView bannerExpanded;
    /**
     * The type of the banner that is being displayed, or null if no banner is being
     * displayed.
     */
    private BannerType displayedBannerType;
    /**
     * Flag for determining if the sub-decoration has been set up.
     */
    private boolean subDecorInstalled;
    protected ActionBarDrawerToggle mDrawerToggle;
    //FIXME - we should not set a separate flag to indicate the status of UI component
    private boolean isUiOnline = true;
    private boolean isConnectedToWifi = false;
    private boolean isActivityStarted = false;
    @Inject
    protected IEdxEnvironment environment;


    private List<NetworkObserver> networkObservers = new ArrayList<NetworkObserver>();

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

    private final Handler handler = new Handler();
    protected final Logger logger = new Logger(getClass().getName());

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        // Ensure that the sub-decoration is initialized even without
        // any content view setup, to accommodate Fragment-only Activities.
        ensureSubDecor();

        logger.debug("created");
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityStarted = true;

        PrefManager pmFeatures = new PrefManager(this, PrefManager.Pref.FEATURES);


        // enabling action bar app icon.
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowHomeEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setIcon(android.R.color.transparent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this);
        try {
            DrawerLayout mDrawerLayout = (DrawerLayout)
                    findViewById(R.id.drawer_layout);
            if (mDrawerLayout != null) {

                Fragment frag = getSupportFragmentManager()
                        .findFragmentByTag("NavigationFragment");
                if (frag == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.slider_menu,
                                    new NavigationFragment(), "NavigationFragment").commit();
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    /* RoboAppCompatActivity injects the content view from
     * it's onCreate() implementation, so that needs to be
     * intercepted and the sub-decoration layout setup
     * before the content view setup, instead of relying on
     * setting this up in our own onCreate() implementation.
     */

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        ensureSubDecor();
        super.setContentView(layoutResID);
    }

    @Override
    public void setContentView(@NonNull View view) {
        ensureSubDecor();
        super.setContentView(view);
    }

    @Override
    public void setContentView(@NonNull View view, @Nullable ViewGroup.LayoutParams params) {
        ensureSubDecor();
        super.setContentView(view, params);
    }

    @Override
    public void addContentView(@NonNull View view, @Nullable ViewGroup.LayoutParams params) {
        ensureSubDecor();
        super.addContentView(view, params);
    }

    /**
     * Set up the basic decoration layout, such as the offline banner, and
     * the error and warning fly-in notifications.
     */
    private void ensureSubDecor() {
        if (!subDecorInstalled) {
            super.setContentView(R.layout.activity_base_decor);
            banner = findViewById(R.id.banner);
            bannerTextView = (TextView) findViewById(R.id.banner_text);
            bannerExpanded = (TextView) findViewById(R.id.banner_expanded);
            banner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewAnimationUtil.showMessageBar(bannerExpanded);
                }
            });

            /* Remove the ID from the old content parent, in favor of the
             * one defined by our sub-decoration layout. Note that this
             * approach only works for the AppCompatActivity implementation,
             * since it always gets the content parent from it's ID. If we
             * stop using the appcompat library at some point, then this
             * approach will need to be modified according to how
             * AppCompatActivity sets up it's own sub-decoration, by
             * intercepting all the setContentView() and addContentView()
             * calls, and manually adding the content view to the custom
             * designated parent.
             */
            getWindow().findViewById(android.R.id.content).setId(View.NO_ID);
            subDecorInstalled = true;
        }
    }

    @Override
    public void onContentChanged() {
        /* Don't call through to the super implementation if we're only
         * setting up the sub-decoration. The RoboAppCompatActivity
         * implementation does the view injections from this callback.
         */
        if (subDecorInstalled) {
            super.onContentChanged();
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

    //this is configure the Navigation Drawer of the application
    protected void configureDrawer() {
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.slider_menu, new NavigationFragment(),
                            "NavigationFragment").commit();

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                    R.string.label_open_drawer, R.string.label_close_drawer) {
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
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu) | createOptionsMenu(menu);
    }

    /**
     * Initialize the options menu. This is called from
     * {@link #onCreateOptionsMenu(Menu)}, so that subclasses can override
     * the base menu implementation while still calling back to the system
     * implementation. The selection handling for menu items defined here
     * should be performed in {@link #handleOptionsItemSelected(MenuItem)},
     * and any these methods should both be overriden together.
     *
     * @param menu The options menu.
     * @return Return true if the menu should be displayed.
     */
    protected boolean createOptionsMenu(Menu menu) {
        // No default menu for now
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Toggle navigation drawer when the app icon or title on the action bar
        // is clicked
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (handleOptionsItemSelected(item)) {
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

    /**
     * Handle options menu item selection. This is called from
     * {@link #onOptionsItemSelected(MenuItem)} to provide a menu
     * selection handler that can be overriden by subclass that override
     * {@link #createOptionsMenu(Menu)}, and should only be used to handle
     * selections of the menu items that are initialized from that method.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     */
    protected boolean handleOptionsItemSelected(MenuItem item) {
        // No default menu to handle selection for
        return false;
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
        // Pass any configuration change to the drawer toggls
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
     * Display the banner at the top of the Activity.
     *
     * @param bannerType The banner type
     */
    @Override
    public void showBanner(@NonNull BannerType bannerType) {
        bannerTextView.setText(bannerType.getShortMessageRes(this));
        bannerExpanded.setText(bannerType.getLongMessageRes(this));
        bannerExpanded.setOnClickListener(bannerType.getClickListener());
        banner.setVisibility(View.VISIBLE);
        displayedBannerType = bannerType;
    }

    /**
     * Hide the banner at the top of the Activity.
     */
    private void hideOfflineBanner() {
        if (displayedBannerType == BannerType.OFFLINE) {
            banner.setVisibility(View.GONE);
            displayedBannerType = null;
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

    /**
     * Call this method to inform user about going  offline
     */
    public void showOfflineAccessMessage() {
        try {
            animateLayouts(findViewById(R.id.offline_access_panel));
        } catch (Exception e) {
            logger.error(e);
        }
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
    public void onEvent(LogoutEvent event) {
        finish();
    }

    /**
     * callback from EventBus
     *
     * @param event
     */
    public void onEvent(NetworkConnectivityChangeEvent event) {

        logger.debug("network state changed");
        if (NetworkUtil.isConnected(this)) {
            if (!isUiOnline) {
                // only notify if previous state was NOT same
                isUiOnline = true;
                handler.post(new Runnable() {
                    public void run() {
                        onOnline();
                        notifyNetworkConnect();
                    }
                });
            }

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
            if (isUiOnline) {
                isUiOnline = false;
                handler.post(new Runnable() {
                    public void run() {
                        onOffline();
                        notifyNetworkDisconnect();
                    }
                });
            }
        }
    }

    /**
     * callback from EventBus
     *
     * @param event
     */
    public void onEvent(NewVersionAvailableEvent event) {
        bannerExpanded.setText(event.getNotificationString(this));
        ViewAnimationUtil.showMessageBar(bannerExpanded);
    }

    /**
     * Sub-classes may override this method to handle connected state.
     */
    protected void onOnline() {
        hideOfflineBanner();
        logger.debug("You are now online");
    }

    /**
     * Sub-classes may override this method to handle disconnected state.
     */
    protected void onOffline() {
        showBanner(BannerType.OFFLINE);
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
     * Returns user's profile.
     */
    protected ProfileModel getProfile() {
        PrefManager prefManager = new PrefManager(this, PrefManager.Pref.LOGIN);
        return prefManager.getCurrentUserProfile();
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

    public boolean hideErrorMessage() {
        LinearLayout error_layout = (LinearLayout) findViewById(R.id.error_layout);
        if (error_layout == null) {
            logger.warn("Error Layout not available, so couldn't show flying message");
            return false;
        }
        ViewAnimationUtil.hideMessageBar(error_layout);
        return true;
    }

    @Override
    public boolean tryToSetUIInteraction(boolean enable) {
        return false;
    }

}
