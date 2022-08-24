package org.edx.mobile.view;

import android.os.Bundle;
import android.provider.CalendarContract;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProvider;

import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentAuthenticatedWebviewBinding;
import org.edx.mobile.deeplink.Screen;
import org.edx.mobile.event.UnitLoadedEvent;
import org.edx.mobile.exception.ErrorMessage;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.notifications.SnackbarErrorNotification;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseBannerInfoModel;
import org.edx.mobile.model.course.CourseBannerType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.HtmlBlockModel;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.CalendarUtils;
import org.edx.mobile.util.ConfigUtil;
import org.edx.mobile.util.CourseDateUtil;
import org.edx.mobile.view.custom.PreLoadingListener;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;
import org.edx.mobile.view.dialog.AlertDialogFragment;
import org.edx.mobile.viewModel.CourseDateViewModel;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CourseUnitWebViewFragment extends CourseUnitFragment {

    @Inject
    CourseManager courseManager;

    private CourseDateViewModel courseDateViewModel;
    private PreLoadingListener preloadingListener;
    private boolean isPageLoading = false;
    private String enrollmentMode = "";
    private boolean isSelfPaced = true;
    private boolean evaluatediFrameJS = false;
    private String courseName = "";
    private String calendarTitle = "";
    private String accountName = "";
    private boolean forceReloadComponent = false;
    private FragmentAuthenticatedWebviewBinding binding;
    private AlertDialogFragment loaderDialog;

    public static CourseUnitWebViewFragment newInstance(HtmlBlockModel unit, String courseName, String enrollmentMode, boolean isSelfPaced) {
        CourseUnitWebViewFragment fragment = new CourseUnitWebViewFragment();
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        args.putSerializable(Router.EXTRA_COURSE_NAME, courseName);
        args.putString(Router.EXTRA_ENROLLMENT_MODE, enrollmentMode);
        args.putBoolean(Router.EXTRA_IS_SELF_PACED, isSelfPaced);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        binding = FragmentAuthenticatedWebviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        enrollmentMode = getStringArgument(Router.EXTRA_ENROLLMENT_MODE);
        isSelfPaced = getBooleanArgument(Router.EXTRA_IS_SELF_PACED, true);
        courseName = getStringArgument(Router.EXTRA_COURSE_NAME);
        calendarTitle = CalendarUtils.getCourseCalendarTitle(environment, courseName);
        accountName = CalendarUtils.getUserAccountForSync(environment);
        loaderDialog = AlertDialogFragment.newInstance(R.string.title_syncing_calendar, R.layout.alert_dialog_progress);
        if (getActivity() instanceof PreLoadingListener) {
            preloadingListener = (PreLoadingListener) getActivity();
        } else {
            throw new RuntimeException("Parent activity of this Fragment should implement the PreLoadingListener interface");
        }
        binding.swipeContainer.setEnabled(false);
        binding.authWebview.initWebView(getActivity(), true, false, true,
                this::markComponentCompletion, null);
        binding.authWebview.getWebViewClient().setPageStatusListener(new URLInterceptorWebViewClient.IPageStatusListener() {
            @Override
            public void onPageStarted() {
                isPageLoading = true;
            }

            @Override
            public void onPageFinished() {
                if (binding.authWebview.isPageLoaded()) {
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
            loadUnit(false);
        }
        if (unit.getType() == BlockType.PROBLEM) {
            initObserver();
        }
        // Enable Pan & Zoom for specific HTML components
        if (unit.getType() == BlockType.DRAG_AND_DROP_V2) {
            binding.authWebview.getWebView().getSettings().setSupportZoom(true);
            binding.authWebview.getWebView().getSettings().setDisplayZoomControls(false);
            binding.authWebview.getWebView().getSettings().setBuiltInZoomControls(true);
        }
    }

    private void evaluateXBlocksForBanner() {
        List<BlockType> notPermittedBlocks = Arrays.asList(BlockType.DISCUSSION, BlockType.HTML, BlockType.VIDEO);
        if (!notPermittedBlocks.contains(unit.getType())) {
            setupOpenInBrowserView();
        }
    }

    private void evaluateJavascriptForiFrame() {
        if (!TextUtils.isEmpty(unit.getBlockId()) && !evaluatediFrameJS) {
            // execute js code to check an HTML block that contains an iframe
            String javascript =
                    "try {" +
                            "    var top_div_list = document.querySelectorAll('div[data-usage-id=\"" + unit.getId() + "\"]');\n" +
                            "    top_div_list.length == 1 && top_div_list[0].querySelectorAll(\"iframe\").length > 0;" +
                            "} catch(err) {" +
                            "    false;" +
                            "};";
            binding.authWebview.evaluateJavascript(javascript, value -> {
                evaluatediFrameJS = true;
                if (Boolean.parseBoolean(value)) {
                    setupOpenInBrowserView();
                }
            });
        }
    }

    private void setupOpenInBrowserView() {
        if (getContext() != null) {
            @StringRes int linkTextResId = R.string.open_in_browser_text;
            binding.tvOpenBrowser.setVisibility(View.VISIBLE);

            String openInBrowserMessage = getString(R.string.open_in_browser_message) + " "
                    + getString(linkTextResId) + " " + AppConstants.ICON_PLACEHOLDER;
            SpannableString openInBrowserSpan = new SpannableString(openInBrowserMessage);

            ImageSpan openInNewIcon = new ImageSpan(requireContext(), R.drawable.ic_open_in_new);
            openInBrowserSpan.setSpan(openInNewIcon, openInBrowserMessage.indexOf(AppConstants.ICON_PLACEHOLDER),
                    openInBrowserMessage.length(), DynamicDrawableSpan.ALIGN_BASELINE);

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NotNull View textView) {
                    CourseComponent component = courseManager.getComponentByIdFromAppLevelCache(unit.getCourseId(), unit.getId());
                    environment.getRouter().showAuthenticatedWebViewActivity(requireContext(), component.getParent());
                    forceReloadComponent = true;
                    trackOpenInBrowserBannerEvent(Analytics.Events.OPEN_IN_BROWSER_BANNER_TAPPED,
                            Analytics.Values.OPEN_IN_BROWSER_BANNER_TAPPED);
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

            binding.tvOpenBrowser.setText(openInBrowserSpan);
            binding.tvOpenBrowser.setMovementMethod(LinkMovementMethod.getInstance());
            trackOpenInBrowserBannerEvent(Analytics.Events.OPEN_IN_BROWSER_BANNER_DISPLAYED,
                    Analytics.Values.OPEN_IN_BROWSER_BANNER_DISPLAYED);
        }
    }

    private void trackOpenInBrowserBannerEvent(String eventName, String biValue) {
        environment.getAnalyticsRegistry().trackOpenInBrowserBannerEvent(eventName, biValue,
                enrollmentMode, unit.getCourseId(), unit.getId(),
                unit.getType().toString().toLowerCase(), unit.getWebUrl());
    }

    private void fetchDateBannerInfo() {
        // Show course dates banner in assignment view only if the course is self paced
        if (unit.getType() == BlockType.PROBLEM && isSelfPaced) {
            courseDateViewModel.fetchCourseDates(unit.getCourseId(), false, true, false);
        }
    }

    private void initInfoBanner(CourseBannerInfoModel courseBannerInfo) {
        if (courseBannerInfo == null || courseBannerInfo.getHasEnded()
                || courseBannerInfo.getDatesBannerInfo().getCourseBannerType() != CourseBannerType.RESET_DATES) {
            binding.infoBanner.containerLayout.setVisibility(View.GONE);
            return;
        }
        CourseDateUtil.INSTANCE.setupCourseDatesBanner(binding.infoBanner.containerLayout, unit.getCourseId(), enrollmentMode, isSelfPaced,
                Analytics.Screens.PLS_COURSE_UNIT_ASSIGNMENT, environment.getAnalyticsRegistry(), courseBannerInfo,
                v -> courseDateViewModel.resetCourseDatesBanner(unit.getCourseId()));
    }

    private void initObserver() {
        courseDateViewModel = new ViewModelProvider(this).get(CourseDateViewModel.class);

        courseDateViewModel.getSyncLoader().observe(getViewLifecycleOwner(), showLoader -> {
            if (showLoader) {
                loaderDialog.setCancelable(false);
                loaderDialog.showNow(getChildFragmentManager(), null);
            } else {
                loaderDialog.dismiss();
                showCalendarUpdatedSnackbar();
                trackCalendarEvent(Analytics.Events.CALENDAR_UPDATE_SUCCESS, Analytics.Values.CALENDAR_UPDATE_SUCCESS);
            }
        });

        courseDateViewModel.getCourseDates().observe(getViewLifecycleOwner(), courseDates -> {
            if (courseDates.getCourseDateBlocks() != null) {
                courseDates.organiseCourseDates();
                long outdatedCalenderId = CalendarUtils.isCalendarOutOfDate(
                        requireContext(), accountName, calendarTitle, courseDates.getCourseDateBlocks());
                if (outdatedCalenderId != -1L) {
                    showCalendarOutOfDateDialog(outdatedCalenderId);
                }
            }
        });

        courseDateViewModel.getBannerInfo().observe(getViewLifecycleOwner(), this::initInfoBanner);

        courseDateViewModel.getShowLoader().observe(getViewLifecycleOwner(), flag ->
                preloadingListener.setLoadingState(flag ?
                        PreLoadingListener.State.MAIN_UNIT_LOADING :
                        PreLoadingListener.State.MAIN_UNIT_LOADED));

        courseDateViewModel.getResetCourseDates().observe(getViewLifecycleOwner(), resetCourseDates -> {
            if (resetCourseDates != null) {
                binding.authWebview.loadUrl(true, unit.getBlockUrl());
                if (!CalendarUtils.INSTANCE.isCalendarExists(getContextOrThrow(), accountName, calendarTitle)) {
                    showShiftDateSnackBar(true);
                }
            }
        });

        courseDateViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                if (errorMessage.getThrowable() instanceof HttpStatusException &&
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

    private void showCalendarOutOfDateDialog(Long calendarId) {
        AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(getString(R.string.title_calendar_out_of_date),
                getString(R.string.message_calendar_out_of_date), getString(R.string.label_update_now), (dialogInterface, which) -> updateCalendarEvents(),
                getString(R.string.label_remove_course_calendar), (dialogInterface, which) -> removeCalendar(calendarId));
        alertDialogFragment.setCancelable(false);
        alertDialogFragment.show(getChildFragmentManager(), null);
    }

    private void showShiftDateSnackBar(boolean isSuccess) {
        SnackbarErrorNotification snackbarErrorNotification = new SnackbarErrorNotification(binding.authWebview);
        if (isSuccess) {
            snackbarErrorNotification.showError(R.string.assessment_shift_dates_success_msg,
                    0, R.string.assessment_view_all_dates, SnackbarErrorNotification.COURSE_DATE_MESSAGE_DURATION,
                    v -> environment.getRouter().showCourseDashboardTabs(getActivity(), unit.getCourseId(), Screen.COURSE_DATES));
        } else {
            snackbarErrorNotification.showError(R.string.course_dates_reset_unsuccessful, 0,
                    0, SnackbarErrorNotification.COURSE_DATE_MESSAGE_DURATION, null);
        }
        environment.getAnalyticsRegistry().trackPLSCourseDatesShift(unit.getCourseId(), enrollmentMode,
                Analytics.Screens.PLS_COURSE_UNIT_ASSIGNMENT, isSuccess);
    }

    private void updateCalendarEvents() {
        trackCalendarEvent(Analytics.Events.CALENDAR_SYNC_UPDATE, Analytics.Values.CALENDAR_SYNC_UPDATE);
        long newCalId = CalendarUtils.createOrUpdateCalendar(getContextOrThrow(), accountName, CalendarContract.ACCOUNT_TYPE_LOCAL, calendarTitle);
        ConfigUtil.Companion.checkCalendarSyncEnabled(environment.getConfig(), response ->
                courseDateViewModel.addOrUpdateEventsInCalendar(getContextOrThrow(),
                        newCalId, unit.getCourseId(), courseName, response.isDeepLinkEnabled(), true));
    }

    private void removeCalendar(Long calendarId) {
        trackCalendarEvent(Analytics.Events.CALENDAR_SYNC_REMOVE, Analytics.Values.CALENDAR_SYNC_REMOVE);
        CalendarUtils.INSTANCE.deleteCalendar(getContextOrThrow(), calendarId);
        showCalendarRemovedSnackbar();
        trackCalendarEvent(Analytics.Events.CALENDAR_REMOVE_SUCCESS, Analytics.Values.CALENDAR_REMOVE_SUCCESS);
    }

    private void trackCalendarEvent(String eventName, String biValue) {
        environment.getAnalyticsRegistry().trackCalendarEvent(eventName, biValue, unit.getCourseId(),
                enrollmentMode, isSelfPaced, courseDateViewModel.getSyncingCalendarTime());
        courseDateViewModel.resetSyncingCalendarTime();
    }

    private void loadUnit(Boolean forceRefresh) {
        if (binding.authWebview != null) {
            if (forceRefresh || (!binding.authWebview.isPageLoaded() && !isPageLoading)) {
                binding.authWebview.loadUrl(true, unit.getBlockUrl());
                if (isVisible()) {
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
            loadUnit(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.authWebview.onResume();
        // Forcefully reload the unit if returning from Authenticated WebView Activity
        if (forceReloadComponent) {
            loadUnit(true);
            forceReloadComponent = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.authWebview.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.authWebview.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.authWebview.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEventMainThread(UnitLoadedEvent event) {
        loadUnit(false);
    }
}
