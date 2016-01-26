package org.edx.mobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.player.PlayerActivity;
import org.edx.mobile.player.VideoListFragment.VideoListCallback;
import org.edx.mobile.util.AppConstants;

public class MyVideosTabActivity extends PlayerActivity implements VideoListCallback {

    private View offlineBar;
    private TabHost mTabHost;
    private MyRecentVideosFragment recentVideosFragment;
    private VideoTabCheckBoxListener videoCheckListener;
    private MyAllVideosFragment allVideosFrag;
    /* Save current tabs identifier in this.. */
    private String mCurrentTab;
    private CheckBox myVideocheckBox;
    private final Logger logger = new Logger(getClass().getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myvideos_tab);

        // configure slider layout. This should be called only once and
        // hence is shifted to onCreate() function
        configureDrawer();

        offlineBar = findViewById(R.id.offline_bar);

        environment.getSegment().trackScreenView(ISegment.Screens.MY_VIDEOS);

        // now init the tabs
        initializeTab();

        // let's restore player after tabs are ready
        onCreatePlayer(savedInstanceState);

        // now restore the player, if it was saved previously
        restorePlayerFragment();
    }

    @Override
    protected void onStart() {
        super.onStart();

        ActionBar bar = getSupportActionBar();
        if (bar != null && !isLandscape()) {
            bar.show();
            setTitle(getString(R.string.label_my_videos));
        }

        if (recentVideosFragment != null) {
            recentVideosFragment.setCallback(this);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // Wait until MyRecentVideosFragment.onResume() has been called so that its adapter has been filled
        // Wait until PlayerFragment.onActivityCreated has been called, so PlayerFragment.player != null
        if (null != playerFragment && null != recentVideosFragment) {
            playerFragment.setNextPreviousListeners(recentVideosFragment.getNextListener(),
                    recentVideosFragment.getPreviousListener());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideCheckBox(); 
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        logger.debug("restarting ");
        try{
            if (mCurrentTab
                    .equalsIgnoreCase(getString(R.string.tab_my_recent_videos))) {
                recentVideosFragment.showDeletePanel(recentVideosFragment.getView());
            }
            invalidateOptionsMenu();
        }catch(Exception ex){
            logger.error(ex);
        }

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            if(recentVideosFragment!=null){
                recentVideosFragment.onSaveInstanceState(outState);
                if (recentVideosFragment.isAdded())
                     getSupportFragmentManager().putFragment(outState, "recentFragment", recentVideosFragment);
            }
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    @Override
    protected void restore(Bundle savedInstanceState) {
        super.restore(savedInstanceState);
        try {
            if (savedInstanceState != null) {
                try {
                    Fragment fragment = getSupportFragmentManager().getFragment(savedInstanceState,
                            "recentFragment");
                    if (fragment != null && fragment instanceof MyRecentVideosFragment) {
                        recentVideosFragment = (MyRecentVideosFragment) fragment;
                        recentVideosFragment.setContainerActivity(this);
                    }
                } catch(Exception ex) {
                    logger.error(ex);
                }
                try{
                    if(recentVideosFragment!=null){
                        recentVideosFragment.restore(savedInstanceState);
                    }
                }catch(Exception e){
                    logger.error(e);
                }
            }
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    private void initializeTab() {
        try{
            mTabHost = (TabHost) findViewById(android.R.id.tabhost);
            mTabHost.setOnTabChangedListener(listener);
            mTabHost.setup();

            /* Setup your tab icons and content views.. Nothing special in this.. */
            TabHost.TabSpec spec = mTabHost
                    .newTabSpec(getString(R.string.tab_my_all_videos));
            if (allVideosFrag == null) {
                allVideosFrag = new MyAllVideosFragment();
                allVideosFrag.setRetainInstance(true);
            }
            spec.setContent(new TabHost.TabContentFactory() {
                public View createTabContent(String tag) {
                    return findViewById(android.R.id.tabcontent);
                }
            });
            spec.setIndicator(getString(R.string.my_all_videos));
            mTabHost.addTab(spec);

            // NOT single instance
            if (recentVideosFragment == null) {
                recentVideosFragment = new MyRecentVideosFragment();
                // retain is VERY IMPORTANT
                recentVideosFragment.setRetainInstance(true);
            }
            recentVideosFragment.setContainerActivity(this);

            spec = mTabHost.newTabSpec(getString(R.string.tab_my_recent_videos));
            spec.setContent(new TabHost.TabContentFactory() {
                public View createTabContent(String tag) {
                    return findViewById(android.R.id.tabcontent);
                }
            });
            spec.setIndicator(getString(R.string.my_recent_videos));
            mTabHost.addTab(spec);

            mCurrentTab = getString(R.string.tab_my_all_videos);

            TabWidget widget = mTabHost.getTabWidget();
            for (int i = 0; i < widget.getChildCount(); i++) {
                View child = widget.getChildAt(i);
                final TextView tv = (TextView) child.findViewById(
                        android.R.id.title);
                tv.setTextColor(this.getResources().getColorStateList(
                        R.color.tab_selector));
                tv.setAllCaps(true);

                child.setBackgroundResource(R.drawable.tab_indicator);
            }
            mTabHost.setCurrentTab(0);
        }catch(Exception e){
            logger.error(e);
        }
    }

    public void setTabChangeEnabled(boolean enabled) {
        for (int i=0; i<mTabHost.getTabWidget().getTabCount(); i++) {
            View tab = mTabHost.getTabWidget().getChildTabViewAt(i);
            tab.setEnabled(enabled);
        }
        mTabHost.setOnTabChangedListener(enabled ? listener : null);
    }

    private void restorePlayerFragment() {
        try{
            if (playerFragment != null) {
                    // switch to "recent" tab
                    if(recentVideosFragment!=null){
                        setCurrentTab(1);
                        String tabId = getString(R.string.tab_my_recent_videos);
                        mCurrentTab = tabId;
                        pushFragments(tabId, recentVideosFragment);

                        // now also re-attach the recent fragment
                        FragmentManager fm = getSupportFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        ft.detach(recentVideosFragment);
                        logger.debug("removing recent fragment...");
                        ft.attach(recentVideosFragment);
                        logger.debug("adding recent fragment...");
                        ft.commit();
                    }
                }
        }catch(Exception ex){
            logger.error(ex);
        }
    }

    private void killPlayer() {
        try{
            if (playerFragment != null) {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.remove(playerFragment);
                ft.commit();

                playerFragment = null;
                logger.debug("killing player ...");
            }
        }catch(Exception ex){
            logger.error(ex);
        }
    }

    /* Comes here when user switch tab, or we do programmatically */
    TabHost.OnTabChangeListener listener = new TabHost.OnTabChangeListener() {

        @Override
        public void onTabChanged(String tabId) {
            if (tabId.equals(getString(R.string.tab_my_all_videos))) {
                try{
                    if(allVideosFrag==null){
                        allVideosFrag = new MyAllVideosFragment();
                        allVideosFrag.setRetainInstance(true);
                    }
                    pushFragments(tabId, allVideosFrag);
                    if(playerFragment!=null){
                        playerFragment.lockOrientation();
                    }
                    hideCheckBox();
                    killPlayer();
                }catch(Exception ex){
                    logger.error(ex);
                }
            } else if (tabId.equals(getString(R.string.tab_my_recent_videos))) {
                try{
                    if (recentVideosFragment == null) {
                        recentVideosFragment = new MyRecentVideosFragment();
                        // retain is VERY IMPORTANT
                        recentVideosFragment.setRetainInstance(true);
                        recentVideosFragment.setContainerActivity(MyVideosTabActivity.this);
                    }
                    if(playerFragment!=null){
                        playerFragment.unlockOrientation();
                    }else{
                        //Set the current playing videoId as null to the adapter only if tab switch was done
                        //not on orientation change during the video was being played
                        recentVideosFragment.setCurrentPlayingVideoIdAsNull();
                    }
                    pushFragments(tabId, recentVideosFragment);
                    if(recentVideosFragment!=null){
                        recentVideosFragment.setSelectionEmpty();
                    }
                }catch(Exception ex){
                    logger.error(ex);
                }
            }
        }
    };

    /*
     * Might be useful if we want to switch tab programmatically, from inside
     * any of the fragment.
     */
    public void setCurrentTab(int val) {
        mTabHost.setCurrentTab(val);
    }

    public void pushFragments(String tag, Fragment fragment){
        try {
            if(fragment==null){
                return;
            }

            // Set current tab.. 
            mCurrentTab = tag;
            //      currentFragment = fragment;

            FragmentManager   manager         =   getSupportFragmentManager();
            FragmentTransaction ft            =   manager.beginTransaction();
            ft.replace(android.R.id.tabcontent, fragment, tag);
            ft.commit();
            manager.executePendingTransactions();
            invalidateOptionsMenu();
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.video_list, menu);
        MenuItem checkBox_menuItem = menu.findItem(R.id.delete_checkbox);
        View checkBoxView = checkBox_menuItem.getActionView();
        myVideocheckBox = (CheckBox) checkBoxView.findViewById(R.id.select_checkbox);
        if (videoCheckListener == null) {
            videoCheckListener = new VideoTabCheckBoxListener();
        }
        if (AppConstants.myVideosDeleteMode && !(mCurrentTab
                .equalsIgnoreCase(getString(R.string.tab_my_all_videos)))) {
            checkBox_menuItem.setVisible(true);
            if (AppConstants.myVideosDeleteMode) {
                myVideocheckBox.setOnCheckedChangeListener(videoCheckListener);
            }
        } else {
            checkBox_menuItem.setVisible(false);
        }
        return true;
    }

    private class VideoTabCheckBoxListener implements OnCheckedChangeListener{
        private boolean lastIsChecked = false;

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            if (isChecked == lastIsChecked) {
                return;
            }
            lastIsChecked = isChecked;
            if (mCurrentTab.equalsIgnoreCase(getString(R.string.tab_my_recent_videos))) {
                if (isChecked) {
                    recentVideosFragment.setAllVideosSectionChecked();
                    myVideocheckBox.setButtonDrawable(R.drawable.ic_checkbox_active);
                } else {
                    recentVideosFragment.unsetAllVideosSectionChecked();
                    myVideocheckBox.setButtonDrawable(R.drawable.ic_checkbox_default);
                }
            }
        }
    }

    @Override
    protected void onOffline() {
        super.onOffline();
        if (mCurrentTab.equalsIgnoreCase(getString(R.string.tab_my_recent_videos))) {
            recentVideosFragment.onOffline();
        }
        if (playerFragment != null) {
            playerFragment.onOffline();
        }
        offlineBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onOnline() {
        super.onOnline();
        if (mCurrentTab.equalsIgnoreCase(getString(R.string.tab_my_recent_videos))) {
            recentVideosFragment.onOnline();
        }
        if (playerFragment != null) {
            playerFragment.onOnline();
        }
        offlineBar.setVisibility(View.GONE);
    }

    public void showCheckBox() {
        AppConstants.myVideosDeleteMode = true;
        invalidateOptionsMenu();
    }

    public void hideCheckBox() {
        AppConstants.myVideosDeleteMode = false;
        if(myVideocheckBox!=null){
            myVideocheckBox.setChecked(false);
            myVideocheckBox.setButtonDrawable(R.drawable.ic_checkbox_default);
            //myVideocheckBox.setBackgroundResource(R.drawable.ic_checkbox_default);
        }
        invalidateOptionsMenu();
    }

    public void setMyVideosCheckBoxSelected(){
        try{
            myVideocheckBox.setSelected(true);
            myVideocheckBox.setButtonDrawable(R.drawable.ic_checkbox_active);
            //myVideocheckBox.setBackgroundResource(R.drawable.ic_checkbox_active);
        }catch(Exception ex){
            logger.error(ex);
        }
    }

    public void unsetMyVideosCheckBoxSelected(){
        try{
            myVideocheckBox.setSelected(false);
            myVideocheckBox.setButtonDrawable(R.drawable.ic_checkbox_default);
            //myVideocheckBox.setBackgroundResource(R.drawable.ic_checkbox_default);
        }catch(Exception ex){
            logger.error(ex);
        }
    }

    @Override
    public void onError() {
    }

    @Override
    public void onPlaybackStarted() {
        try{
            if(recentVideosFragment!=null){
                recentVideosFragment.markPlaying();
                recentVideosFragment.notifyAdapter();
            }
        }catch(Exception ex){
            logger.error(ex);
        }
    }

    @Override
    public void onPlaybackComplete() {
        try{
            if(recentVideosFragment!=null){
                recentVideosFragment.onPlaybackComplete();
                recentVideosFragment.notifyAdapter();
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    public synchronized void saveCurrentPlaybackPosition(int currentPosition) {
        try{
            if(recentVideosFragment!=null){
                recentVideosFragment.saveCurrentPlaybackPosition(currentPosition);
                recentVideosFragment.notifyAdapter();
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        // super class has transition animated setup
    }

    @Override
    public void finish() {
        super.finish();
        // super class has transition animated setup
    }
}
