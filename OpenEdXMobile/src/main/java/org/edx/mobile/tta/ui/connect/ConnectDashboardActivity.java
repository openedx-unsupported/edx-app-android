package org.edx.mobile.tta.ui.connect;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import org.edx.mobile.R;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.analytics.analytics_enums.Action;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.analytics.analytics_enums.Source;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.BasePagerAdapter;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.connect.view_model.ConnectDashboardViewModel;
import org.edx.mobile.tta.ui.custom.OnSwipeListener;
import org.edx.mobile.tta.ui.custom.VideoEnabledWebChromeClient;
import org.edx.mobile.tta.ui.custom.VideoEnabledWebView;
import org.edx.mobile.tta.ui.landing.LandingActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.tta.utils.AppUtil;
import org.edx.mobile.tta.utils.BreadcrumbUtil;
import org.edx.mobile.tta.wordpress_client.model.Post;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.PermissionsUtil;
import org.edx.mobile.view.common.PageViewStateCallback;


import static org.edx.mobile.util.BrowserUtil.config;
import static org.edx.mobile.util.BrowserUtil.loginAPI;

public class ConnectDashboardActivity extends BaseVMActivity {
    private int RANK;

    private static final int SCROLL_POSITION_TOP = 0;
    private static final int SCROLL_POSITION_MID = 1;
    private static final int SCROLL_POSITION_BOT = 2;

    private ConnectDashboardViewModel viewModel;

    private Toolbar toolbar;
    private VideoEnabledWebView webView;
    private View nonVideoLayout;
    private ViewGroup videoLayout;
    private LinearLayout pullDownLayout;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private GestureDetectorCompat detector;

    private VideoEnabledWebChromeClient webChromeClient;
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private Post currentPost;
    private Content content;
    private int scrollPosition = SCROLL_POSITION_MID;
    private boolean isPush = false;

