package org.edx.mobile.view;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.TextUtils;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconImageView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.view.custom.AuthenticatedWebView;

import roboguice.inject.InjectView;

/**
 * Provides a webview which authenticates the user before loading a page,
 * Javascript can also be passed in arguments for evaluation.
 */
public class AuthenticatedWebViewFragment extends BaseFragment {
    public static final String ARG_URL = "ARG_URL";
    public static final String ARG_JAVASCRIPT = "ARG_JAVASCRIPT";
    public static final String ARG_IS_MANUALLY_RELOADABLE = "ARG_IS_MANUALLY_RELOADABLE";
    protected final Logger logger = new Logger(getClass().getName());

    @org.edx.mobile.annotation.Nullable
    @InjectView(R.id.auth_webview)
    protected AuthenticatedWebView authWebView;

    @org.edx.mobile.annotation.Nullable
    @InjectView(R.id.swipe_container)
    protected SwipeRefreshLayout swipeContainer;

    @org.edx.mobile.annotation.Nullable
    @InjectView(R.id.content_error_text)
    protected TextView tvContentError;

    @org.edx.mobile.annotation.Nullable
    @InjectView(R.id.content_error_action)
    protected Button btnContentErrorAction;

    private boolean isSystemUpdatingWebView = false;

    public static Bundle makeArguments(@NonNull String url, @Nullable String javascript, boolean isManuallyReloadable) {
        final Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        args.putBoolean(ARG_IS_MANUALLY_RELOADABLE, isManuallyReloadable);
        if (!TextUtils.isEmpty(javascript)) {
            args.putString(ARG_JAVASCRIPT, javascript);
        }
        return args;
    }

    public static Fragment newInstance(@NonNull String url) {
        return newInstance(url, null);
    }

    public static Fragment newInstance(@NonNull String url, @Nullable String javascript) {
        final Fragment fragment = new AuthenticatedWebViewFragment();
        fragment.setArguments(makeArguments(url, javascript, false));
        return fragment;
    }

    public static Fragment newInstance(@NonNull String url, @Nullable String javascript, boolean isManuallyReloadable) {
        final Fragment fragment = new AuthenticatedWebViewFragment();
        fragment.setArguments(makeArguments(url, javascript, isManuallyReloadable));
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*
         * In some cases like system updating the WebView, it is not available for rendering and the
         * system raises an InflateException. To handle this case we're showing a full-screen error
         * with reload button.
         * More info on JIRA story: LEARNER-7267
         * */
        try {
            isSystemUpdatingWebView = false;
            return inflater.inflate(R.layout.fragment_authenticated_webview, container, false);
        } catch (InflateException e) {
            logger.error(e, true);
            isSystemUpdatingWebView = true;
            return inflater.inflate(R.layout.content_error, container, false);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!isSystemUpdatingWebView) {
            // Disable the SwipeRefreshLayout by-default to allow the subclasses to provide a proper implementation for it
            swipeContainer.setEnabled(false);
        } else {
            view.findViewById(R.id.content_error).setVisibility(View.VISIBLE);
            final IconImageView ivContentError = view.findViewById(R.id.content_error_icon);
            ivContentError.setIcon(FontAwesomeIcons.fa_exclamation_circle);
            tvContentError.setText(getString(R.string.error_unknown));
            btnContentErrorAction.setVisibility(View.VISIBLE);
            btnContentErrorAction.setText(R.string.lbl_reload);
            btnContentErrorAction.setOnClickListener(
                    v -> UiUtil.restartFragment(AuthenticatedWebViewFragment.this));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!isSystemUpdatingWebView) {
            if (getArguments() != null) {
                final String url = getArguments().getString(ARG_URL);
                final String javascript = getArguments().getString(ARG_JAVASCRIPT);
                final boolean isManuallyReloadable = getArguments().getBoolean(ARG_IS_MANUALLY_RELOADABLE);

                authWebView.initWebView(getActivity(), false, isManuallyReloadable);
                authWebView.loadUrlWithJavascript(true, url, javascript);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isSystemUpdatingWebView) {
            authWebView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!isSystemUpdatingWebView) {
            authWebView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!isSystemUpdatingWebView) {
            authWebView.onDestroy();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!isSystemUpdatingWebView) {
            authWebView.onDestroyView();
        }
    }

    /**
     * @return <code>true</code> if the system is updating the WebView package, <code>false</code>
     * otherwise.
     */
    public boolean isSystemUpdatingWebView() {
        return isSystemUpdatingWebView;
    }
}
