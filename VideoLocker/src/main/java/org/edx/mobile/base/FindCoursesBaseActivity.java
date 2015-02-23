package org.edx.mobile.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import org.edx.mobile.R;
import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.task.EnrollForCourseTask;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.Router;
import org.edx.mobile.view.custom.ETextView;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;
import org.edx.mobile.view.dialog.EnrollmentFailureDialogFragment;
import org.edx.mobile.view.dialog.IDialogCallback;

import java.util.HashMap;
import java.util.Map;

public class FindCoursesBaseActivity extends BaseFragmentActivity
        implements URLInterceptorWebViewClient.IActionListener, URLInterceptorWebViewClient.IPageStatusListener {

    private static final String ACTION_ENROLLED = "ACTION_ENROLLED_TO_COURSE";

    private View offlineBar;
    private WebView webview;
    private boolean isWebViewLoaded;
    private ProgressBar progressWheel;
    private boolean isTaskInProgress = false;

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
            URLInterceptorWebViewClient client = new URLInterceptorWebViewClient(webview) {

                @Override
                public void onOpenExternalURL(String url) {
                    // open URL in external browser
                    BrowserUtil.open(FindCoursesBaseActivity.this, url);
                }
            };
            client.setActionListener(this);
            client.setPageStatusListener(this);
        }
    }

    @Override
    protected void onOnline() {
        offlineBar.setVisibility(View.GONE);
        if(isWebViewLoaded){
            hideOfflineMessage();
            invalidateOptionsMenu();
        }else{
            setupWebView();
            hideOfflineMessage();
        }
    }

    @Override
    protected void onOffline() {
        offlineBar.setVisibility(View.VISIBLE);
        //If webview is not loaded, then show the offline mode message
        if(!isWebViewLoaded) {
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
    private void hideOfflineMessage() {
        if(webview!=null) {
            webview.setVisibility(View.VISIBLE);
        }
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

    @Override
    public void onClickCourseInfo(String pathId) {
        logger.debug("PathId" +pathId);
        Router.getInstance().showCourseInfo(this, pathId);
    }

    @Override
    public void onClickEnroll(final String courseId, final boolean emailOptIn) {
        if (isTaskInProgress) {
            // avoid duplicate actions
            logger.debug("already enroll task is in progress, so skipping Enroll action");
            return;
        }

        isTaskInProgress = true;

        logger.debug("CourseId - "+courseId);
        logger.debug("Email option - "+emailOptIn);
        EnrollForCourseTask enrollForCourseTask = new EnrollForCourseTask(FindCoursesBaseActivity.this) {
            @Override
            public void onFinish(Boolean result) {
                isTaskInProgress = false;

                if(result!=null && result) {
                    logger.debug("Enrollment successful");
                    //If the course is successfully enrolled, send a broadcast
                    // to close the FindCoursesActivity
                    Intent intent = new Intent();
                    intent.putExtra("course_id", courseId);
                    intent.setAction(ACTION_ENROLLED);
                    sendBroadcast(intent);

                    // show flying message about the success of Enroll
                    Api api = new Api(context);
                    CourseEntry course = api.getCourseById(courseId);
                    if (course == null) {
                        // this means, you were not already enrolled to this course
                        String msg = String.format("%s", context.getString(R.string.you_are_now_enrolled));
                        sendBroadcastFlyingInfoMessage(msg);
                    }
                }else{
                    showEnrollErrorMessage(courseId, emailOptIn);
                }
            }

            @Override
            public void onException(Exception ex) {
                isTaskInProgress = false;
                logger.error(ex);
                logger.debug("Error during enroll api call");
                showEnrollErrorMessage(courseId,emailOptIn);
            }
        };
        enrollForCourseTask.setProgressDialog(progressWheel);
        enrollForCourseTask.execute(courseId,emailOptIn);
    }

    @Override
    public void onPageStarted() {
        showLoadingProgress();
        isWebViewLoaded = false;
    }

    @Override
    public void onPageFinished() {
        hideLoadingProgress();
        isWebViewLoaded = true;
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
        filter.addAction(ACTION_ENROLLED);
        registerReceiver(courseEnrollReceiver, filter);
    }

    protected void disableEnrollCallback() {
        // un-register enrollReceiver
        unregisterReceiver(courseEnrollReceiver);
    }

    private void showEnrollErrorMessage(final String courseId, final boolean emailOptIn) {
        if (isActivityStarted()) {
            Map<String, String> dialogMap = new HashMap<String, String>();
            dialogMap.put("message_1", getString(R.string.enrollment_failure));

            dialogMap.put("yes_button", getString(R.string.try_again));
            dialogMap.put("no_button", getString(R.string.label_cancel));
            EnrollmentFailureDialogFragment failureDialogFragment = EnrollmentFailureDialogFragment
                    .newInstance(dialogMap, new IDialogCallback() {
                        @Override
                        public void onPositiveClicked() {
                            onClickEnroll(courseId, emailOptIn);
                        }

                        @Override
                        public void onNegativeClicked() {
                        }
                    });
            failureDialogFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            failureDialogFragment.show(getSupportFragmentManager(), "dialog");
            failureDialogFragment.setCancelable(false);
        }
    }

    @Override
    public void onPageLoadError() {
        isWebViewLoaded = false;
        showOfflineMessage();
    }

    @Override
    public void onPagePartiallyLoaded() {
        hideLoadingProgress();
    }
}
