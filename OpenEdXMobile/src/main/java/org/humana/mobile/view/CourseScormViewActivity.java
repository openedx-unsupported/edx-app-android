package org.humana.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.inject.Inject;

import org.humana.mobile.R;
import org.humana.mobile.core.IEdxEnvironment;
import org.humana.mobile.event.NetworkConnectivityChangeEvent;
import org.humana.mobile.tta.analytics.Analytic;
import org.humana.mobile.tta.scorm.JSInterfaceTincan;
import org.humana.mobile.tta.tincan.Tincan;
import org.humana.mobile.tta.tincan.model.Resume;
import org.humana.mobile.util.BrowserUtil;
import org.humana.mobile.util.Config;
import org.humana.mobile.util.IOUtils;
import org.humana.mobile.util.NetworkUtil;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

import static org.humana.mobile.util.BrowserUtil.config;
import static org.humana.mobile.util.BrowserUtil.environment;
import static org.humana.mobile.util.BrowserUtil.loginPrefs;


/**
 * Created by Dimon_GDA on 9/23/16.
 */

public class CourseScormViewActivity extends AppCompatActivity {

    private final static String FOLDER_PATH = "FOLDER_PATH";
    private final static String LAUNCHER_FILE = "index.html";//"story_html5.html";
    private final static String ANOTHER_LAUNCHER_FILE = "story.html";//"story_html5.html";
    private static String course_name;
    private static String course_id;
    private static String unit_url;

    private WebView webView;
    private ImageView fullscreen_exit;
    private File launcher;
    private Analytic analytic;
    private Tincan tincan;
    private Resume resume_info;

    private String activityUrl;
    private String resumePayload;
    Uri apkURI;

