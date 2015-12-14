package org.edx.mobile.base;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.event.FlyingMessageEvent;
import org.edx.mobile.event.LogoutEvent;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.interfaces.NetworkObserver;
import org.edx.mobile.interfaces.NetworkSubject;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.ViewAnimationUtil;
import org.edx.mobile.view.ICommonUI;
import org.edx.mobile.view.NavigationFragment;
import org.edx.mobile.view.dialog.WebViewDialogFragment;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public abstract class BaseFragmentActivity extends RoboAppCompatActivity
        implements NetworkSubject, ICommonUI {

    public static final String ACTION_SHOW_MESSAGE_INFO = "ACTION_SHOW_MESSAGE_INFO";
    public static final String ACTION_SHOW_MESSAGE_ERROR = "ACTION_SHOW_MESSAGE_ERROR";

    private MenuItem offlineMenuItem;
    private ActionBarDrawerToggle mDrawerToggle;
    //FIXME - we should not set a separate flag to indicate the status of UI component
    private boolean isUiOnline = true;
    private boolean isConnectedToWifi = false;
    private boolean applyPrevTransitionOnRestart = false;
    private boolean isActivityStarted = false;
    @Inject
    protected IEdxEnvironment environment;


    private List<NetworkObserver> networkObservers = new ArrayList<NetworkObserver>();

    public void registerNetworkObserver(NetworkObserver observer){
        if(observer != null && !networkObservers.contains(observer)){
            networkObservers.add(observer);
        }
    }

    public void unregisterNetworkObserver(NetworkObserver observer){
        if(observer != null && networkObservers.contains(observer)){
            networkObservers.remove(observer);
        }
    }

    @Override
    public void notifyNetworkDisconnect(){
        for(NetworkObserver o : networkObservers){
            o.onOffline();
        }
    }

    @Override
    public void notifyNetworkConnect(){
        for(NetworkObserver o : networkObservers){
            o.onOnline();
        }
    }

    private final Handler handler = new Handler();
    protected final Logger logger = new Logger(getClass().getName());

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        updateActionBarShadow();

        logger.debug( "created");
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityStarted = true;

        PrefManager pmFeatures = new PrefManager(this, PrefManager.Pref.FEATURES);

        boolean enableSocialFeatures = NetworkUtil.isSocialFeatureFlagEnabled(this, environment.getConfig());

        pmFeatures.put(PrefManager.Key.ALLOW_SOCIAL_FEATURES, enableSocialFeatures);


        // enabling action bar app icon.
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowHomeEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setIcon(android.R.color.transparent);
            //If activity is in landscape, hide the Action bar
            if (isLandscape()) {
                bar.hide();
            }else{
                bar.show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this);
        try{
            DrawerLayout mDrawerLayout = (DrawerLayout)
                    findViewById(R.id.drawer_layout);
            if (mDrawerLayout != null) {

                Fragment frag = getSupportFragmentManager()
                        .findFragmentByTag("NavigationFragment");
                if(frag==null){
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.slider_menu,
                                    new NavigationFragment(),"NavigationFragment").commit();
                }
            }
        }catch(Exception ex){
            logger.error(ex);
        }
    }


    private void updateActionBarShadow() {
        //Check for JellyBeans version
        if (Build.VERSION.SDK_INT == 18) {
            // Get the content view
            View contentView = findViewById(android.R.id.content);

            // Make sure it's a valid instance of a FrameLayout
            if (contentView instanceof FrameLayout) {
                TypedValue tv = new TypedValue();

                // Get the windowContentOverlay value of the current theme
                if (getTheme().resolveAttribute(
                        android.R.attr.windowContentOverlay, tv, true)) {

                    // If it's a valid resource, set it as the foreground drawable
                    // for the content view
                    if (tv.resourceId != 0) {
                        ((FrameLayout) contentView).setForeground(
                                getResources().getDrawable(tv.resourceId));
                    }
                }
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        isActivityStarted = false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        logger.debug("activity restarted");

        if (applyPrevTransitionOnRestart) {
            // apply slide transition animation
            overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
        }
        applyPrevTransitionOnRestart = false;
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
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    //this is configure the Navigation Drawer of the application
    protected void configureDrawer() {
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.slider_menu, new NavigationFragment(),"NavigationFragment").commit();

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                    R.string.label_close,  R.string.label_close ) {
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    invalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    super.onDrawerSlide(drawerView,0);
                    Fragment frag = getSupportFragmentManager().findFragmentByTag("NavigationFragment");
                    if(frag==null){
                        getSupportFragmentManager().beginTransaction()

                                .replace(R.id.slider_menu, new NavigationFragment(),"NavigationFragment").commit();
                    }
                    invalidateOptionsMenu();
                }

                public void onDrawerSlide(View drawerView, float slideOffset) {
                    // Disable the menu icon back arrow animation
                    // http://stackoverflow.com/questions/27117243/disable-hamburger-to-back-arrow-animation-on-toolbar
                    super.onDrawerSlide(drawerView, 0);
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);
        }
    }

    //Closing the Navigation Drawer
    public void closeDrawer(){
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(mDrawerLayout!=null){
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
     *
     * @return Return true if the menu should be displayed.
     */
    protected boolean createOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        offlineMenuItem = menu.findItem(R.id.offline);
        offlineMenuItem.setVisible(!NetworkUtil.isConnected(this));
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
     *
     * @return boolean Return false to allow normal menu processing to
     *         proceed, true to consume it here.
     */
    protected boolean handleOptionsItemSelected(MenuItem item) {
        return false;
    }

    /**
     * This function is overidden to set the font for Action bar title
     * @param title
     */
    @Override
    public void setTitle(CharSequence title) {
        try {
            final ActionBar bar = getSupportActionBar();
            if (bar != null && title!=null) {
                SpannableString s = new SpannableString(title);
                s.setSpan(new CustomTypefaceSpan(this, "OpenSans-Semibold.ttf"), 0, s.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                bar.setTitle(s);
            }
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    public void setActionBarVisible(boolean visible){
        try {
            ActionBar bar = getSupportActionBar();
            if (bar != null ) {
                if ( visible )
                    bar.show();
                else
                    bar.hide();
            }
        }catch(Exception ex){
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
        if (mDrawerToggle != null ) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        if (mDrawerToggle != null ) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    public void animateLayouts(View view){
        if (view == null) {
            logger.warn("Null view cannot be animated!");
            return;
        }
        ViewAnimationUtil.showMessageBar(view);
    }

    public void stopAnimation(View view){
        if(view!=null){
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
        TextView infoMessageTv = (TextView) findViewById(R.id.downloadMessage);
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
        View messageView = findViewById(R.id.downloadMessage);
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
        try{
            animateLayouts(findViewById(R.id.offline_access_panel));
        }catch(Exception e){
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


    public void setApplyPrevTransitionOnRestart(
            boolean applyPrevTransitionOnRestart) {
        //Set transition when activity restarts
        this.applyPrevTransitionOnRestart = applyPrevTransitionOnRestart;
    }

    /**
     * callback from EventBus
     * @param event
     */
    public void onEvent(LogoutEvent event){
        finish();
    }
    /**
     * callback from EventBus
     * @param event
     */
    public void onEvent(NetworkConnectivityChangeEvent event){

        logger.debug("network state changed");
        if (NetworkUtil.isConnected(this)) {
            if ( !isUiOnline) {
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
                if(!isConnectedToWifi){
                    isConnectedToWifi = true;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onConnectedToWifi();
                        }
                    });
                }
            } else if (NetworkUtil.isConnectedMobile(this)) {
                if(isConnectedToWifi){
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
     * Sub-classes may override this method to handle connected state.
     */
    protected void onOnline() {
        if (offlineMenuItem != null) {
            offlineMenuItem.setVisible(false);
        }
        logger.debug("You are now online");
    }

    /**
     * Sub-classes may override this method to handle disconnected state.
     */
    protected void onOffline() {
        if (offlineMenuItem != null) {
            offlineMenuItem.setVisible(true);
        }
        logger.debug ("You are now offline");
    }

    /**
     * Gets called whenever network state is changed and device is now connected to mobile data.
     * Sub-classes may override this method to handle when mobile data is connected.
     * This method is called after {@link #onOnline()} method.
     */
    protected void onConnectedToMobile() {}

    /**
     * Gets called whenever network state is changed and device is now connected to wifi.
     * Sub-classes may override this method to handle when wifi is connected.
     * This method is called after {@link #onOnline()} method.
     */
    protected void onConnectedToWifi() {}

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

    /**
     * Displays a dialog which has a WebView container to display contents of given URL.
     * @param url String
     * @param showTitle
     * @param dialogTitle
     */
    public void showWebDialog(String url, boolean showTitle, String dialogTitle) {
        //Show the dialog only if the activity is started. This is to avoid Illegal state
        //exceptions if the dialog fragment tries to show even if the application is not in foreground
        if(isActivityStarted()){
            WebViewDialogFragment webViewFragment = new WebViewDialogFragment();
            webViewFragment.setDialogContents(url, showTitle, dialogTitle);
            webViewFragment.setStyle(DialogFragment.STYLE_NORMAL,
                    android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            webViewFragment.setCancelable(false);
            webViewFragment.show(getSupportFragmentManager(), "web-view-dialog");
        }
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

    /**
     * This method should be implemented by {@link org.edx.mobile.view.MyCoursesListActivity}.
     */
    protected void reloadMyCoursesData() {
        // nothing to do here
    }

    /**
     * callback from eventbus
     * Receives the sticky broadcast message and attempts showing flying message.
     **/
    public void onEvent(FlyingMessageEvent event){
        try {
            if (event.type == FlyingMessageEvent.MessageType.INFO) {
                String message = event.message;
                if (showInfoMessage(message)) {
                    // make this message one-shot
                    EventBus.getDefault().removeStickyEvent(event);
                } else {
                    // may be some other screen will display this message
                    // do nothing here, do NOT remove broadcast
                }

                if (message.equalsIgnoreCase(getString(R.string.you_are_now_enrolled))) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            reloadMyCoursesData();
                        }
                    });
                }
            }

            else if (event.type == FlyingMessageEvent.MessageType.ERROR) {
                String header = event.title;
                String message = event.message;
                if (showErrorMessage(header, message)) {
                    // make this message one-shot
                    EventBus.getDefault().removeStickyEvent(event);
                } else {
                    // may be some other screen will display this message
                    // do nothing here, do NOT remove broadcast
                }
            }
        } catch(Exception ex) {
            logger.error(ex);
        }
    }


    @Override
    public boolean tryToSetUIInteraction(boolean enable){
        return false;
    }

}
