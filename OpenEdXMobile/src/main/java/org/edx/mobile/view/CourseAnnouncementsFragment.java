package org.edx.mobile.view;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.http.callback.ErrorHandlingOkCallback;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.http.provider.OkHttpClientProvider;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.WebViewUtil;
import org.edx.mobile.view.custom.EdxWebView;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.Request;

@AndroidEntryPoint
public class CourseAnnouncementsFragment extends BaseFragment implements RefreshListener {
    private final Logger logger = new Logger(getClass().getName());

    public static final String EXTRA_ANNOUNCEMENTS = "announcements";

    private EdxWebView webView;

    private EnrolledCoursesResponse courseData;

    private List<AnnouncementsModel> savedAnnouncements;

    @Inject
    protected IEdxEnvironment environment;

    @Inject
    OkHttpClientProvider okHttpClientProvider;

    private FullScreenErrorNotification errorNotification;

    public static Bundle makeArguments(EnrolledCoursesResponse courseData) {
        Bundle courseBundle = new Bundle();
        courseBundle.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
        return courseBundle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webview_with_paddings, container, false);

        webView = view.findViewById(R.id.webview);
        URLInterceptorWebViewClient client = new URLInterceptorWebViewClient(requireActivity(), webView,
                false, null);
        // treat every link as external link in this view, so that all links will open in external browser
        client.setAllLinksAsExternal(true);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        errorNotification = new FullScreenErrorNotification(webView);

        if (savedInstanceState != null) {
            try {
                savedAnnouncements = savedInstanceState.getParcelableArrayList(EXTRA_ANNOUNCEMENTS);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        if (getArguments() != null) {
            try {
                final Bundle bundle = getArguments();
                courseData = (EnrolledCoursesResponse) bundle.getSerializable(Router.EXTRA_COURSE_DATA);

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
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (savedAnnouncements != null) {
            outState.putParcelableArrayList(EXTRA_ANNOUNCEMENTS, new ArrayList<Parcelable>(savedAnnouncements));
        }
    }

    private void loadAnnouncementData(EnrolledCoursesResponse enrollment) {
        okHttpClientProvider.getWithOfflineCache().newCall(new Request.Builder()
                        .url(enrollment.getCourse().getCourse_updates())
                        .get()
                        .build())
                .enqueue(new ErrorHandlingOkCallback<>(
                        requireActivity(),
                        new TypeToken<List<AnnouncementsModel>>() {
                        },
                        errorNotification,
                        this
                ) {
                    @Override
                    protected void onResponse(@NonNull final List<AnnouncementsModel> announcementsList) {
                        if (getActivity() == null) {
                            return;
                        }
                        savedAnnouncements = announcementsList;
                        if (announcementsList.size() > 0) {
                            populateAnnouncements(announcementsList);
                        } else {
                            errorNotification.showError(R.string.no_announcements_to_display,
                                    R.drawable.ic_error, 0, null);
                        }
                    }

                    @Override
                    protected void onFinish() {
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

    @Override
    public void onRefresh() {
        errorNotification.hideError();
        loadAnnouncementData(courseData);
    }
}
