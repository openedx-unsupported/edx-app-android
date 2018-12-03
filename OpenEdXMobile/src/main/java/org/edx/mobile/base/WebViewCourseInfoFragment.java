package org.edx.mobile.base;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;

import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentFindCourseInfoBinding;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.interfaces.WebViewStatusListener;
import org.edx.mobile.view.BaseWebViewDiscoverFragment;
import org.edx.mobile.view.CourseInfoActivity;

public class WebViewCourseInfoFragment extends BaseWebViewDiscoverFragment
        implements WebViewStatusListener {

    private FragmentFindCourseInfoBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_find_course_info, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadUrl(getInitialUrl());
    }

    @Override
    public FullScreenErrorNotification initFullScreenErrorNotification() {
        return new FullScreenErrorNotification(binding.webview);
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
            final String pathId = getArguments().getString(CourseInfoActivity.EXTRA_PATH_ID);
            return environment.getConfig().getCourseDiscoveryConfig()
                    .getCourseInfoUrlTemplate()
                    .replace("{" + CourseInfoActivity.EXTRA_PATH_ID + "}", pathId);
        }
        return environment.getConfig().getCourseDiscoveryConfig().getCourseSearchUrl();
    }

    @Override
    protected boolean isShowingFullScreenError() {
        return errorNotification != null && errorNotification.isShowing();
    }
}
