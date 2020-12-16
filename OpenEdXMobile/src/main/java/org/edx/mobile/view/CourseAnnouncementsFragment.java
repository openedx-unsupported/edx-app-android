package org.edx.mobile.view;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.http.callback.ErrorHandlingOkCallback;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.http.notifications.SnackbarErrorNotification;
import org.edx.mobile.http.provider.OkHttpClientProvider;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.social.facebook.FacebookProvider;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.WebViewUtil;
import org.edx.mobile.view.custom.EdxWebView;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.Request;

public class CourseAnnouncementsFragment extends BaseFragment implements RefreshListener {
    private final Logger logger = new Logger(getClass().getName());

    private EdxWebView webView;

    private EnrolledCoursesResponse courseData;
    private List<AnnouncementsModel> savedAnnouncements;

    @Inject
    protected IEdxEnvironment environment;

    @Inject
    private OkHttpClientProvider okHttpClientProvider;

    private FullScreenErrorNotification errorNotification;

    private SnackbarErrorNotification snackbarErrorNotification;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_webview_with_paddings, container, false);

        webView = view.findViewById(R.id.webview);
        URLInterceptorWebViewClient client = new URLInterceptorWebViewClient(
                getActivity(), webView);
        // treat every link as external link in this view, so that all links will open in external browser
        client.setAllLinksAsExternal(true);


        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        errorNotification = new FullScreenErrorNotification(webView);
        snackbarErrorNotification = new SnackbarErrorNotification(webView);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {

            try {
                savedAnnouncements = savedInstanceState.getParcelableArrayList(Router.EXTRA_ANNOUNCEMENTS);
            } catch (Exception ex) {
                logger.error(ex);
            }

        }

        try {
            final Bundle bundle = getArguments();
            courseData = (EnrolledCoursesResponse) bundle.getSerializable(Router.EXTRA_COURSE_DATA);
            FacebookProvider fbProvider = new FacebookProvider();

            if (courseData != null) {
                //Create the inflater used to create the announcement list
                if (savedAnnouncements == null) {
                    loadAnnouncementData(courseData);
                } else {
                    populateAnnouncements(savedAnnouncements);
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (savedAnnouncements != null) {
            outState.putParcelableArrayList(Router.EXTRA_ANNOUNCEMENTS, new ArrayList<Parcelable>(savedAnnouncements));
        }
    }

    private void loadAnnouncementData(EnrolledCoursesResponse enrollment) {
        okHttpClientProvider.getWithOfflineCache().newCall(new Request.Builder()
                .url(enrollment.getCourse().getCourse_updates())
                .get()
                .build())
                .enqueue(new ErrorHandlingOkCallback<List<AnnouncementsModel>>(getActivity(),
                        new TypeToken<List<AnnouncementsModel>>() {
                        }, errorNotification, snackbarErrorNotification,
                        this) {
                    @Override
                    protected void onResponse(final List<AnnouncementsModel> announcementsList) {
                        if (getActivity() == null) {
                            return;
                        }
                        savedAnnouncements = announcementsList;
                        if (announcementsList != null && announcementsList.size() > 0) {
                            populateAnnouncements(announcementsList);
                        } else {
                            errorNotification.showError(R.string.no_announcements_to_display,
                                    FontAwesomeIcons.fa_exclamation_circle, 0, null);
                        }
                    }

                    @Override
                    protected void onFinish() {
                        if (getActivity() == null) {
                            return;
                        }
                        if (!EventBus.getDefault().isRegistered(CourseAnnouncementsFragment.this)) {
                            EventBus.getDefault().registerSticky(CourseAnnouncementsFragment.this);
                        }
                    }
                });

    }

    private void populateAnnouncements(@NonNull List<AnnouncementsModel> announcementsList) {
        errorNotification.hideError();

        StringBuilder buff = WebViewUtil.getIntialWebviewBuffer(getActivity(), logger);

        buff.append("<body>");
        for (AnnouncementsModel model : announcementsList) {
            buff.append("<div class=\"header\">");
            buff.append(model.getDate());
            buff.append("</div>");
            buff.append("<div class=\"separator\"></div>");
            buff.append("<div>");
            buff.append(model.getContent());
            buff.append("</div>");
        }
        buff.append("</body>");

        webView.loadDataWithBaseURL(environment.getConfig().getApiHostURL(), buff.toString(), "text/html", StandardCharsets.UTF_8.name(), null);
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
        loadAnnouncementData(courseData);
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
}
