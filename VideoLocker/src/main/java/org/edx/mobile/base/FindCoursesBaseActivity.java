package org.edx.mobile.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.event.FlyingMessageEvent;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.task.EnrollForCourseTask;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;
import org.edx.mobile.view.dialog.EnrollmentFailureDialogFragment;
import org.edx.mobile.view.dialog.IDialogCallback;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

public abstract class FindCoursesBaseActivity extends BaseFragmentActivity implements
        URLInterceptorWebViewClient.IActionListener,
        URLInterceptorWebViewClient.IPageStatusListener {

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
        progressWheel = (ProgressBar) findViewById(R.id.loading_indicator);

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

    protected boolean isWebViewLoaded() {
        return isWebViewLoaded;
    }

    private void setupWebView() {
        URLInterceptorWebViewClient client = new URLInterceptorWebViewClient(this, webview);

        // if all the links are to be treated as external
        client.setAllLinksAsExternal(isAllLinksExternal());

        client.setActionListener(this);
        client.setPageStatusListener(this);
    }

    @Override
    protected void onOnline() {
        if (!isWebViewLoaded) {
            super.onOnline();
            offlineBar.setVisibility(View.GONE);
            hideOfflineMessage();
        }
    }

    @Override
    protected void onOffline() {
        // If the WebView is not loaded, then show the offline mode message
        if (!isWebViewLoaded) {
            super.onOffline();
            offlineBar.setVisibility(View.VISIBLE);
            showOfflineMessage();
            hideLoadingProgress();
        }
    }

    /**
     * This function shows the offline mode message
     */
    private void showOfflineMessage(){
        if(webview!=null){
            webview.setVisibility(View.GONE);
        }
        TextView offlineModeTv = (TextView) findViewById(R.id.offline_mode_message);
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
        TextView offlineModeTv = (TextView) findViewById(R.id.offline_mode_message);
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
        //If Path id is not null or empty then call CourseInfoActivity
        if(!TextUtils.isEmpty(pathId)){
            logger.debug("PathId" +pathId);
            environment.getRouter().showCourseInfo(this, pathId);
        }
    }

    @Override
    public void onClickEnroll(final String courseId, final boolean emailOptIn) {
        if (isTaskInProgress) {
            // avoid duplicate actions
            logger.debug("already enroll task is in progress, so skipping Enroll action");
            return;
        }

        try {
            environment.getSegment().trackEnrollClicked(courseId, emailOptIn);
        }catch(Exception e){
            logger.error(e);
        }

        isTaskInProgress = true;

        logger.debug("CourseId - "+courseId);
        logger.debug("Email option - "+emailOptIn);
        EnrollForCourseTask enrollForCourseTask = new EnrollForCourseTask(FindCoursesBaseActivity.this,
            courseId, emailOptIn) {
            @Override
            public void onSuccess(Void result) {
                isTaskInProgress = false;
                logger.debug("Enrollment successful");
                //If the course is successfully enrolled, send a broadcast
                // to close the FindCoursesActivity
                Intent intent = new Intent();
                intent.putExtra("course_id", courseId);
                intent.setAction(ACTION_ENROLLED);
                sendBroadcast(intent);

                // show flying message about the success of Enroll

                EnrolledCoursesResponse course = environment.getServiceManager().getCourseById(courseId);
                String msg;
                if (course == null || course.getCourse() == null ) {
                    // this means, you were not already enrolled to this course
                    msg = String.format("%s", context.getString(R.string.you_are_now_enrolled));
                }else{
                    // this means, you were already enrolled to this course
                    msg = String.format("%s", context.getString(R.string.already_enrolled));
                }
                EventBus.getDefault().postSticky(new FlyingMessageEvent(msg));
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
        enrollForCourseTask.execute();
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

    /**
     * By default, all links will not be treated as external.
     * Depends on host, as long as the links have same host, they are treated as non-external links.
     * @return
     */
    protected boolean isAllLinksExternal() {
        return false;
    }

    @Override
    public void onPagePartiallyLoaded() {
        hideLoadingProgress();
    }
}
