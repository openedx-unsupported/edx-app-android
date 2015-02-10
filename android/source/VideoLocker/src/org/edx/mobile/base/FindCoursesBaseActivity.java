package org.edx.mobile.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.edx.mobile.R;
import org.edx.mobile.task.EnrollForCourseTask;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.Router;
import org.edx.mobile.view.custom.ETextView;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

public class FindCoursesBaseActivity extends BaseFragmentActivity implements URLInterceptorWebViewClient.IActionListener {

    private View offlineBar;
    private WebView webview;
    private boolean isWebViewLoaded;
    private ProgressBar progressWheel;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        webview = (WebView) findViewById(R.id.webview);
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


        setupWebView();
        enableEnrollCallback();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(R.string.find_courses_title));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disableEnrollCallback();
    }

    private void setupWebView() {
        if(webview!=null){
            isWebViewLoaded = false;
            URLInterceptorWebViewClient client = new URLInterceptorWebViewClient(webview);
            client.setActionListener(this);
        }
    }

    @Override
    protected void onOnline() {
        offlineBar.setVisibility(View.GONE);
        if(isWebViewLoaded){
            hideOfflineMessage();
            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onOffline() {
        offlineBar.setVisibility(View.VISIBLE);
        //If webview is not loaded, then show the offline mode message
        if(isWebViewLoaded) {
            showOfflineMessage();
        }
        hideLoadingProgress();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //Hide the download progress from Action bar
        MenuItem menuItem = menu.findItem(R.id.progress_download);
        menuItem.setVisible(false);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Hide the download progress from Action bar.
        //This has to be called in onCreateOptions as well
        MenuItem menuItem = menu.findItem(R.id.progress_download);
        menuItem.setVisible(false);
        return true;
    }

    /**
     * This function shows the offline mode message
     */
    private void showOfflineMessage(){

        if(webview!=null){
            webview.setVisibility(View.GONE);
        }
        ETextView offlineModeTv = (ETextView) findViewById(R.id.offline_mode_message);
        if(offlineModeTv!=null){
            offlineModeTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This function hides the offline mode message
     */
    private void hideOfflineMessage(){
        if(webview!=null){
            webview.setVisibility(View.VISIBLE);
        }
        ETextView offlineModeTv = (ETextView) findViewById(R.id.offline_mode_message);
        if(offlineModeTv!=null){
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

    @Override
    public void onClickCourseInfo(String pathId) {
        logger.debug("PathId" +pathId);
        Router.getInstance().showCourseInfo(this, pathId);
    }

    @Override
    public void onClickEnroll(String courseId, boolean emailOptIn) {
        logger.debug("CourseId - "+courseId);
        logger.debug("Email option - "+emailOptIn);
        EnrollForCourseTask enrollForCourseTask = new EnrollForCourseTask(FindCoursesBaseActivity.this) {
            @Override
            public void onFinish(Boolean result) {
                if(result!=null && result){
                    logger.debug("Enrollment successful");
                    //If the course is successfully enrolled, send a broadcast
                    // to close the FindCoursesActivity
                    Intent intent = new Intent();
                    intent.setAction(AppConstants.ENROLL_CLICKED);
                    sendBroadcast(intent);
                }
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
                logger.debug("Error during enroll api call");
            }
        };
        enrollForCourseTask.setProgressDialog(progressWheel);
        enrollForCourseTask.execute(courseId,emailOptIn);
    }

    @Override
    public void onPageStarted() {
        showLoadingProgress();
    }

    @Override
    public void onPageFinished() {
        hideLoadingProgress();
    }

    //Broadcast Receiver to notify all activities to finish if user logs out
    private BroadcastReceiver courseEnrollReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    protected void enableEnrollCallback() {
        // register for enroll click listener
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.ENROLL_CLICKED);
        registerReceiver(courseEnrollReceiver, filter);
    }

    protected void disableEnrollCallback() {
        // un-register enrollReceiver
        unregisterReceiver(courseEnrollReceiver);
    }
}
