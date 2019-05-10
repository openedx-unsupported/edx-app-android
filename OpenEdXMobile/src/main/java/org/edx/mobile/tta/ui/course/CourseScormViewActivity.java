package org.edx.mobile.tta.ui.course;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.edx.mobile.R;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.scorm.JSInterfaceTincan;
import org.edx.mobile.tta.tincan.Tincan;
import org.edx.mobile.tta.tincan.model.Resume;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.course.view_model.CourseScormViewModel;
import org.edx.mobile.util.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class CourseScormViewActivity extends BaseVMActivity {

    private final static String LAUNCHER_FILE = "index.html";//"story_html5.html";

    private CourseScormViewModel viewModel;

    private WebView webView;

    private File launcher;
    private String folderPath;
    private String courseName;
    private String courseId;
    private String unitId;

    private Tincan tincan;
    private Resume resume_info;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // lock the current device orientation
        int currentOrientation = this.getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else{
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        viewModel = new CourseScormViewModel(this);
        binding(R.layout.t_activity_course_scorm_view, viewModel);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tincan=new Tincan();

        if (getIntent().getExtras() != null) {
            getDataFromParameters(getIntent().getExtras());
        }

        webView = (WebView) findViewById(R.id.webView);

        if (folderPath != null && folderPath.length() > 0) {
            launcher = findFile(new File(folderPath), "", LAUNCHER_FILE);

            if(launcher==null)
                launcher=findFile(new File(folderPath), "", "story_html5.html");
        }

        //get payload for resume
        resume_info=new Resume();
        resume_info=viewModel.getDataManager().getEdxEnvironment().getDatabase().getResumeInfo(courseId, unitId);

        if(resume_info==null || TextUtils.isEmpty(resume_info.getResume_Payload()))
            resume_info.setResume_Payload("");

        setupWebview();
    }

    private void getDataFromParameters(Bundle parameters){
        folderPath = parameters.getString(Constants.KEY_FILE_PATH);
        courseName = parameters.getString(Constants.KEY_COURSE_NAME);
        courseId = parameters.getString(Constants.KEY_COURSE_ID);
        unitId = parameters.getString(Constants.KEY_UNIT_ID);
    }

    private File findFile(File aFile, String sDir, String toFind) {
        if (aFile.isFile() &&
                aFile.getAbsolutePath().contains(sDir) &&
                aFile.getName().contains(toFind)) {
            return aFile;
        } else if (aFile.isDirectory()) {
            for (File child : aFile.listFiles()) {
                File found = findFile(child, sDir, toFind);
                if (found != null) {
                    return found;
                }//if
            }//for
        }//else
        return null;
    }

    private void setupWebview() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.addJavascriptInterface(new JSInterfaceTincan(CourseScormViewActivity.this), "jsInterfaceTincan");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            webView.getSettings().setDatabasePath("/data/data/" + webView.getContext().getPackageName() + "/databases/");
        }

        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onLoadResource(WebView view, String url) {

                super.onLoadResource(view, url);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

                if (getWebResourceResponse(url) != null) {
                    return getWebResourceResponse(url);
                } else
                    return super.shouldInterceptRequest(view, url);
            }

            //url overloading
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.endsWith("html")) {
                    webView.loadUrl(url);
                    return true;
                }

                if (url.startsWith("intent://")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);

                        if (intent != null) {
                            view.stopLoading();

                            PackageManager packageManager = getPackageManager();
                            ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                            if (info != null) {
                                startActivity(intent);
                            } else {
                                String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                                view.loadUrl(fallbackUrl);

                                Toast.makeText(CourseScormViewActivity.this, "Install player to watch this course", Toast.LENGTH_SHORT).show();

                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + intent.getPackage())));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + intent.getPackage())));
                                }

                                // or call external browser
