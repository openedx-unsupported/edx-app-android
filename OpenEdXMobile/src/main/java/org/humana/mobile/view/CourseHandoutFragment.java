package org.humana.mobile.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Xml.Encoding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.google.inject.Inject;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.humana.mobile.R;
import org.humana.mobile.base.BaseFragment;
import org.humana.mobile.core.IEdxEnvironment;
import org.humana.mobile.event.NetworkConnectivityChangeEvent;
import org.humana.mobile.http.callback.ErrorHandlingOkCallback;
import org.humana.mobile.http.notifications.FullScreenErrorNotification;
import org.humana.mobile.http.notifications.SnackbarErrorNotification;
import org.humana.mobile.http.provider.OkHttpClientProvider;
import org.humana.mobile.interfaces.RefreshListener;
import org.humana.mobile.logger.Logger;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.model.api.HandoutModel;
import org.humana.mobile.module.analytics.Analytics;
import org.humana.mobile.module.analytics.AnalyticsRegistry;
import org.humana.mobile.tta.analytics.analytics_enums.Nav;
import org.humana.mobile.tta.utils.BreadcrumbUtil;
import org.humana.mobile.util.NetworkUtil;
import org.humana.mobile.util.WebViewUtil;
import org.humana.mobile.view.common.PageViewStateCallback;
import org.humana.mobile.view.custom.URLInterceptorWebViewClient;

import de.greenrobot.event.EventBus;
import okhttp3.Request;
import roboguice.inject.InjectView;

public class CourseHandoutFragment extends BaseFragment
        implements RefreshListener, PageViewStateCallback {
    //Mx Chirag: rank used in breadcrumb
    private int RANK;

    protected final Logger logger = new Logger(getClass().getName());

//    @InjectExtra(Router.EXTRA_COURSE_DATA)
    private EnrolledCoursesResponse courseData;

    @Inject
    private AnalyticsRegistry analyticsRegistry;

    @Inject
    private IEdxEnvironment environment;

    @Inject
    private OkHttpClientProvider okHttpClientProvider;

    @InjectView(R.id.webview)
    private WebView webview;

    private FullScreenErrorNotification errorNotification;

    private SnackbarErrorNotification snackbarErrorNotification;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RANK = BreadcrumbUtil.getCurrentRank() + 1;

        //Mx: Chirag: Get EnrolledCoursesResponse from bundle argument
        if (getArguments() != null) {
            courseData = (EnrolledCoursesResponse) getArguments().getSerializable(Router.EXTRA_COURSE_DATA);
        }

        if (courseData != null) {
            analyticsRegistry.trackScreenView(Analytics.Screens.COURSE_HANDOUTS, courseData.getCourse().getId(), null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_handout, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        errorNotification = new FullScreenErrorNotification(webview);
        snackbarErrorNotification = new SnackbarErrorNotification(webview);
        new URLInterceptorWebViewClient(getActivity(), webview).setAllLinksAsExternal(true);
        loadData();
    }

    private void loadData() {
        if (courseData == null){
            return;
        }
        okHttpClientProvider.getWithOfflineCache().newCall(new Request.Builder()
                .url(courseData.getCourse().getCourse_handouts())
                .get()
                .build())
                .enqueue(new ErrorHandlingOkCallback<HandoutModel>(getActivity(),
                        HandoutModel.class, errorNotification, snackbarErrorNotification, this) {
                    @Override
                    protected void onResponse(@NonNull final HandoutModel result) {
                        if (getActivity() == null) {
                            return;
                        }

                        if (!TextUtils.isEmpty(result.handouts_html)) {
                            populateHandouts(result);
                        } else {
                            errorNotification.showError(R.string.no_handouts_to_display,
                                    FontAwesomeIcons.fa_exclamation_circle, 0, null);
                        }
                    }

                    @Override
                    protected void onFailure(@NonNull final Throwable error) {
                        super.onFailure(error);

                        if (getActivity() == null) {
                            return;
                        }
                    }

                    @Override
                    protected void onFinish() {
                        if (!EventBus.getDefault().isRegistered(CourseHandoutFragment.this)) {
                            EventBus.getDefault().registerSticky(CourseHandoutFragment.this);
                        }
                    }
                });
    }

    private void populateHandouts(HandoutModel handout) {
        hideErrorMessage();

        StringBuilder buff = WebViewUtil.getIntialWebviewBuffer(getActivity(), logger);

        buff.append("<body>");
        buff.append("<div class=\"header\">");
        buff.append(handout.handouts_html);
        buff.append("</div>");
        buff.append("</body>");

        webview.clearCache(true);
        webview.loadDataWithBaseURL(environment.getConfig().getApiHostURL(), buff.toString(),
                "text/html", Encoding.UTF_8.toString(), null);

    }

    private void hideErrorMessage() {
        webview.setVisibility(View.VISIBLE);
        errorNotification.hideError();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NetworkConnectivityChangeEvent event) {
        if (!NetworkUtil.isConnected(getContext())) {
            if (!errorNotification.isShowing()) {
                snackbarErrorNotification.showOfflineError(this);
            }
        }
    }

    @Override
    public void onRefresh() {
        errorNotification.hideError();
        loadData();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onRevisit() {
        if (NetworkUtil.isConnected(getActivity())) {
            snackbarErrorNotification.hideError();
        }
    }

    @Override
    public void onPageShow() {
        logger.debug("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.handout.name()));
    }

    @Override
    public void onPageDisappear() {

    }
}
