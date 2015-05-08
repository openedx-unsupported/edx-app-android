package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.edx.mobile.R;
import org.edx.mobile.model.IUnit;
import org.edx.mobile.view.common.PageViewStateCallback;

/**
 *
 */
public class CourseUnitWebviewFragment extends Fragment implements PageViewStateCallback {
    IUnit unit;

    /**
     * Create a new instance of fragment
     */
    static CourseUnitWebviewFragment newInstance(IUnit unit) {
        CourseUnitWebviewFragment f = new CourseUnitWebviewFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        f.setArguments(args);

        return f;
    }

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unit = getArguments() == null ? null :
            (IUnit) getArguments().getSerializable(Router.EXTRA_COURSE_UNIT);
    }

    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_unit_webview, container, false);
        //TODO - populate view here
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //should we recover here?
        WebView webView = (WebView)getView().findViewById(R.id.course_unit_webView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                // Handle the error
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
               return false;
            }
        });
        webView.loadUrl("http://www.google.com");
    }


    /// for PageViewStateCallback ///
    @Override
    public void onPageShow() {

    }

    @Override
    public void onPageDisappear() {

    }
}
