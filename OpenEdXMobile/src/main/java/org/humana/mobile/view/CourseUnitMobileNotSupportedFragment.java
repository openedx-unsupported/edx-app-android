package org.humana.mobile.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import org.humana.mobile.R;
import org.humana.mobile.model.course.BlockType;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.services.ViewPagerDownloadManager;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.data.DataManager;
import org.humana.mobile.util.BrowserUtil;
import org.humana.mobile.view.custom.AuthenticatedWebView;
import org.humana.mobile.view.custom.URLInterceptorWebViewClient;

/**
 *
 */
public class CourseUnitMobileNotSupportedFragment extends CourseUnitFragment {

    /**
     * Create a new instance of fragment
     */
    private DataManager mdataManager;
    private final String lms_xblock = "/mx_humana_lms/ora/";

    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;


    public static CourseUnitMobileNotSupportedFragment newInstance(CourseComponent unit) {
        CourseUnitMobileNotSupportedFragment f = new CourseUnitMobileNotSupportedFragment();

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
    }

    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_unit_grade, container, false);
        AuthenticatedWebView webView = v.findViewById(R.id.auth_webview);

        ((TextView) v.findViewById(R.id.not_available_message)).setText(
                unit.getType() == BlockType.VIDEO ? R.string.video_only_on_web_short : R.string.assessment_not_available);
        v.findViewById(R.id.view_on_web_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BrowserUtil.open(getActivity(), unit.getWebUrl());
                environment.getSegment().trackOpenInBrowser(unit.getId()
                        , unit.getCourseId(), unit.isMultiDevice());
            }
        });
        mdataManager = DataManager.getInstance(getActivity());
        webView.initWebView(getActivity(), true, false);

        //webView.getSettings().setUseWideViewPort(true);

        //Other webview settings
        webView.getWebViewClient().setPageStatusListener(new URLInterceptorWebViewClient.IPageStatusListener() {
            @Override
            public void onPageStarted() {
            }


            @Override
            public void onPageFinished() {
                ViewPagerDownloadManager.instance.done(CourseUnitMobileNotSupportedFragment.this, true);
            }

            @Override
            public void onPageLoadError(WebView view, int errorCode, String description, String failingUrl) {
                if (failingUrl != null && failingUrl.equals(view.getUrl())) {
                    ViewPagerDownloadManager.instance.done(CourseUnitMobileNotSupportedFragment.this, false);
                }
            }

            @Override
            public void onPageLoadError(WebView view, WebResourceRequest request,
                                        WebResourceResponse errorResponse, boolean isMainRequestFailure) {
                if (isMainRequestFailure) {
                    ViewPagerDownloadManager.instance.done(CourseUnitMobileNotSupportedFragment.this,
                            false);
                }
            }

            @Override
            public void onPageLoadProgressChanged(WebView webView, int progress) {

            }

            @Override
            public void openFile(WebView webView, ValueCallback<Uri[]> uploadMsg, WebChromeClient.FileChooserParams fileChooserParams) {
                webView.getSettings().setAllowFileAccess(true);
                Log.d("webView", "setWebChromeClient");

                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                uploadMessage = uploadMsg;

                Intent intent = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    intent = fileChooserParams.createIntent();
                }
                try
                {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e)
                {
                    uploadMessage = null;
                }

            }

        });




        String blockId = unit.getId();

        String urlToloadStudent = environment.getConfig().getApiHostURL() + lms_xblock
                + blockId + "/"+Constants.USERNAME;
        if (!Constants.USERNAME.equals("")) {
            webView.loadUrl(false, urlToloadStudent);
            Log.d("LoadedURL", urlToloadStudent);
        } else {
            webView.loadUrl(false, unit.getBlockUrl());
            Constants.USERNAME = "";
        }




        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (ViewPagerDownloadManager.instance.inInitialPhase(unit))
            ViewPagerDownloadManager.instance.addTask(this);
    }


    @Override
    public void run() {
        ViewPagerDownloadManager.instance.done(this, true);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if (requestCode == REQUEST_SELECT_FILE)
            {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        }
        else if (requestCode == FILECHOOSER_RESULTCODE)
        {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != getActivity().RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
        else
            Toast.makeText(getActivity().getApplicationContext(), "Failed to Upload IMAGE", Toast.LENGTH_LONG).show();
    }
}
