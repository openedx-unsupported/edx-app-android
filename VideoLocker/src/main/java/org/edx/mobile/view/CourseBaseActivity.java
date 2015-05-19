package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.event.DownloadEvent;
import org.edx.mobile.model.ICourse;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.third_party.iconify.IconDrawable;
import org.edx.mobile.third_party.iconify.Iconify;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.common.TaskProcessCallback;

import de.greenrobot.event.EventBus;

/**
 *  A base class to handle some common task
 *  NOTE- in the layout file,  these should be defined
 *  1. offlineBar
 *  2. progress_spinner
 *  3. offline_mode_message
 */
public abstract  class CourseBaseActivity  extends BaseFragmentActivity implements TaskProcessCallback{

    private View offlineBar;
    private View lastAccessBar;
    private View downloadProgressBar;
    protected TextView downloadIndicator;

    protected ProgressBar progressWheel;

    protected EnrolledCoursesResponse courseData;
    protected ICourse course;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        Bundle bundle = arg0;
        if ( bundle == null ) {
            if ( getIntent() != null )
                bundle = getIntent().getBundleExtra(Router.EXTRA_BUNDLE);
        }
        restore(bundle);

        initialize(arg0);
        blockDrawerFromOpening();
    }

    protected int getContentViewResourceId(){
        return R.layout.activity_course_base;
    }

    protected void initialize(Bundle arg){
        setContentView(getContentViewResourceId());

        setApplyPrevTransitionOnRestart(true);
        offlineBar = findViewById(R.id.offline_bar);
        lastAccessBar = findViewById(R.id.last_access_bar);
        downloadProgressBar = findViewById(R.id.download_in_progress_bar);
        downloadIndicator = (TextView)findViewById(R.id.video_download_indicator);
        Iconify.setIcon(downloadIndicator, Iconify.IconValue.fa_spinner);
        findViewById(R.id.download_in_progress_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.getInstance().showDownloads(CourseBaseActivity.this);
            }
        });

        progressWheel = (ProgressBar) findViewById(R.id.progress_spinner);
        if (!(NetworkUtil.isConnected(this))) {
            AppConstants.offline_flag = true;
            invalidateOptionsMenu();
            showOfflineMessage();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        if ( !EventBus.getDefault().isRegistered(this) )
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if ( courseData != null)
            outState.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
        if ( course != null )
            outState.putSerializable(Router.EXTRA_COURSE, course);
        super.onSaveInstanceState(outState);
    }

    protected void restore(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            courseData = (EnrolledCoursesResponse) savedInstanceState.getSerializable(Router.EXTRA_COURSE_DATA);
            course = (ICourse) savedInstanceState.getSerializable(Router.EXTRA_COURSE);
        }
    }

    /**
     * callback for EventBus
     * https://github.com/greenrobot/EventBus
     */
    public void onEvent(DownloadEvent event) {
        setVisibilityForDownloadProgressView(true);
    }

    @Override
    protected void onOnline() {
        offlineBar.setVisibility(View.GONE);
        hideOfflineMessage();
    }

    @Override
    protected void onOffline() {
        offlineBar.setVisibility(View.VISIBLE);

        hideLoadingProgress();
        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected boolean createOptionMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.course_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if( menu.findItem(R.id.action_share_on_web) != null)
            menu.findItem(R.id.action_share_on_web).setIcon(
                new IconDrawable(this, Iconify.IconValue.fa_share_square_o)
                    .actionBarSize());
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_share_on_web:
                BrowserUtil.open(this, getUrlForWebView());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected  String getUrlForWebView(){
        return "";
    }


    /**
     * This function shows the offline mode message
     */
    private void showOfflineMessage(){
        if(offlineBar!=null){
            offlineBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This function hides the offline mode message
     */
    private void hideOfflineMessage() {
        if(offlineBar!=null){
            offlineBar.setVisibility(View.GONE);
        }
    }

    /**
     * This function shows the loading progress wheel
     * Show progress wheel while loading the web page
     */
    private void showLoadingProgress(){
        if(progressWheel!=null){
            progressWheel.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This function hides the loading progress wheel
     * Hide progress wheel after the web page completes loading
     */
    private void hideLoadingProgress(){
        if(progressWheel!=null){
            progressWheel.setVisibility(View.GONE);
        }
    }


    protected void setVisibilityForDownloadProgressView(boolean show){
        downloadProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    protected void hideLastAccessedView(View v) {
        try{
            lastAccessBar.setVisibility(View.GONE);
        }catch(Exception e){
            logger.error(e);
        }
    }

    protected void showLastAccessedView(View v, String title, View.OnClickListener listener) {
        try{
            lastAccessBar.setVisibility(View.VISIBLE);
            //
            View lastAccessTextView = v == null ? findViewById(R.id.last_access_text) :
                v.findViewById(R.id.last_access_text);
            ((TextView)lastAccessTextView).setText(title);
            View detailButton = v == null ? findViewById(R.id.last_access_button) :
                v.findViewById(R.id.last_access_button);
            detailButton.setOnClickListener(listener);

        }catch(Exception e){
            logger.error(e);
        }
    }


    /**
     * Call this function if you do not want to allow
     * opening/showing the drawer(Navigation Fragment) on swiping left to right
     */
    protected void blockDrawerFromOpening(){
        DrawerLayout drawerLayout = (DrawerLayout)
            findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    /**
     * implements TaskProcessCallback
     */
    public void startProcess(){
        showLoadingProgress();
    }
    /**
     * implements TaskProcessCallback
     */
    public void finishProcess(){
        hideLoadingProgress();
    }
}