    public static Intent getLaunchIntent(Context context, String folder, String mCourse_name, String mCourse_id, String mUnit_url) {
        Intent intent = new Intent(context, CourseScormViewActivity.class);
        intent.putExtra(FOLDER_PATH, folder);
        course_name = mCourse_name;
        course_id = mCourse_id;
        unit_url = mUnit_url;
        // Log.d("Course_Url",mUnit_url);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorm_view);

        analytic = new Analytic(getApplicationContext());
        tincan = new Tincan();

        EventBus.getDefault().registerSticky(this);

        //Arjun :: for avoiding from screen from sleeping.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (savedInstanceState != null)
            ((WebView) findViewById(R.id.webview)).restoreState(savedInstanceState);

        webView = (WebView) findViewById(R.id.webView);
       /* fullscreen_exit = (ImageView) findViewById(R.id.fullscreen_exit);
        fullscreen_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });*/
        /*Config config1 = environment.getConfig();*/
        if (getIntent() != null && getIntent().getExtras() != null) {
            String folderPath = getIntent().getExtras().getString(FOLDER_PATH);

            if (folderPath != null && folderPath.length() > 0) {
                launcher = findFile(new File(folderPath), "", LAUNCHER_FILE);
                if (launcher == null) {
                    launcher = findFile(new File(folderPath), "", "story_html5.html");
                }
                if (launcher == null) {
                    launcher = findFile(new File(folderPath), "", ANOTHER_LAUNCHER_FILE);
                }
            }
        }
        resume_info = new Resume();
        //get payload for resume
        resume_info = environment.getDatabase().getResumeInfo(course_id, unit_url);

        if (resume_info == null || TextUtils.isEmpty(resume_info.getResume_Payload())) {
            //  resume_info = new Resume();
            resume_info.setResume_Payload("");
        }

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
                if (url != null && url.endsWith("html")) {
                    webView.loadUrl(url);
                    return false;
                }

                if (url != null && url.startsWith("intent://")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);

                        if (intent != null) {
                            view.stopLoading();

                            PackageManager packageManager = getPackageManager();
                            ResolveInfo info = packageManager.resolveActivity(intent,
                                    PackageManager.MATCH_DEFAULT_ONLY);
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
                            }
                            return true;
                        }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                } else {
                    Uri parsedUri = Uri.parse(url);
                    File file = new File(parsedUri.getPath());
                    // install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    Context appContext = CourseScormViewActivity.this.getApplicationContext();
                    if (url != null && !url.startsWith("https:")) {
                        apkURI = FileProvider.getUriForFile(
                                appContext,
                                appContext
                                        .getPackageName() + ".provider", file);
                    } else {
                        apkURI = Uri.parse(url);
                    }
                    Intent install = new Intent(Intent.ACTION_VIEW, apkURI);
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(install);
                    return true;

                }
                return false;
            }

        });
        // webView.loadUrl("file://"+ launcher.getAbsolutePath());

      /*  if (launcher != null){
            Log.d("BearerToken", loginPrefs.getAuthorizationHeader());
            Log.d("userName", loginPrefs.getUsername());
            Log.d("course_id",course_id);
            Log.d("course_name", course_name);
            Log.d("config", config.getAnalyticsBaseUrl());
*//*            webView.loadUrl("file://" + launcher.getAbsolutePath() + "?tincan=true&endpoint="
                    + config.getAnalyticsBaseUrl() + "&auth=" + *//**//*loginPrefs.getAuthorizationHeader()*//**//* "Bearer 638ff0d6591d183719eb28723bd37f7ac914f349" + "&actor={\"name\": [\"" + *//**//*loginPrefs.getUsername()*//**//* "Ayushikm" + "\"]," +
                    " \"mbox\": [\"mailto:example@theteacherapp.org\"], \"objectType\": [\"Agent\"]}&course_id=" + *//**//*course_id*//**//* "course-v1:TeachForIndia+BR403+2016_17" + "&source=mobile&course_name=" + *//**//*course_name*//**//* "Influences On Student Learning");*//*

            webView.loadUrl("file://" + launcher.getAbsolutePath() + "?tincan=true&endpoint="
                    + config.getAnalyticsBaseUrl() + "&auth=" + loginPrefs.getAuthorizationHeader() + "&actor={\"name\": [\"" + loginPrefs.getUsername() + "\"]," +
                    " \"mbox\": [\"mailto:example@theteacherapp.org\"], \"objectType\": [\"Agent\"]}&course_id=" + course_id + "&source=mobile&course_name=" + course_name);

        }
*/
        if (launcher != null)
            webView.loadUrl("file://" + launcher.getAbsolutePath() + "?tincan=true&endpoint="
                    + config.getAnalyticsBaseUrl() + "&auth=" + loginPrefs.getAuthorizationHeader() + "&actor={\"name\": [\"" + loginPrefs.getUsername() + "\"]," +
                    " \"mbox\": [\"mailto:example@theteacherapp.org\"], \"objectType\": [\"Agent\"]}&course_id=" + course_id + "&source=mobile&course_name=" + course_name);
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

    private WebResourceResponse getWebResourceResponse(String url) {
        boolean response = true;
        int statusCode = 200;
        String reasonPhase = "";
        Map<String, String> responseHeaders = new HashMap<String, String>();
        String mimeType = "";
        String encoding = "";
        InputStream inputStream = null;

        if (url.startsWith(config.getTincanLrsUrl() + "/activities/state?stateId=resume")) {
            if (NetworkUtil.isConnected(CourseScormViewActivity.this)) {
                response = false;
                inputStream = IOUtils.toInputStream(resume_info.getResume_Payload());
            } else {
                //get
                encoding = "UTF-8";
                mimeType = "text/html";
                statusCode = 204;
                reasonPhase = "OK";
                responseHeaders.put("Access-Control-Allow-Origin", "*");
                responseHeaders.put("X-AspNet-Version", "4.0.30319");
                responseHeaders.put("X-Powered-By", "ASP.NET");

                inputStream = IOUtils.toInputStream(resume_info.getResume_Payload());

                storeTincanPayload(url);
            }
        } else if (url.startsWith(config.getTincanLrsUrl() + "/statements?")) {
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
        } else if (url.startsWith(config.getTincanLrsUrl() + "/statements?")) {
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
            final Uri uri = Uri.parse(url);
            handleUri(uri);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return new WebResourceResponse(mimeType, encoding, statusCode, reasonPhase, responseHeaders, inputStream);
            } else
                return new WebResourceResponse(mimeType, encoding, inputStream);
        } else {
            return null;
        }
    }

    private boolean handleUri(final Uri uri) {
        Log.i("Scrom_webview", "Uri =" + uri);
        final String host = uri.getHost();
        final String scheme = uri.getScheme();
        // Based on some condition you need to determine if you are going to load the url
        // in your web view itself or in a browser.
        // You can use `host` or `scheme` or any part of the `uri` to decide.
        /*if (*//* any condition *//*) {
            // Returning false means that you are going to load this url in the webView itself
            return false;
        } else {
            // Returning true means that you need to handle what to do with the url
            // e.g. open web page in a Browser
            final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        }*/
        return true;
    }

    public void ReceiveTinCanStatement(String statement) {
        analytic.addTinCanAnalyticDB(loginPrefs.getUsername(), statement, course_id);
        syncAnalytics();
    }

    public void ReceiveTinCanResumePayload(String resume_payload) {
        resumePayload = resume_payload;
        Resume resume = new Resume();
        resume.setCourse_Id(course_id);
        resume.setUnit_id(unit_url);
        resume.setUser_Id(loginPrefs.getUsername());
        resume.setResume_Payload(resumePayload);
        tincan.addResumePayload(resume);
    }

    public void ReceiveTincanObject(String tincan_obj) {
    }

    private void storeTincanPayload(String url) {
        Resume resume = new Resume();
        resume.setCourse_Id(course_id);
        resume.setUnit_id(unit_url);
        resume.setUser_Id(loginPrefs.getUsername());
        resume.setResume_Payload(resumePayload);
        resume.setActivity_url(url);
        tincan.addResumePayload(resume);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NetworkConnectivityChangeEvent event) {
        if (NetworkUtil.isConnected(this)) {
            analytic.syncTincanResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        syncAnalytics();
    }

    private void syncAnalytics() {
        if (NetworkUtil.isConnected(this)) {
            try {
                Analytic analytic = new Analytic(this);
                analytic.syncAnalytics();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
