package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.model.ICourse;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.third_party.iconify.IconDrawable;
import org.edx.mobile.third_party.iconify.Iconify;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.common.TaskProcessCallback;
import org.edx.mobile.view.custom.ETextView;

/**
 *  A base class to handle some common task
 *  NOTE- in the layout file,  these should be defined
 *  1. offlineBar
 *  2. progress_spinner
 *  3. offline_mode_message
 */
public abstract  class CourseBaseActivity  extends BaseFragmentActivity implements TaskProcessCallback{

    private View offlineBar;
    private ProgressBar progressWheel;

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
        progressWheel = (ProgressBar) findViewById(R.id.progress_spinner);
        if (!(NetworkUtil.isConnected(this))) {
            AppConstants.offline_flag = true;
            invalidateOptionsMenu();
            showOfflineMessage();
            if(offlineBar!=null){
                offlineBar.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
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
        PrefManager.UserPrefManager userPrefManager = new PrefManager.UserPrefManager(this);

        if (userPrefManager.isUserPrefVideoModel()) {
            menu.findItem(R.id.action_change_mode).setIcon(
                new IconDrawable(this, Iconify.IconValue.fa_film)
                    .actionBarSize());
        } else {
            menu.findItem(R.id.action_change_mode).setIcon(
                new IconDrawable(this, Iconify.IconValue.fa_list)
                    .actionBarSize());
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_change_mode:
                changeMode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void changeMode(){
                 //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(this, this.progressWheel);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                    .inflate(R.menu.change_mode, popup.getMenu());
                MenuItem menuItem = popup.getMenu().findItem(R.id.change_mode_video_only);


                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(
                            CourseBaseActivity.this,
                            "You Clicked : " + item.getTitle(),
                            Toast.LENGTH_SHORT
                        ).show();
                        return true;
                    }
                });

                popup.show(); //showing popup menu

    }
    /**
     * This function shows the offline mode message
     */
    private void showOfflineMessage(){

        ETextView offlineModeTv = (ETextView) findViewById(R.id.offline_mode_message);
        if(offlineModeTv!=null){
            offlineModeTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This function hides the offline mode message
     */
    private void hideOfflineMessage() {

        ETextView offlineModeTv = (ETextView) findViewById(R.id.offline_mode_message);
        if(offlineModeTv!=null) {
            offlineModeTv.setVisibility(View.GONE);
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
