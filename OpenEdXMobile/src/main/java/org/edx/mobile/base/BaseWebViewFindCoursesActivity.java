package org.edx.mobile.base;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.course.CourseService;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.http.provider.OkHttpClientProvider;
import org.edx.mobile.interfaces.WebViewStatusListener;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.WebViewUtil;
import org.edx.mobile.view.common.TaskProgressCallback;
import org.edx.mobile.view.custom.EdxWebView;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;
import org.edx.mobile.view.dialog.EnrollmentFailureDialogFragment;
import org.edx.mobile.view.dialog.IDialogCallback;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

public abstract class BaseWebViewFindCoursesActivity extends BaseFragmentActivity
        implements URLInterceptorWebViewClient.IActionListener, WebViewStatusListener {
    private static final int LOG_IN_REQUEST_CODE = 42;
    private static final String INSTANCE_COURSE_ID = "enrollCourseId";
    private static final String INSTANCE_EMAIL_OPT_IN = "enrollEmailOptIn";

    private static final String ACTION_ENROLLED = "ACTION_ENROLLED_TO_COURSE";

    private EdxWebView webView;
    private ProgressBar progressWheel;
    private boolean isTaskInProgress = false;
    private String lastClickEnrollCourseId;
    private boolean lastClickEnrollEmailOptIn;

    private FullScreenErrorNotification errorNotification;

    @Inject
    private CourseService courseService;

    @Inject
    private CourseAPI courseApi;

    @Inject
    private OkHttpClientProvider okHttpClientProvider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setToolbarAsActionBar();
        webView = (EdxWebView) findViewById(R.id.webview);
        progressWheel = (ProgressBar) findViewById(R.id.loading_indicator);
        errorNotification = new FullScreenErrorNotification(webView);

        webView.getSettings().setDomStorageEnabled(true);

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
        webView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disableEnrollCallback();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        webView.destroy();
    }

    private void setupWebView() {
        URLInterceptorWebViewClient client = new URLInterceptorWebViewClient(this, webView);

        // if all the links are to be treated as external
        client.setAllLinksAsExternal(isAllLinksExternal());

        client.setActionListener(this);
        client.setPageStatusListener(pageStatusListener);
    }

    /**
     * Loads the given URL into {@link #webView}.
     *
     * @param url The URL to load.
     */
    protected void loadUrl(@NonNull String url) {
        WebViewUtil.loadUrlBasedOnOsVersion(this, webView, url, this, errorNotification, okHttpClientProvider);
    }

    @Override
    public void showLoadingProgress() {
        if (progressWheel != null) {
            progressWheel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideLoadingProgress() {
        if (progressWheel != null) {
            progressWheel.setVisibility(View.GONE);
        }
    }

    @Override
    public void clearWebView() {
        WebViewUtil.clearWebviewHtml(webView);
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
                        BaseWebViewFindCoursesActivity.this,
                        new TaskProgressCallback.ProgressViewController(progressWheel)) {
                    @Override
                    protected void onResponse(@NonNull final ResponseBody responseBody) {
                        super.onResponse(responseBody);
                        logger.debug("Enrollment successful: " + courseId);
                        Toast.makeText(BaseWebViewFindCoursesActivity.this, getString(R.string.you_are_now_enrolled), Toast.LENGTH_SHORT).show();

                        environment.getAnalyticsRegistry().trackEnrolmentSuccess(courseId, emailOptIn);

                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                courseApi.getEnrolledCourses().enqueue(new CourseAPI.GetCourseByIdCallback(
                                        BaseWebViewFindCoursesActivity.this,
                                        courseId,
                                        new TaskProgressCallback.ProgressViewController(progressWheel)) {
                                    @Override
                                    protected void onResponse(@NonNull final EnrolledCoursesResponse course) {
                                        environment.getRouter().showMyCourses(BaseWebViewFindCoursesActivity.this);
                                        environment.getRouter().showCourseDashboardTabs(BaseWebViewFindCoursesActivity.this, environment.getConfig(), course, false);
                                    }

                                    @Override
                                    protected void onFailure(@NonNull final Throwable error) {
                                        isTaskInProgress = false;
                                        Toast.makeText(BaseWebViewFindCoursesActivity.this, R.string.cannot_show_dashboard, Toast.LENGTH_SHORT).show();
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

    /**
     * By default, all links will not be treated as external.
     * Depends on host, as long as the links have same host, they are treated as non-external links.
     *
     * @return
     */
    protected boolean isAllLinksExternal() {
        return false;
    }

    /*
     * In order to avoid reflection issues of public functions in event bus especially those that
     * aren't available on a certain api level, this listener has been refactored to a class
     * variable which is better explained in following references:
     * https://github.com/greenrobot/EventBus/issues/149
     * http://greenrobot.org/eventbus/documentation/faq/
     */
    private URLInterceptorWebViewClient.IPageStatusListener pageStatusListener = new URLInterceptorWebViewClient.IPageStatusListener() {
        @Override
        public void onPageStarted() {
            showLoadingProgress();
        }

        @Override
        public void onPageFinished() {
            hideLoadingProgress();
        }

        @Override
        public void onPageLoadError(WebView view, int errorCode, String description,
                                    String failingUrl) {
            errorNotification.showError(BaseWebViewFindCoursesActivity.this,
                    new HttpStatusException(Response.error(HttpStatus.SERVICE_UNAVAILABLE,
                            ResponseBody.create(MediaType.parse("text/plain"), description))));
            clearWebView();
        }

        @Override
        @TargetApi(Build.VERSION_CODES.M)
        public void onPageLoadError(WebView view, WebResourceRequest request,
                                    WebResourceResponse errorResponse,
                                    boolean isMainRequestFailure) {
            if (isMainRequestFailure) {
                errorNotification.showError(BaseWebViewFindCoursesActivity.this,
                        new HttpStatusException(Response.error(errorResponse.getStatusCode(),
                                ResponseBody.create(MediaType.parse(errorResponse.getMimeType()),
                                        errorResponse.getReasonPhrase()))));
                clearWebView();
            }
        }

        @Override
        public void onPagePartiallyLoaded() {
        }
    };
}