    private OnSwipeListener onSwipeListener = new OnSwipeListener() {

        @Override
        public boolean onSwipe(Direction direction) {
            if(direction == Direction.up) {

                if (scrollPosition == SCROLL_POSITION_MID) {
                    webView.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.VISIBLE);
                    scrollPosition = SCROLL_POSITION_TOP;
                } else if (scrollPosition == SCROLL_POSITION_BOT) {
                    webView.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.VISIBLE);
                    scrollPosition = SCROLL_POSITION_MID;
                }

                return true;
            } else if (direction == Direction.down){

                if (scrollPosition == SCROLL_POSITION_TOP) {
                    webView.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.VISIBLE);
                    scrollPosition = SCROLL_POSITION_MID;
                } else if (scrollPosition == SCROLL_POSITION_MID) {
                    webView.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.GONE);
                    viewPager.setVisibility(View.GONE);
                    scrollPosition = SCROLL_POSITION_BOT;
                }

                return true;
            }

            return super.onSwipe(direction);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RANK = BreadcrumbUtil.getCurrentRank() + 1;
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.connect.name()));
        Bundle parameters = getIntent().getExtras();
        if (parameters.containsKey(Constants.KEY_IS_PUSH)){
            isPush = parameters.getBoolean(Constants.KEY_IS_PUSH);
        }
        content = parameters.getParcelable(Constants.KEY_CONTENT);
        viewModel = new ConnectDashboardViewModel(this, content);
        binding(R.layout.t_activity_connect_dashboard, viewModel);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        tabLayout.setupWithViewPager(viewPager);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nonVideoLayout = findViewById(R.id.nonVideoLayout);
        videoLayout = findViewById(R.id.videoLayout);

        webView = findViewById(R.id.connect_webview);
        setupWebview();
        viewModel.registerEventBus();

        viewModel.fetchPost(new OnResponseCallback<Post>() {
            @Override
            public void onSuccess(Post data) {
                currentPost = data;
                webView.loadUrl(currentPost.getLink());
            }

            @Override
            public void onFailure(Exception e) {
                showLongSnack(e.getLocalizedMessage());
            }
        });

        detector = new GestureDetectorCompat(this, onSwipeListener);

        /*pullDownLayout = findViewById(R.id.pull_down_layout);
        pullDownLayout.setOnTouchListener((v, event) -> {
            if (detector.onTouchEvent(event)){
                return true;
            }
            return super.onTouchEvent(event);
        });*/

        /*pullDownLayout.setOnClickListener(v -> {
            if (scrollPosition == SCROLL_POSITION_TOP) {
                webView.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.VISIBLE);
                scrollPosition = SCROLL_POSITION_MID;
            } else if (scrollPosition == SCROLL_POSITION_MID) {
                webView.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.GONE);
                viewPager.setVisibility(View.GONE);
                scrollPosition = SCROLL_POSITION_BOT;
            } else {
                webView.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.VISIBLE);
                scrollPosition = SCROLL_POSITION_MID;
            }
        });*/

        analytic.addMxAnalytics_db(
                content.getName() , Action.ViewPost, content.getSource().getName(),
                Source.Mobile, content.getSource_identity());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.connect_dashboard_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_share:
                viewModel.openShareMenu(findViewById(R.id.action_share));
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupWebview() {

        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        //setting store webpage in cache

        webView.getSettings().setAppCacheMaxSize(5 * 1024 * 1024); // 5MB
        webView.getSettings().setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        if (!NetworkUtil.isConnected(this)) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        final CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        String domain = config.getConnectDomainUrl();
        cookieManager.setCookie(domain, loginAPI.getConnectCookies() + "; Domain=" + domain);

        WebSettings webSettings = webView.getSettings();

        webSettings.setUserAgentString(webSettings.getUserAgentString() + "/" + "theteacherapp/3.0");
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new WebAppInterfaceConnect(this, webView), "android");

        webView.setOnKeyListener((v, keyCode, event) -> {

            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && webView.canGoBack()) {
                //check for internet ,otherwise connection message
                if (!NetworkUtil.isConnected(ConnectDashboardActivity.this)) {
                    webView.setVisibility(View.GONE);
                    return true;
                }
                webView.goBack();

                /*if (webView.canGoBack())
                {
                    webView.goBack();
                }
                else
                {
                    // Standard back button implementation (for example this could close the app)
                    environment.getRouter().getTeacherDashBoardActivity(ConnectNavigationActivity.this,1);
                    ConnectNavigationActivity.this.finish();
                }*/
                return true;
            }
            return false;
        });

        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, null, webView) // See all available constructors...
        {
            // Subscribe to standard events, such as onProgressChanged()...
            @Override
            public void onProgressChanged(WebView view, int progress) {
                // Your code...
            }

            /// For 3.0+ Devices (Start)
            // onActivityResult attached before constructor
            protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("video");
                startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            // For Lollipop 5.0+ Devices
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                uploadMessage = filePathCallback;

                Intent intent = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    intent = fileChooserParams.createIntent();
                }
                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e) {
                    uploadMessage = null;
                    return false;
                }
                return true;
            }

            //For Android 4.1 only
            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("video");
                startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("video");


                // video/mp4
                //video/x-msvideo
                //video/x-ms-wmv
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }


            @Override
            public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
                new AlertDialog.Builder(ConnectDashboardActivity.this)
                        /*.setTitle("Thank you !")*/
                        /*.setMessage(getResources().getString(R.string.mx_connect_uploaded_vedio_success))*/
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                (dialog, wicht) -> result.confirm()).setCancelable(false)
                        .create()
                        .show();
                return true;
            }

        };
        webChromeClient.setOnToggledFullscreen(fullscreen -> {
            // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
            if (fullscreen) {
                ConnectDashboardActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                WindowManager.LayoutParams attrs = getWindow().getAttributes();
                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                getWindow().setAttributes(attrs);
                if (Build.VERSION.SDK_INT >= 14) {
                    //noinspection all
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                }
            } else {
                ConnectDashboardActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                WindowManager.LayoutParams attrs = getWindow().getAttributes();
                attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                getWindow().setAttributes(attrs);
                if (Build.VERSION.SDK_INT >= 14) {
                    //noinspection all
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }

                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

                //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            }
        });
        webView.setWebChromeClient(webChromeClient);
        // Call private class InsideWebViewClient
        webView.setWebViewClient(new InsideWebViewClient());

        webView.addJavascriptInterface(new JavaScriptInterfaceConnectNavigation(this), "jsInterface");

    }

    public void addReplyToComment(final int parent_id) {

    }

    @Override
    protected void onPermissionGranted(String[] permissions, int requestCode) {
        switch (requestCode){
            case PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST:
                viewModel.performReadWriteOperation();
                break;
        }
    }

    @Override
    protected void onPermissionDenied(String[] permissions, int requestCode) {
        switch (requestCode){
            case PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST:
                showLongSnack("Permission Denied");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        // Notify the VideoEnabledWebChromeClient, and handle it ourselves if it doesn't handle it
        if (!webChromeClient.onBackPressed()) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else if (!isPush){
                super.onBackPressed();
            } else {
                ActivityUtil.gotoPage(this, LandingActivity.class);
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.unregisterEvnetBus();
    }

    private class InsideWebViewClient extends WebViewClient {
        @Override
        // Force links to be opened inside WebView and not in Default Browser
        // Thanks http://stackoverflow.com/a/33681975/1815624
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!NetworkUtil.isConnected(ConnectDashboardActivity.this)) {
                //noInternetConnection_tv.setVisibility(View.VISIBLE);
                showLongSnack(getString(R.string.reset_no_network_message));
                webView.setVisibility(View.GONE);
                return true;
            }
            if (url.startsWith(config.getConnectUrl())) {
                // Toast.makeText(ctx,url,Toast.LENGTH_LONG).show();
                view.loadUrl(url);
            } else if (url.startsWith("whatsapp://")) {
                if (AppUtil.appInstalledOrNot("com.whatsapp", getPackageManager())) {
                    Uri uri = Uri.parse(url);
                    String msg = uri.getQueryParameter("text");
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
                    sendIntent.setType("text/plain");
                    sendIntent.setPackage("com.whatsapp");
                    startActivity(sendIntent);
                } else {
                    showLongSnack("Whatsapp is not found in your device");
                }
                return true;
            } else if (url.endsWith(".mp4")) {
                showShortSnack("Download start.");
                //add analytics
//                analytic.addMxAnalytics_db(loginPrefs.getUsername(),currentPost.getTitle().getRendered(), Action.DownloadConnect,category_name, Source.Mobile);
                String name = "";
                if (currentPost.getSlug() == null || currentPost.getSlug().equals(""))
                    name = "download";
                else
                    name = currentPost.getSlug().toString().trim();

                Uri source = Uri.parse(url);
                // Make a new request pointing to the .mp4 url
                DownloadManager.Request request = new DownloadManager.Request(source);
                // appears the same in Notification bar while downloading
                request.setDescription("Description for the DownloadManager Bar");
                request.setTitle(name + ".mp4");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                }

                // save the file in the "Downloads" folder of SDCARD
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name + ".mp4");
                // get download service and enqueue file
                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                manager.enqueue(request);
            } else {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                } catch (ActivityNotFoundException ex) {
                    showLongSnack("Application not found in your device to perform this action");
                }
            }
            return true;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {

            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (view.getSettings().getCacheMode() == WebSettings.LOAD_NO_CACHE && request.getUrl().toString().equals(webviewUrl)) {
                    view.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
                    view.loadUrl(webviewUrl);
                    return;
                } else if (request.getUrl().toString().equals(webviewUrl)) {
                    // cache failed as well, load a local resource as last resort
                    // or inform the user
                    showErrorMessage(R.string.reset_no_network_message, FontAwesomeIcons.fa_wifi);
                }
            }*/

            super.onReceivedError(view, request, error);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            try {
                if (view.getTitle() == null || view.getTitle().equals("") || view.getTitle().equals("Web page not available") ||
                        view.getTitle().equals(currentPost.getLink().toString().split("//")[1])) {
                    // cache failed as well, load a local resource as last resort
                    // or inform the user
                    showLongSnack(getString(R.string.reset_no_network_message));
                }

                ViewGroup.LayoutParams params = webView.getLayoutParams();
                params.height = webView.getContentHeight();
                webView.setLayoutParams(params);

                ViewGroup.LayoutParams pagerParams = viewPager.getLayoutParams();
                pagerParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                viewPager.setLayoutParams(pagerParams);

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            super.onPageFinished(view, url);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.connect.name()));
        viewPager.post(() -> {
            try {
                PageViewStateCallback callback = (PageViewStateCallback) ((BasePagerAdapter) viewPager.getAdapter())
                        .getItem(viewModel.initialPosition.get());
                if (callback != null){
                    callback.onPageShow();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
