package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.course.HtmlBlockModel;
import org.edx.mobile.view.custom.AuthenticatedWebView;

import roboguice.inject.InjectView;

public class CourseUnitWebViewFragment extends CourseUnitFragment {
    protected final Logger logger = new Logger(getClass().getName());

    @InjectView(R.id.auth_webview)
    private AuthenticatedWebView authWebView;

    @InjectView(R.id.swipe_container)
    protected SwipeRefreshLayout swipeContainer;

    public static CourseUnitWebViewFragment newInstance(HtmlBlockModel unit) {
        CourseUnitWebViewFragment fragment = new CourseUnitWebViewFragment();
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_authenticated_webview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeContainer.setEnabled(false);
        authWebView.initWebView(getActivity(), true, false);
        authWebView.loadUrl(true, unit.getBlockUrl());
    }

    /*@Override
    public void onResume() {
        super.onResume();
        authWebView.onResume();
        if (hasComponentCallback != null) {
            final CourseComponent component = hasComponentCallback.getComponent();
            if (component != null && component.equals(unit)) {
                authWebView.loadUrl(false, unit.getBlockUrl());
            }
        }
    }*/

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
