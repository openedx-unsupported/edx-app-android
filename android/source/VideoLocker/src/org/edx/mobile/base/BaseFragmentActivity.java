package org.edx.mobile.base;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.analytics.SegmentTracker;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.module.storage.Storage;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.LayoutAnimationControllerUtil;
import org.edx.mobile.util.LogUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.DownloadListActivity;
import org.edx.mobile.view.NavigationFragment;
import org.edx.mobile.view.custom.ProgressWheel;
import org.edx.mobile.view.dialog.WebViewDialogFragment;

public class BaseFragmentActivity extends FragmentActivity {

    // per second callback
    private static final int MSG_TYPE_TICK = 9302;

    private ProgressWheel totalProgress;
    private MenuItem progressMenuItem;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean isOnline = false;
    private boolean applyPrevTransitionOnRestart = false;
    private boolean isActivityStarted = false;
    protected IDatabase db;
    protected IStorage storage;
    protected ISegment segIO;
    protected boolean runOnTick = true;

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
            ex.printStackTrace();
        }
        enableNetworkStateChangeCallback();
        enableLogoutCallback();
        updateActionBarShadow();

        LogUtil.log(getClass().getName(), "created");
    }

    @Override
    public void startActivity(Intent intent) {
        try{
            super.startActivity(intent);
            applyTransitionNext();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityStarted = true;

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
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            ex.printStackTrace();
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
        LogUtil.log(getClass().getName(), "activity restarted");

        if (applyPrevTransitionOnRestart) {
            applyTransitionPrev();
        }
        applyPrevTransitionOnRestart = false;
    }

    @Override
    protected void onDestroy() {
        disableNetworkStateChangedCallback();
        disableLogoutCallback();
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
            // FIXME: App crashes on a few devices (mostly 4.0.+) on super method call
            // This is a workaround to avoid app crash, app still works even if Exception occurs
            super.onRestoreInstanceState(savedInstanceState);
        } catch(Exception ex) {
            Log.e(getClass().getName(), "onRestoreInstanceState failed", ex);
        }
    }

    //this is configure the Navigation Drawer of the application
    protected void configureDrawer() {
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.slider_menu, new NavigationFragment(),"NavigationFragment").commit();

            /* This is commented because of issue on 4.4 devices on Action Bar
             R.string.label_my_courses, // nav drawer open - description for accessibility
             R.string.label_my_courses // nav drawer close - description for accessibility
             */
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                    R.drawable.ic_drawer, //nav menu toggle icon
                    0,0) {
                public void onDrawerClosed(View view) {
                    invalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {
                    Fragment frag = getSupportFragmentManager().findFragmentByTag("NavigationFragment");
                    if(frag==null){
                        getSupportFragmentManager().beginTransaction()

                                .replace(R.id.slider_menu, new NavigationFragment(),"NavigationFragment").commit();
                    }
                    invalidateOptionsMenu();
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
        // inflate menu from xml
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

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
                    Intent downloadIntent = new Intent(BaseFragmentActivity.this, DownloadListActivity.class);
                    startActivity(downloadIntent);
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
            ex.printStackTrace();
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


    /**
     * Prints given message in the debug log with class name as "tag".
     * This method prints log only in the debug mode.
     * @param msg : This message is to print the log message
     */
    protected void log(String msg) {
        LogUtil.log(getClass().getName(), msg);
    }

    public void animateLayouts(View view){
        if (view == null) {
            LogUtil.error(getClass().getName(), "null view cannot be animated!");
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
     */
    public void showMessage(String message){
        TextView downloadMessageTv = (TextView) findViewById(R.id.downloadMessage);
        if(downloadMessageTv!=null){
            downloadMessageTv.setText(message);
            animateLayouts(downloadMessageTv);
        }
    }

    /**
     * Call this method to inform user about going  offline
     */
    public void showOfflineAccessMessage() {
        try{
            animateLayouts(findViewById(R.id.offline_access_panel));
        }catch(Exception e){
            e.printStackTrace();
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
                        LogUtil.log("test", "isDownloading "+downloading);
                        if(!downloading){
                            progressMenuItem.setVisible(false);
                        }else{
                            updateDownloadingProgress();
                        }
                    }   //store not null check
                }

            }                               //progress menu item not null check
        } catch(Exception ex) {
            ex.printStackTrace();
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
                        storage.getAverageDownloadProgress(averageProgressCallback);
                    }else{
                        progressMenuItem.setVisible(false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //Broadcast Receiver to notify all activities to finish if user logs out
    private BroadcastReceiver logoutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    protected void enableLogoutCallback() {
        // register for logout click listener
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.LOGOUT_CLICKED);
        registerReceiver(logoutReceiver, filter);
    }

    protected void disableLogoutCallback() {
        // un-register logoutReceiver
        unregisterReceiver(logoutReceiver);
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

    //Broadcast receiver to notify app for network change
    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            log("network state changed");
            if (NetworkUtil.isConnected(context)) {
                if ( !isOnline) {
                    // only notify if previous state was NOT same
                    isOnline = true;
                    handler.post(new Runnable() {
                        public void run() {
                            AppConstants.offline_flag = false;
                            onOnline();
                        }
                    });
                }
            } else {
                if (isOnline) {
                    isOnline = false;
                    handler.post(new Runnable() {
                        public void run() {
                            AppConstants.offline_flag = true;
                            onOffline();
                        }
                    });
                }
            }
        }

    };

    protected void enableNetworkStateChangeCallback() {
        // register for network state change receiver
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(networkStateReceiver, filter);
    }

    protected void disableNetworkStateChangedCallback() {
        // un-register network state receiver
        unregisterReceiver(networkStateReceiver);
    }

    /**
     * Sub-classes may override this method to handle connected state.
     */
    protected void onOnline() {
        AppConstants.offline_flag = false;
        log ("you are now online");
    }

    /**
     * Sub-classes may override this method to handle disconnected state.
     */
    protected void onOffline() {
        AppConstants.offline_flag = true;
        log ("you are now offline");
    }

    private void applyTransitionNext() {
        // apply slide transition animation
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
        LogUtil.log(getClass().getName(), "next transition animation applied");
    }

    private void applyTransitionPrev() {
        // apply slide transition animation
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
        LogUtil.log(getClass().getName(), "prev transition animation applied");
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
        db = DatabaseFactory.getInstance(this,
                DatabaseFactory.TYPE_DATABASE_NATIVE, username);

        segIO = SegmentFactory.getInstance(this,
                new SegmentTracker(this));
    }

    private DataCallback<Integer> averageProgressCallback = new DataCallback<Integer>() {
        @Override
        public void onResult(Integer result) {
            int progressPercent = result;
            LogUtil.log("test", "Progress Percentage"+progressPercent);
            if(progressPercent >= 0 && progressPercent <= 100){
                totalProgress.setProgressPercent(progressPercent);
            }
        }
        @Override
        public void onFail(Exception ex) {
            ex.printStackTrace();
        }
    };


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

    protected void showWebDialog(String fileName, boolean showTitle, String dialogTitle){
        WebViewDialogFragment eulaFragment = new WebViewDialogFragment();
        eulaFragment.setDialogContents(fileName, showTitle, dialogTitle);
        eulaFragment.show(getSupportFragmentManager(), "dialog");
        eulaFragment.setStyle(DialogFragment.STYLE_NORMAL,
                android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        eulaFragment.setCancelable(false);
    }
}
