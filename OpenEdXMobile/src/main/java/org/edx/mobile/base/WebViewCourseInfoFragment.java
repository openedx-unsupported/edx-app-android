package org.edx.mobile.base;

import android.app.Activity;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;

import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentWebviewBinding;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.interfaces.WebViewStatusListener;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.links.DefaultActionListener;
import org.edx.mobile.view.BaseWebViewFragment;

import static org.edx.mobile.view.Router.EXTRA_PATH_ID;

public class WebViewCourseInfoFragment extends BaseWebViewFragment
        implements WebViewStatusListener {

    private static final int LOG_IN_REQUEST_CODE = 42;
    private static final String INSTANCE_COURSE_ID = "enrollCourseId";
    private static final String INSTANCE_EMAIL_OPT_IN = "enrollEmailOptIn";

    private String lastClickEnrollCourseId;
    private boolean lastClickEnrollEmailOptIn;

    private DefaultActionListener defaultActionListener;

    private FragmentWebviewBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_webview, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setWebViewActionListener();
        loadUrl(getInitialUrl());
        if (null != savedInstanceState) {
            lastClickEnrollCourseId = savedInstanceState.getString(INSTANCE_COURSE_ID);
            lastClickEnrollEmailOptIn = savedInstanceState.getBoolean(INSTANCE_EMAIL_OPT_IN);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INSTANCE_COURSE_ID, lastClickEnrollCourseId);
        outState.putBoolean(INSTANCE_EMAIL_OPT_IN, lastClickEnrollEmailOptIn);
    }

    public void setWebViewActionListener() {
        defaultActionListener = new DefaultActionListener(getActivity(), progressWheel,
                new DefaultActionListener.EnrollCallback() {
                    @Override
                    public void onResponse(@NonNull EnrolledCoursesResponse course) {

                    }

                    @Override
                    public void onFailure(@NonNull Throwable error) {
                    }

                    @Override
                    public void onUserNotLoggedIn(@NonNull String courseId, boolean emailOptIn) {
                        lastClickEnrollCourseId = courseId;
                        lastClickEnrollEmailOptIn = emailOptIn;
                        startActivityForResult(environment.getRouter().getRegisterIntent(), LOG_IN_REQUEST_CODE);
                    }
                });
        client.setActionListener(defaultActionListener);
    }

    @Override
    public FullScreenErrorNotification initFullScreenErrorNotification() {
        return new FullScreenErrorNotification(binding.webview);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOG_IN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            defaultActionListener.onClickEnroll(lastClickEnrollCourseId, lastClickEnrollEmailOptIn);
        }
    }

    /**
     * Loads the given URL into {@link #webView}.
     *
     * @param url The URL to load.
     */
    @Override
    protected void loadUrl(@NonNull String url) {
        if (client != null) {
            client.setLoadingInitialUrl(true);
        }
        super.loadUrl(url);
    }

    /**
     * By default, all links will not be treated as external.
     * Depends on host, as long as the links have same host, they are treated as non-external links.
     *
     * @return
     */
    protected boolean isAllLinksExternal() {
        return true;
    }

    @Override
    public void onRefresh() {
        loadUrl(getInitialUrl());
    }

    @NonNull
    protected String getInitialUrl() {
        if (URLUtil.isValidUrl(binding.webview.getUrl())) {
            return binding.webview.getUrl();
        } else if (getArguments() != null) {
            final String pathId = getArguments().getString(EXTRA_PATH_ID);
            return environment.getConfig().getDiscoveryConfig().getCourseDiscoveryConfig()
                    .getInfoUrlTemplate()
                    .replace("{" + EXTRA_PATH_ID + "}", pathId);
        }
        return environment.getConfig().getDiscoveryConfig().getCourseDiscoveryConfig().getBaseUrl();
    }

    @Override
    protected boolean isShowingFullScreenError() {
        return errorNotification != null && errorNotification.isShowing();
    }
}