//                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
//                    context.startActivity(browserIntent);
                            }
                            return true;
                        }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

        if (launcher != null) {
            String url = "file://" + launcher.getAbsolutePath() +
                            "?tincan=true&endpoint=" + viewModel.getDataManager().getConfig().getTincanLrsUrl() +
                            "&auth=" + viewModel.getDataManager().getLoginPrefs().getAuthorizationHeader() +
                            "&actor={\"name\": [" + viewModel.getDataManager().getLoginPrefs().getUsername() +
                            "], \"mbox\": [\"mailto:example@theteacherapp.org\"], \"objectType\": [\"Agent\"]}&course_id=" + courseName +
                            "&source=mobile";
            webView.loadUrl(url);
        }
    }

    private WebResourceResponse getWebResourceResponse(String url) {
        boolean response = true;
        int statusCode = 200;
        String reasonPhase = "";
        Map<String, String> responseHeaders = new HashMap<String, String>();
        String mimeType = "";
        String encoding = "";
        InputStream inputStream = null;

        if (url.startsWith(viewModel.getDataManager().getConfig().getTincanLrsUrl() + "/activities/state?stateId=resume")) {
            //get
            encoding = "UTF-8";
            mimeType = "text/html";
            statusCode = 204;
            reasonPhase = "OK";
            responseHeaders.put("Access-Control-Allow-Header", "Content-Type,Content-Length,Authorization,If-Match,If-None-Match,X-Experience-API-Version,X-Experience-API-Consistent-Through");
            responseHeaders.put("Access-Control-Expose-Header", "ETag,Last-Modified,Cache-Control,Content-Type,Content-Length,WWW-Authenticate,X-Experience-API-Version,X-Experience-API-Consistent-Through");
            responseHeaders.put("Cache-Control", "no-cache");
            responseHeaders.put("X-Experience-API-Version", "1.0.0");
            responseHeaders.put("access-control-allow-methods", "HEAD,GET,POST,PUT,DELETE");

            responseHeaders.put("Content-Type", "application/json");
            responseHeaders.put("Connection", "keep-alive");
            responseHeaders.put("Access-Control-Allow-Origin", "*");
            responseHeaders.put("X-AspNet-Version", "4.0.30319");
            responseHeaders.put("X-Powered-By", "ASP.NET");

            inputStream = IOUtils.toInputStream(resume_info.getResume_Payload());
        } else if (url.startsWith(viewModel.getDataManager().getConfig().getTincanLrsUrl() + "/statements?")) {
            //option
            encoding = "UTF-8";
            mimeType = "text/html";
            statusCode = 200;
            reasonPhase = "OK";
            responseHeaders.put("Access-Control-Allow-Origin", "*");
            responseHeaders.put("Access-Control-Allow-Methods", "GET,PUT,POST");
            responseHeaders.put("Access-Control-Allow-Headers", "authorization, content-type, x-experience-api-version");
            responseHeaders.put("Access-Control-Max-Age", "1728000");
            responseHeaders.put("X-Powered-By:", "ASP.NET");
        } else if (url.startsWith(viewModel.getDataManager().getConfig().getTincanLrsUrl() + "/statements?")) {
            //option
            encoding = "UTF-8";
            mimeType = "text/html";
            statusCode = 204;
            reasonPhase = "OK";
            responseHeaders.put("Access-Control-Allow-Origin", "*");
            responseHeaders.put("Cache-Control", "no-cache");
            responseHeaders.put("Expires", "-1");
            responseHeaders.put("X-AspNet-Version", "4.0.30319");
            responseHeaders.put("Server", "Microsoft-IIS/8.5");
            responseHeaders.put("X-Powered-By:", "ASP.NET");
        } else {
            response = false;
        }

        if (response) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return new WebResourceResponse(mimeType, encoding, statusCode, reasonPhase, responseHeaders, inputStream);
            } else
                return new WebResourceResponse(mimeType, encoding, inputStream);
        } else {
            return null;
        }
    }

    public void ReceiveTinCanStatement(String statement) {
        analytic.addTinCanAnalyticDB(statement, courseName);
        // Toast.makeText(CourseScormViewActivity.this, statement, Toast.LENGTH_LONG).show();
    }

    public void ReceiveTinCanResumePayload(String resume_payload) {
        Resume resume=new Resume();
        resume.setCourse_Id(courseId);
        resume.setUnit_id(unitId);
        resume.setUser_Id(viewModel.getDataManager().getLoginPrefs().getUsername());
        resume.setResume_Payload(resume_payload);
        tincan.addResumePayload(resume);
        //Toast.makeText(CourseScormViewActivity.this, resume_payload, Toast.LENGTH_LONG).show();
    }

    public void ReceiveTincanObject(String tincan_obj) {
        //Toast.makeText(CourseScormViewActivity.this, tincan_obj, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            Class.forName("android.webkit.WebView").getMethod("onPause", (Class[]) null).invoke(webView, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        webView.setLayoutParams(new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        webView.loadUrl("javascript:window.dispatchEvent(new Event('resize'));");
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Class.forName("android.webkit.WebView").getMethod("onResume", (Class[]) null).invoke(webView, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }
}
