package org.edx.mobile.util.links;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.course.CourseService;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.common.TaskProgressCallback;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;
import org.edx.mobile.view.dialog.EnrollmentFailureDialogFragment;
import org.edx.mobile.view.dialog.IDialogCallback;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import roboguice.RoboGuice;

/**
 * A ready to use implementation of {@link org.edx.mobile.view.custom.URLInterceptorWebViewClient.ActionListener}
 * for all the classes that need to handle a WebView's recognized links as defined in
 * {@link WebViewLink} class.
 */
public class DefaultActionListener implements URLInterceptorWebViewClient.ActionListener {
    private final Logger logger = new Logger(URLInterceptorWebViewClient.class);

    /**
     * Provides callbacks to know about the status of enrollment in a course.
     */
    public interface EnrollCallback {
        void onResponse(@NonNull final EnrolledCoursesResponse course);

        void onFailure(@NonNull Throwable error);

        void onUserNotLoggedIn(@NonNull String courseId, boolean emailOptIn);
    }

    @Inject
    private IEdxEnvironment environment;

    @Inject
    private CourseService courseService;

    @Inject
    private CourseAPI courseApi;

    private FragmentActivity activity;
    private View progressWheel;
    private EnrollCallback enrollCallback;
    private boolean isTaskInProgress = false;

    public DefaultActionListener(@NonNull FragmentActivity activity, @NonNull View progressWheel,
                                 @NonNull EnrollCallback enrollCallback) {
        this.activity = activity;
        this.progressWheel = progressWheel;
        this.enrollCallback = enrollCallback;
        RoboGuice.injectMembers(activity, this);
    }

    @Override
    public void onLinkRecognized(@NonNull WebViewLink helper) {
        switch (helper.authority) {
            case ENROLLED_PROGRAM_INFO: {
                final CharSequence url = ResourceUtil.getFormattedString(
                        environment.getConfig().getProgramConfig().getDetailUrlTemplate(),
                        WebViewLink.Param.PATH_ID,
                        helper.params.get(WebViewLink.Param.PATH_ID));
                environment.getRouter().showAuthenticatedWebviewActivity(activity, url.toString(),
                        activity.getString(R.string.label_my_programs));
                break;
            }
            case ENROLLED_COURSE_INFO: {
                final String courseId = helper.params.get(WebViewLink.Param.COURSE_ID);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        courseApi.getEnrolledCourses().enqueue(new CourseAPI.GetCourseByIdCallback(
                                activity, courseId,
                                new TaskProgressCallback.ProgressViewController(progressWheel)) {
                            @Override
                            protected void onResponse(@NonNull final EnrolledCoursesResponse course) {
                                environment.getRouter().showCourseDashboardTabs(activity, course, false);
                            }

                            @Override
                            protected void onFailure(@NonNull final Throwable error) {
                                Toast.makeText(activity, R.string.cannot_show_dashboard, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                break;
            }
            case COURSE_INFO: {
                final String pathId = helper.params.get(WebViewLink.Param.PATH_ID);
                if (!TextUtils.isEmpty(pathId)) {
                    logger.debug("PathId" + pathId);
                    environment.getRouter().showCourseInfo(activity, pathId);
                }
                break;
            }
            case ENROLL: {
                final String courseId = helper.params.get(WebViewLink.Param.COURSE_ID);
                final String emailOptIn = helper.params.get(WebViewLink.Param.EMAIL_OPT);
                onClickEnroll(courseId, Boolean.getBoolean(emailOptIn));
                break;
            }
        }
    }

    public void onClickEnroll(@NonNull final String courseId, final boolean emailOptIn) {
        if (isTaskInProgress) {
            // avoid duplicate actions
            logger.debug("already enroll task is in progress, so skipping Enroll action");
            return;
        }

        if (environment.getLoginPrefs().getUsername() == null) {
            enrollCallback.onUserNotLoggedIn(courseId, emailOptIn);
            return;
        }

        isTaskInProgress = true;
        environment.getAnalyticsRegistry().trackEnrollClicked(courseId, emailOptIn);

        logger.debug("CourseId - " + courseId);
        logger.debug("Email option - " + emailOptIn);
        courseService.enrollInACourse(new CourseService.EnrollBody(courseId, emailOptIn))
                .enqueue(new CourseService.EnrollCallback(
                        activity,
                        new TaskProgressCallback.ProgressViewController(progressWheel)) {
                    @Override
                    protected void onResponse(@NonNull final ResponseBody responseBody) {
                        super.onResponse(responseBody);
                        logger.debug("Enrollment successful: " + courseId);
                        Toast.makeText(activity, activity.getString(R.string.you_are_now_enrolled), Toast.LENGTH_SHORT).show();

                        environment.getAnalyticsRegistry().trackEnrolmentSuccess(courseId, emailOptIn);

                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                courseApi.getEnrolledCourses().enqueue(new CourseAPI.GetCourseByIdCallback(
                                        activity, courseId,
                                        new TaskProgressCallback.ProgressViewController(progressWheel)) {
                                    @Override
                                    protected void onResponse(@NonNull final EnrolledCoursesResponse course) {
                                        enrollCallback.onResponse(course);
                                        environment.getRouter().showMainDashboard(activity);
                                        environment.getRouter().showCourseDashboardTabs(activity, course, false);
                                    }

                                    @Override
                                    protected void onFailure(@NonNull final Throwable error) {
                                        logger.warn("Error during enroll api call\n" + error);
                                        isTaskInProgress = false;
                                        enrollCallback.onFailure(error);
                                        Toast.makeText(activity, R.string.cannot_show_dashboard, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    protected void onFailure(@NonNull Throwable error) {
                        logger.warn("Error during enroll api call\n" + error);
                        isTaskInProgress = false;
                        enrollCallback.onFailure(error);

                        if (activity instanceof BaseFragmentActivity) {
                            final BaseFragmentActivity baseFragmentActivity = (BaseFragmentActivity) activity;
                            if (error instanceof HttpStatusException && ((HttpStatusException) error).getStatusCode() == HttpStatus.BAD_REQUEST) {
                                final HashMap<String, CharSequence> params = new HashMap<>();
                                params.put("platform_name", environment.getConfig().getPlatformName());
                                final CharSequence message = ResourceUtil.getFormattedString(activity.getResources(), R.string.enrollment_error_message, params);
                                baseFragmentActivity.showAlertDialog(activity.getString(R.string.enrollment_error_title), message.toString());
                            } else {
                                showEnrollErrorMessage(baseFragmentActivity, courseId, emailOptIn);
                            }
                        }
                    }
                });
    }

    private void showEnrollErrorMessage(@NonNull BaseFragmentActivity baseFragmentActivity,
                                        final String courseId, final boolean emailOptIn) {
        if (baseFragmentActivity.isActivityStarted()) {
            Map<String, String> dialogMap = new HashMap<String, String>();
            dialogMap.put("message_1", activity.getString(R.string.enrollment_failure));
            dialogMap.put("yes_button", activity.getString(R.string.try_again));
            dialogMap.put("no_button", activity.getString(R.string.label_cancel));
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
            failureDialogFragment.show(activity.getSupportFragmentManager(), "dialog");
            failureDialogFragment.setCancelable(false);
        }
    }
}
