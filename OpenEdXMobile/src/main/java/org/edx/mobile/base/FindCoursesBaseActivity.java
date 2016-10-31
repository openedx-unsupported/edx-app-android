package org.edx.mobile.base;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.course.CourseService;
import org.edx.mobile.http.callback.CallTrigger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.view.common.TaskProgressCallback;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;
import org.edx.mobile.view.dialog.EnrollmentFailureDialogFragment;
import org.edx.mobile.view.dialog.IDialogCallback;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;

public abstract class FindCoursesBaseActivity extends BaseFragmentActivity implements
        URLInterceptorWebViewClient.IActionListener,
        URLInterceptorWebViewClient.IPageStatusListener {

    private static final int LOG_IN_REQUEST_CODE = 42;
    private static final String INSTANCE_COURSE_ID = "enrollCourseId";
    private static final String INSTANCE_EMAIL_OPT_IN = "enrollEmailOptIn";

    private static final String ACTION_ENROLLED = "ACTION_ENROLLED_TO_COURSE";

    private View offlineBar;
    private WebView webview;
    private boolean isWebViewLoaded;
    private ProgressBar progressWheel;
    private boolean isTaskInProgress = false;
    private String lastClickEnrollCourseId;
    private boolean lastClickEnrollEmailOptIn;

    @Inject
    private CourseService courseService;

    @Inject
    private CourseAPI courseApi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowHomeEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setIcon(android.R.color.transparent);
        }


        webview = (WebView) findViewById(R.id.webview);
        offlineBar = findViewById(R.id.offline_bar);
        progressWheel = (ProgressBar) findViewById(R.id.loading_indicator);

        webview.getSettings().setDomStorageEnabled(true);

        setupWebView();
        enableEnrollCallback();

        if (null != savedInstanceState) {
            lastClickEnrollCourseId = savedInstanceState.getString(INSTANCE_COURSE_ID);
            lastClickEnrollEmailOptIn = savedInstanceState.getBoolean(INSTANCE_EMAIL_OPT_IN);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        webview.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        webview.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disableEnrollCallback();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        webview.destroy();
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
    private void showOfflineMessage() {
        if (webview != null) {
            webview.setVisibility(View.GONE);
        }
        TextView offlineModeTv = (TextView) findViewById(R.id.offline_mode_message);
        if (offlineModeTv != null) {
            offlineModeTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This function hides the offline mode message
     */
    private void hideOfflineMessage() {
        if (webview != null) {
            webview.setVisibility(View.VISIBLE);
        }
        TextView offlineModeTv = (TextView) findViewById(R.id.offline_mode_message);
        if (offlineModeTv != null) {
            offlineModeTv.setVisibility(View.GONE);
        }
    }

    /**
     * This function shows the loading progress wheel
     * Show progress wheel while loading the web page
     */
    private void showLoadingProgress() {
        if (progressWheel != null) {
            progressWheel.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This function hides the loading progress wheel
     * Hide progress wheel after the web page completes loading
     */
    private void hideLoadingProgress() {
        if (progressWheel != null) {
            progressWheel.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClickCourseInfo(String pathId) {
        //If Path id is not null or empty then call CourseInfoActivity
        if (!TextUtils.isEmpty(pathId)) {
            logger.debug("PathId" + pathId);
            environment.getRouter().showCourseInfo(this, pathId);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOG_IN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            onClickEnroll(lastClickEnrollCourseId, lastClickEnrollEmailOptIn);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INSTANCE_COURSE_ID, lastClickEnrollCourseId);
        outState.putBoolean(INSTANCE_EMAIL_OPT_IN, lastClickEnrollEmailOptIn);
    }

    @Override
    public void onClickEnroll(final String courseId, final boolean emailOptIn) {
        if (isTaskInProgress) {
            // avoid duplicate actions
            logger.debug("already enroll task is in progress, so skipping Enroll action");
            return;
        }

        if (environment.getLoginPrefs().getUsername() == null) {
            lastClickEnrollCourseId = courseId;
            lastClickEnrollEmailOptIn = emailOptIn;
            startActivityForResult(environment.getRouter().getRegisterIntent(), LOG_IN_REQUEST_CODE);
            return;
        }

        environment.getAnalyticsRegistry().trackEnrollClicked(courseId, emailOptIn);

        isTaskInProgress = true;

        logger.debug("CourseId - " + courseId);
        logger.debug("Email option - " + emailOptIn);
        courseService.enrollInACourse(new CourseService.EnrollBody(courseId, emailOptIn))
                .enqueue(new CourseService.EnrollCallback(
                        FindCoursesBaseActivity.this,
                        CallTrigger.USER_ACTION,
                        new TaskProgressCallback.ProgressViewController(progressWheel)) {
                    @Override
                    protected void onResponse(@NonNull final ResponseBody responseBody) {
                        super.onResponse(responseBody);
                        logger.debug("Enrollment successful: " + courseId);
                        Toast.makeText(FindCoursesBaseActivity.this, getString(R.string.you_are_now_enrolled), Toast.LENGTH_SHORT).show();

                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                courseApi.getEnrolledCourses().enqueue(new CourseAPI.GetCourseByIdCallback(
                                        FindCoursesBaseActivity.this,
                                        courseId,
                                        CallTrigger.USER_ACTION,
                                        new TaskProgressCallback.ProgressViewController(progressWheel)) {
                                    @Override
                                    protected void onResponse(@NonNull final EnrolledCoursesResponse course) {
                                        environment.getRouter().showMyCourses(FindCoursesBaseActivity.this);
                                        environment.getRouter().showCourseDashboardTabs(FindCoursesBaseActivity.this, environment.getConfig(), course, false);
                                    }

                                    @Override
                                    protected void onFailure(@NonNull final Throwable error) {
                                        isTaskInProgress = false;
                                        Toast.makeText(FindCoursesBaseActivity.this, R.string.cannot_show_dashboard, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    protected void onFailure(@NonNull Throwable error) {
                        isTaskInProgress = false;
                        logger.debug("Error during enroll api call");
                        showEnrollErrorMessage(courseId, emailOptIn);
                    }
                });
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
     *
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
