package org.edx.mobile.view;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.edx.mobile.R;
import org.edx.mobile.deeplink.Screen;
import org.edx.mobile.event.UnitLoadedEvent;
import org.edx.mobile.exception.AuthException;
import org.edx.mobile.exception.ErrorMessage;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.notifications.SnackbarErrorNotification;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseBannerInfoModel;
import org.edx.mobile.model.course.CourseBannerType;
import org.edx.mobile.model.course.HtmlBlockModel;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.CourseDateUtil;
import org.edx.mobile.view.custom.AuthenticatedWebView;
import org.edx.mobile.view.custom.PreLoadingListener;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;
import org.edx.mobile.viewModel.CourseDateViewModel;
import org.edx.mobile.viewModel.ViewModelFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectView;

public class CourseUnitWebViewFragment extends CourseUnitFragment {

    @InjectView(R.id.auth_webview)
    private AuthenticatedWebView authWebView;

    @InjectView(R.id.info_banner)
    private LinearLayout infoBanner;

    @InjectView(R.id.swipe_container)
    protected SwipeRefreshLayout swipeContainer;

    @InjectView(R.id.tv_open_browser)
    protected TextView tvOpenBrowser;

    private CourseDateViewModel courseDateViewModel;
    private PreLoadingListener preloadingListener;
    private boolean isPageLoading = false;
    private String enrollmentMode = "";
    private boolean isSelfPaced = true;
    private boolean evaluatediFrameJS = false;

    public static CourseUnitWebViewFragment newInstance(HtmlBlockModel unit, String enrollmentMode, boolean isSelfPaced) {
        CourseUnitWebViewFragment fragment = new CourseUnitWebViewFragment();
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        args.putString(Router.EXTRA_ENROLLMENT_MODE, enrollmentMode);
        args.putBoolean(Router.EXTRA_IS_SELF_PACED, isSelfPaced);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        return inflater.inflate(R.layout.fragment_authenticated_webview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        enrollmentMode = getStringArgument(Router.EXTRA_ENROLLMENT_MODE);
        isSelfPaced = getBooleanArgument(Router.EXTRA_IS_SELF_PACED, true);
        if (getActivity() instanceof PreLoadingListener) {
            preloadingListener = (PreLoadingListener) getActivity();
        } else {
            throw new RuntimeException("Parent activity of this Fragment should implement the PreLoadingListener interface");
        }
        swipeContainer.setEnabled(false);
        authWebView.initWebView(getActivity(), true, false, true,
                this::markComponentCompleted);
        authWebView.getWebViewClient().setPageStatusListener(new URLInterceptorWebViewClient.IPageStatusListener() {
            @Override
            public void onPageStarted() {
                isPageLoading = true;
            }

            @Override
            public void onPageFinished() {
                if (authWebView.isPageLoaded()) {
                    fetchDateBannerInfo();
                    evaluateXBlocksForBanner();
                    evaluateJavascriptForiFrame();
                    if (getUserVisibleHint()) {
                        preloadingListener.setLoadingState(PreLoadingListener.State.MAIN_UNIT_LOADED);
                    }
                    isPageLoading = false;
                    EventBus.getDefault().post(new UnitLoadedEvent());
                }
            }

            @Override
            public void onPageLoadError(WebView view, int errorCode, String description, String failingUrl) {
                isPageLoading = false;
            }

            @Override
            public void onPageLoadError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse, boolean isMainRequestFailure) {
                isPageLoading = false;
            }

            @Override
            public void onPageLoadProgressChanged(WebView webView, int progress) {
            }
        });

        // Only load the unit if it is currently visible to user or the visible unit has finished loading
        if (getUserVisibleHint() || preloadingListener.isMainUnitLoaded()) {
            loadUnit();
        }
        if (unit.getType() == BlockType.PROBLEM) {
            initObserver();
        }
        // Enable Pan & Zoom for specific HTML components
        if (unit.getType() == BlockType.DRAG_AND_DROP_V2) {
            authWebView.getWebView().getSettings().setSupportZoom(true);
            authWebView.getWebView().getSettings().setDisplayZoomControls(false);
            authWebView.getWebView().getSettings().setBuiltInZoomControls(true);
        }
    }

    private void evaluateXBlocksForBanner() {
        List<BlockType> allowedBlocks = Arrays.asList(BlockType.PROBLEM, BlockType.OPENASSESSMENT,
                BlockType.DRAG_AND_DROP_V2, BlockType.WORD_CLOUD);
        if (allowedBlocks.contains(unit.getType())) {
            setupOpenInBrowserView(R.string.open_in_new_tab_text);
        }
    }

    private void evaluateJavascriptForiFrame() {
        if (!TextUtils.isEmpty(unit.getBlockId()) && !evaluatediFrameJS) {
            // execute js code to check an HTML block that contains an iframe
            String javascript =
                    "try {" +
                            "    var top_div_list = document.querySelectorAll('div[data-usage-id=\"" + unit.getId() + "\"]');\n" +
                            "    top_div_list.length == 1 && top_div_list[0].querySelectorAll(\"iframe\").length > 0;" +
                            "} catch {" +
                            "    false;" +
                            "};";
            authWebView.evaluateJavascript(javascript, value -> {
                evaluatediFrameJS = true;
                if (Boolean.parseBoolean(value)) {
                    setupOpenInBrowserView(R.string.open_in_browser_text);
                }
            });
        }
    }

