package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.view.custom.AuthenticatedWebView;

import roboguice.inject.InjectView;

/**
 * Provides a webview which authenticates the user before loading a page,
 * Javascript can also be passed in arguments for evaluation.
 */
public class AuthenticatedWebViewFragment extends BaseFragment {
    protected final Logger logger = new Logger(getClass().getName());
    public static final String ARG_URL = "ARG_URL";
    public static final String ARG_JAVASCRIPT = "ARG_JAVASCRIPT";

    @InjectView(R.id.auth_webview)
    private AuthenticatedWebView authWebView;

    public static Fragment newInstance(@NonNull String url) {
        return newInstance(url, null);
    }

    public static Fragment newInstance(@NonNull String url, @Nullable String javascript) {
        final Fragment fragment = new AuthenticatedWebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        args.putString(ARG_JAVASCRIPT, javascript);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_authenticated_webview, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        authWebView.initWebView(getActivity(), false);
        if (getArguments() != null) {
            final String url = getArguments().getString(ARG_URL);
            final String javascript = getArguments().getString(ARG_JAVASCRIPT);
            authWebView.loadUrlWithJavascript(true, url, javascript);
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
    }
}
