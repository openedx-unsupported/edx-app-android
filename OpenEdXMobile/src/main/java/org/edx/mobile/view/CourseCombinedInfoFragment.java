package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.facebook.Settings;
import com.facebook.widget.LikeView;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.http.callback.CallTrigger;
import org.edx.mobile.http.callback.ErrorHandlingOkCallback;
import org.edx.mobile.http.provider.OkHttpClientProvider;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.facebook.IUiLifecycleHelper;
import org.edx.mobile.social.facebook.FacebookProvider;
import org.edx.mobile.util.StandardCharsets;
import org.edx.mobile.util.WebViewUtil;
import org.edx.mobile.view.common.TaskProgressCallback;
import org.edx.mobile.view.custom.EdxWebView;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

public class CourseCombinedInfoFragment extends BaseFragment {

    static final String TAG = CourseCombinedInfoFragment.class.getCanonicalName();

    private final Logger logger = new Logger(getClass().getName());

    private EdxWebView announcementWebView;
    private View notificationSettingRow;
    private Switch notificationSwitch;

    private EnrolledCoursesResponse courseData;
    private List<AnnouncementsModel> savedAnnouncements;
    private IUiLifecycleHelper uiHelper;

    @Inject
    protected IEdxEnvironment environment;

    @Inject
    private OkHttpClientProvider okHttpClientProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger.debug("created: " + getClass().getName());

        final Context context = getContext();

        Settings.sdkInitialize(context);

        uiHelper = IUiLifecycleHelper.Factory.getInstance(getActivity(), null);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_course_combined_info, container, false);

        announcementWebView = (EdxWebView) view.findViewById(R.id.announcement_webview);
        URLInterceptorWebViewClient client = new URLInterceptorWebViewClient(
                getActivity(), announcementWebView);
        // treat every link as external link in this view, so that all links will open in external browser
        client.setAllLinksAsExternal(true);

        notificationSettingRow = view.findViewById(R.id.notification_setting_row);
        notificationSwitch = (Switch) view.findViewById(R.id.notification_switch);

        return view;
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

            if (environment.getConfig().isNotificationEnabled()
                    && courseData != null && courseData.getCourse() != null) {
                notificationSettingRow.setVisibility(View.VISIBLE);
                final String courseId = courseData.getCourse().getId();
                final String subscriptionId = courseData.getCourse().getSubscription_id();
                boolean isSubscribed = environment.getNotificationDelegate().isSubscribedByCourseId(courseId);
                notificationSwitch.setChecked(isSubscribed);
                notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        environment.getNotificationDelegate().changeNotificationSetting(
                                courseId, subscriptionId, isChecked);
                    }
                });
            } else {
                notificationSwitch.setOnCheckedChangeListener(null);
                notificationSettingRow.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        LikeView.handleOnActivityResult(getActivity(), requestCode, resultCode, data);
        //
        uiHelper.onActivityResult(requestCode, resultCode, data, null);

    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (savedAnnouncements != null) {
            outState.putParcelableArrayList(Router.EXTRA_ANNOUNCEMENTS, new ArrayList<Parcelable>(savedAnnouncements));
        }
        uiHelper.onSaveInstanceState(outState);

    }

    private void loadAnnouncementData(EnrolledCoursesResponse enrollment) {
        okHttpClientProvider.getWithOfflineCache().newCall(new Request.Builder()
                .url(enrollment.getCourse().getCourse_updates())
                .get()
                .build())
                .enqueue(new ErrorHandlingOkCallback<List<AnnouncementsModel>>(
                        getActivity(),
                        new TypeToken<List<AnnouncementsModel>>() {},
                        CallTrigger.LOADING_CACHED,
                        new TaskProgressCallback.ProgressViewController(
                                getView().findViewById(R.id.loading_indicator))) {
                    @Override
                    protected void onResponse(
                            @NonNull final List<AnnouncementsModel> announcementsList) {
                        savedAnnouncements = announcementsList;
                        populateAnnouncements(announcementsList);
                    }

                    @Override
                    protected void onFailure(@NonNull final Throwable error) {
                        super.onFailure(error);
                        showEmptyAnnouncementMessage();
                    }
                });

    }

    private void populateAnnouncements(List<AnnouncementsModel> announcementsList) {
        if (announcementsList != null && announcementsList.size() > 0) {
            hideEmptyAnnouncementMessage();

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

            announcementWebView.clearCache(true);
            announcementWebView.loadDataWithBaseURL(environment.getConfig().getApiHostURL(), buff.toString(), "text/html", StandardCharsets.UTF_8.name(), null);
        } else {
            showEmptyAnnouncementMessage();
        }
    }

    public void showEmptyAnnouncementMessage() {
        try {
            if (getView() != null) {
                getView().findViewById(R.id.no_announcement_tv).setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            logger.error(e);
        }

    }

    private void hideEmptyAnnouncementMessage() {
        try {
            if (getView() != null) {
                getView().findViewById(R.id.no_announcement_tv).setVisibility(View.GONE);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