    private void setupOpenInBrowserView(@StringRes int linkTextResId) {
        tvOpenBrowser.setVisibility(View.VISIBLE);

        String openInBrowserMessage = getString(R.string.open_in_browser_message) + " "
                + getString(linkTextResId) + " " + AppConstants.ICON_PLACEHOLDER;
        SpannableString openInBrowserSpan = new SpannableString(openInBrowserMessage);

        ImageSpan openInNewIcon = new ImageSpan(getContext(), R.drawable.ic_open_in_new);
        openInBrowserSpan.setSpan(openInNewIcon, openInBrowserMessage.indexOf(AppConstants.ICON_PLACEHOLDER),
                openInBrowserMessage.length(), DynamicDrawableSpan.ALIGN_BASELINE);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NotNull View textView) {
                BrowserUtil.open(getActivity(), unit.getWebUrl(), true);
            }

            @Override
            public void updateDrawState(@NotNull TextPaint textPaint) {
                textPaint.setUnderlineText(true);
                super.updateDrawState(textPaint);
            }
        };
        int openInBrowserIndex = openInBrowserMessage.indexOf(getString(linkTextResId));
        openInBrowserSpan.setSpan(clickableSpan, openInBrowserIndex,
                openInBrowserIndex + getString(linkTextResId).length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        openInBrowserSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.neutralXXDark)),
                openInBrowserIndex, openInBrowserIndex + getString(linkTextResId).length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        tvOpenBrowser.setText(openInBrowserSpan);
        tvOpenBrowser.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void fetchDateBannerInfo() {
        // Show course dates banner in assignment view only if the course is self paced
        if (unit.getType() == BlockType.PROBLEM && isSelfPaced) {
            courseDateViewModel.fetchCourseDatesBannerInfo(unit.getCourseId(), true);
        }
    }

    private void initInfoBanner(CourseBannerInfoModel courseBannerInfo) {
        if (courseBannerInfo == null || courseBannerInfo.getHasEnded()
                || courseBannerInfo.getDatesBannerInfo().getCourseBannerType() != CourseBannerType.RESET_DATES) {
            infoBanner.setVisibility(View.GONE);
            return;
        }
        CourseDateUtil.INSTANCE.setupCourseDatesBanner(infoBanner, unit.getCourseId(), enrollmentMode, isSelfPaced,
                Analytics.Screens.PLS_COURSE_UNIT_ASSIGNMENT, environment.getAnalyticsRegistry(), courseBannerInfo,
                v -> courseDateViewModel.resetCourseDatesBanner(unit.getCourseId()));
    }

    private void initObserver() {
        courseDateViewModel = new ViewModelProvider(this, new ViewModelFactory()).get(CourseDateViewModel.class);

        courseDateViewModel.getBannerInfo().observe(getViewLifecycleOwner(), this::initInfoBanner);

        courseDateViewModel.getShowLoader().observe(getViewLifecycleOwner(), flag ->
                preloadingListener.setLoadingState(flag ?
                        PreLoadingListener.State.MAIN_UNIT_LOADING :
                        PreLoadingListener.State.MAIN_UNIT_LOADED));

        courseDateViewModel.getResetCourseDates().observe(getViewLifecycleOwner(), resetCourseDates -> {
            if (resetCourseDates != null) {
                showShiftDateSnackBar(true);
                authWebView.loadUrl(true, unit.getBlockUrl());
            }
        });

        courseDateViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                if (errorMessage.getThrowable() instanceof AuthException || errorMessage.getThrowable() instanceof HttpStatusException &&
                        ((HttpStatusException) errorMessage.getThrowable()).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    environment.getRouter().forceLogout(getContextOrThrow(),
                            environment.getAnalyticsRegistry(),
                            environment.getNotificationDelegate());
                } else {
                    switch (errorMessage.getErrorCode()) {
                        case ErrorMessage.BANNER_INFO_CODE:
                            initInfoBanner(null);
                            break;
                        case ErrorMessage.COURSE_RESET_DATES_CODE:
                            showShiftDateSnackBar(false);
                            break;
                    }
                }
            }
        });
    }

    private void showShiftDateSnackBar(boolean isSuccess) {
        SnackbarErrorNotification snackbarErrorNotification = new SnackbarErrorNotification(authWebView);
        if (isSuccess) {
            snackbarErrorNotification.showError(R.string.assessment_shift_dates_success_msg,
                    null, R.string.assessment_view_all_dates, SnackbarErrorNotification.COURSE_DATE_MESSAGE_DURATION,
                    v -> environment.getRouter().showCourseDashboardTabs(getActivity(), null, unit.getCourseId(),
                            null, null, false, Screen.COURSE_DATES));
        } else {
            snackbarErrorNotification.showError(R.string.course_dates_reset_unsuccessful, null,
                    0, SnackbarErrorNotification.COURSE_DATE_MESSAGE_DURATION, null);
        }
        environment.getAnalyticsRegistry().trackPLSCourseDatesShift(unit.getCourseId(), enrollmentMode,
                Analytics.Screens.PLS_COURSE_UNIT_ASSIGNMENT, isSuccess);
    }

    private void loadUnit() {
        if (authWebView != null) {
            if (!authWebView.isPageLoaded() && !isPageLoading) {
                authWebView.loadUrl(true, unit.getBlockUrl());
                if (getUserVisibleHint()) {
                    preloadingListener.setLoadingState(PreLoadingListener.State.MAIN_UNIT_LOADING);
                }
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // Only load the unit if it is currently visible to user
        if (isVisibleToUser) {
            loadUnit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        authWebView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        authWebView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        authWebView.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        authWebView.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(UnitLoadedEvent event) {
        loadUnit();
    }
}
