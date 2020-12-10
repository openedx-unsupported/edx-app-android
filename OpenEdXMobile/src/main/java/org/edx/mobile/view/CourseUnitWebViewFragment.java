package org.edx.mobile.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import org.edx.mobile.util.CourseDateUtil;
import org.edx.mobile.view.custom.AuthenticatedWebView;
import org.edx.mobile.view.custom.PreLoadingListener;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;
import org.edx.mobile.viewModel.CourseDateViewModel;
import org.edx.mobile.viewModel.ViewModelFactory;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectView;

public class CourseUnitWebViewFragment extends CourseUnitFragment {

    @InjectView(R.id.auth_webview)
    private AuthenticatedWebView authWebView;

    @InjectView(R.id.info_banner)
    private LinearLayout infoBanner;

    @InjectView(R.id.swipe_container)
    protected SwipeRefreshLayout swipeContainer;

    private CourseDateViewModel courseDateViewModel;
    private PreLoadingListener preloadingListener;
    private boolean isPageLoading = false;
    private String enrollmentMode = "";

    public static CourseUnitWebViewFragment newInstance(HtmlBlockModel unit, String enrollmentMode) {
        CourseUnitWebViewFragment fragment = new CourseUnitWebViewFragment();
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        args.putString(Router.EXTRA_ENROLLMENT_MODE, enrollmentMode);
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
        if (getActivity() instanceof PreLoadingListener) {
            preloadingListener = (PreLoadingListener) getActivity();
        } else {
            throw new RuntimeException("Parent activity of this Fragment should implement the PreLoadingListener interface");
        }
        swipeContainer.setEnabled(false);
        authWebView.initWebView(getActivity(), true, false);
        authWebView.getWebViewClient().setPageStatusListener(new URLInterceptorWebViewClient.IPageStatusListener() {
            @Override
            public void onPageStarted() {
                isPageLoading = true;
            }

            @Override
            public void onPageFinished() {
                if (authWebView.isPageLoaded()) {
                    fetchDateBannerInfo();
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
    }

    private void fetchDateBannerInfo() {
        if (unit.getType() == BlockType.PROBLEM) {
            courseDateViewModel.fetchCourseDatesBannerInfo(unit.getCourseId(), true);
        }
    }

    private void initInfoBanner(CourseBannerInfoModel courseBannerInfo) {
        if (courseBannerInfo == null || courseBannerInfo.getHasEnded()
                || courseBannerInfo.getDatesBannerInfo().getCourseBannerType() != CourseBannerType.RESET_DATES) {
            infoBanner.setVisibility(View.GONE);
            return;
        }
        CourseDateUtil.INSTANCE.setupCourseDatesBanner(infoBanner, unit.getCourseId(), enrollmentMode,
                Analytics.Screens.PLS_COURSE_UNIT_ASSIGNMENT, environment.getAnalyticsRegistry(), courseBannerInfo,
                v -> courseDateViewModel.resetCourseDatesBanner(unit.getCourseId()));
    }

    private void initObserver() {
        courseDateViewModel = new ViewModelProvider(this, new ViewModelFactory()).get(CourseDateViewModel.class);

        courseDateViewModel.getBannerInfo().observe(this, this::initInfoBanner);

        courseDateViewModel.getShowLoader().observe(this, flag ->
                preloadingListener.setLoadingState(flag ?
                        PreLoadingListener.State.MAIN_UNIT_LOADING :
                        PreLoadingListener.State.MAIN_UNIT_LOADED));

        courseDateViewModel.getResetCourseDates().observe(this, resetCourseDates -> {
            if (resetCourseDates != null) {
                showShiftDateSnackBar(true);
                authWebView.loadUrl(true, unit.getBlockUrl());
            }
        });

        courseDateViewModel.getErrorMessage().observe(this, errorMessage -> {
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
