package org.edx.mobile.base;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.event.FlyingMessageEvent;
import org.edx.mobile.event.LogoutEvent;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.interfaces.NetworkObserver;
import org.edx.mobile.interfaces.NetworkSubject;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.module.storage.Storage;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.LayoutAnimationControllerUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.view.ICommonUI;
import org.edx.mobile.view.NavigationFragment;
import org.edx.mobile.view.Router;
import org.edx.mobile.view.custom.ProgressWheel;
import org.edx.mobile.view.dialog.WebViewDialogFragment;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class BaseFragmentActivity extends FragmentActivity implements NetworkSubject, ICommonUI {

    public static final String ACTION_SHOW_MESSAGE_INFO = "ACTION_SHOW_MESSAGE_INFO";
    public static final String ACTION_SHOW_MESSAGE_ERROR = "ACTION_SHOW_MESSAGE_ERROR";
    // per second callback
    private static final int MSG_TYPE_TICK = 9302;

    private ProgressWheel totalProgress;
    private MenuItem progressMenuItem;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean isOnline = false;
    private boolean isConnectedToWifi = false;
    private boolean applyPrevTransitionOnRestart = false;
    private boolean isActivityStarted = false;
    protected IDatabase db;
    protected IStorage storage;
    protected ISegment segIO;

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

    protected boolean runOnTick = true;
    protected final Logger logger = new Logger(getClass().getName());

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        // for landscape player use full screen theme, otherwise, only hide title bar
        if (isLandscape()) {
            setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
        }

        initDB();
        try{
            applyTransitionNext();
        }catch(Exception ex){
            logger.error(ex);
        }


        updateActionBarShadow();

        logger.debug( "created");
    }

    @Override
    public void startActivity(Intent intent) {
        try{
            super.startActivity(intent);
            applyTransitionNext();
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityStarted = true;

        handler.sendEmptyMessage(MSG_TYPE_TICK);

        PrefManager pmFeatures = new PrefManager(this, PrefManager.Pref.FEATURES);

        boolean enableSocialFeatures = NetworkUtil.isSocialFeatureFlagEnabled(this);

        pmFeatures.put(PrefManager.Key.ALLOW_SOCIAL_FEATURES, enableSocialFeatures);


        //Check if the the onTick method needs to be run
        //This has been done to handle unwanted call to onTick() from login screen
        if(runOnTick)
            handler.sendEmptyMessage(MSG_TYPE_TICK);

        // enabling action bar app icon.
        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
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
        isActivityStarted = true;
        logger.debug( "activity restarted");

        if (applyPrevTransitionOnRestart) {
            applyTransitionPrev();
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
    public void finish() {
        super.finish();
        applyTransitionPrev();
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

            /*
             * we want to disable the animation for ActionBarDrawerToggle V7
             *  http://stackoverflow.com/questions/27117243/disable-hamburger-to-back-arrow-animation-on-toolbar
             */
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
                    super.onDrawerSlide(drawerView, 0); // this disables the animation
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
        createOptionMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * TODO - we will refactor the base class, so we can use onCreateOptionsMenu()
     * directly
     * @param menu
     * @return
     */
    protected boolean createOptionMenu(Menu menu){
        // inflate menu from xml
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        MenuItem checkBox_menuItem = menu.findItem(R.id.delete_checkbox);
        if(checkBox_menuItem!=null){
            checkBox_menuItem.setVisible(false);
        }

        MenuItem offline_tvItem = menu.findItem(R.id.offline);
        MenuItem menuItem = menu.findItem(R.id.progress_download);
        if(AppConstants.offline_flag){
            offline_tvItem.setVisible(true);
            menuItem.setVisible(false);
        }else{
            offline_tvItem.setVisible(false);
            menuItem.setVisible(true);
            View view = menuItem.getActionView();
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Router.getInstance().showDownloads(BaseFragmentActivity.this);
                }
            });
        }
        return true;
    }

    /**
     * Called when invalidateOptionsMenu() is triggered
     * This method is used to initialize the ActionBar progress
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu != null) {
            progressMenuItem = menu.findItem(R.id.progress_download);
            //Check if the the onTick method needs to be run
            //This has been done to handle unwanted call to onTick() from login screen
            if(runOnTick)
                onTick();
        }
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle action bar actions click
        switch (item.getItemId()) {
            case android.R.id.home:
                //Called when user has pressed back on top of the Action Bar
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This function is overidden to set the font for Action bar title
     * @param title
     */
    @Override
    public void setTitle(CharSequence title) {
        try {
            ActionBar bar = getActionBar();
            if (bar != null && title!=null) {
                Typeface type = Typeface.createFromAsset(getAssets(),"fonts/OpenSans-Semibold.ttf");
                int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
                TextView titleTextView = (TextView) findViewById(titleId);
                if(titleTextView!=null){
                    titleTextView.setTextColor(getResources().getColor(R.color.grey_text_mycourse));
                    titleTextView.setTypeface(type);
                    bar.setTitle(title);
                }
            }
        }catch(Exception ex){
            logger.error(ex);
        }
    }

    public void setActionBarVisible(boolean visible){
        try {
            ActionBar bar = getActionBar();
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
        LayoutAnimationControllerUtil messageController;
        messageController = new LayoutAnimationControllerUtil(view);
        messageController.showMessageBar();
    }

    public void stopAnimation(View view){
        if(view!=null){
            LayoutAnimationControllerUtil messageController;
            messageController = new LayoutAnimationControllerUtil(view);
            messageController.stopAnimation();
        }
    }

    /**
     * Animate / show the download started message
     * @param message - Message to display on the Download Panel
     * @return boolean - Returns true if message shown, false otherwise.
     */
    public boolean showInfoMessage(String message){
        TextView infoMessageTv = (TextView) findViewById(R.id.downloadMessage);
        if(infoMessageTv!=null) {
            infoMessageTv.setText(message);
            animateLayouts(infoMessageTv);
            return true;
        } else {
            logger.warn("TextView not available, so couldn't show flying message");
        }

        return false;
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
     * Sub-classes might override this method to execute the code each second.
     */
    protected void onTick() {
        // this is a per second callback
        try {
            if (progressMenuItem != null) {
                if(AppConstants.offline_flag){
                    progressMenuItem.setVisible(false);
                }else{
                    if(db!=null){
                        boolean downloading = db.isAnyVideoDownloading(null);
                        if(!downloading){
                            progressMenuItem.setVisible(false);
                        }else{
                            updateDownloadingProgress();
                        }
                    }   //store not null check
                }
            }                               //progress menu item not null check
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    //Update the Progress if Videos are downloading 
    private void updateDownloadingProgress() {
        if(storage!=null){
            try {
                View view = progressMenuItem.getActionView();
                if (view != null) {
                    totalProgress = (ProgressWheel) view
                            .findViewById(R.id.progress_wheel);
                    if (totalProgress != null) {
                        progressMenuItem.setVisible(true);
                    }else{
                        progressMenuItem.setVisible(false);
                    }
                    storage.getAverageDownloadProgress(averageProgressCallback);
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }



    public void onEvent(LogoutEvent event){
        finish();
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
    public void onEvent(NetworkConnectivityChangeEvent event){

        logger.debug("network state changed");
        if (NetworkUtil.isConnected(this)) {
            if ( !isOnline) {
                // only notify if previous state was NOT same
                isOnline = true;
                handler.post(new Runnable() {
                    public void run() {
                        AppConstants.offline_flag = false;
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
            if (isOnline) {
                isOnline = false;
                handler.post(new Runnable() {
                    public void run() {
                        AppConstants.offline_flag = true;
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
        AppConstants.offline_flag = false;
        logger.debug("You are now online");
    }

    /**
     * Sub-classes may override this method to handle disconnected state.
     */
    protected void onOffline() {
        AppConstants.offline_flag = true;
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

    private void applyTransitionNext() {
        // apply slide transition animation
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
        logger.debug( "next transition animation applied");
    }

    private void applyTransitionPrev() {
        // apply slide transition animation
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
        logger.debug( "prev transition animation applied");
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MSG_TYPE_TICK) {
                // This block will be executed per second,
                // so OnTick() is a per-second callback
                if (isActivityStarted) {
                    onTick();
                    sendEmptyMessageDelayed(MSG_TYPE_TICK, 1000);
                }
            }
        }
    };

    private void initDB() {
        storage = new Storage(this);

        UserPrefs userprefs = new UserPrefs(this);
        String username = null;
        ProfileModel profile = userprefs.getProfile();
        if(profile!=null){
            username =profile.username;
        }
        db = DatabaseFactory.getInstance( DatabaseFactory.TYPE_DATABASE_NATIVE );

        segIO = SegmentFactory.getInstance();
    }

    private DataCallback<Integer> averageProgressCallback = new DataCallback<Integer>() {
        @Override
        public void onResult(Integer result) {
            int progressPercent = result;
            if(progressPercent >= 0 && progressPercent <= 100){
                updateDownloadProgress(progressPercent);
            }
        }
        @Override
        public void onFail(Exception ex) {
            logger.error(ex);
        }
    };


    protected void updateDownloadProgress(int progressPercent){
        if ( totalProgress != null)
            totalProgress.setProgressPercent(progressPercent);
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


    private boolean showErrorMessage(String header, String message) {
        try {
            LinearLayout error_layout = (LinearLayout) findViewById(R.id.error_layout);
            if(error_layout!=null){
                TextView errorHeader = (TextView) findViewById(R.id.error_header);
                TextView errorMessage = (TextView) findViewById(R.id.error_message);
                if(header==null || header.isEmpty()){
                   errorHeader.setVisibility(View.GONE);
                }else{
                    errorHeader.setVisibility(View.VISIBLE);
                    errorHeader.setText(header);
                }
                if (message != null) {
                    errorMessage.setText(message);
                }
                UiUtil.animateLayouts(error_layout);
                return true;
            }else{
                logger.warn("Error Layout not available, so couldn't show flying message");
                return false;
            }
        }catch(Exception e){
            logger.error(e);
        }
        logger.warn("Error Layout not available, so couldn't show flying message");
        return false;
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
