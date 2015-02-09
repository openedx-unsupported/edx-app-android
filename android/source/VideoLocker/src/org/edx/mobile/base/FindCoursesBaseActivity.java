package org.edx.mobile.base;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import org.edx.mobile.R;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.Router;
import org.edx.mobile.view.custom.ETextView;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

public class FindCoursesBaseActivity extends BaseFragmentActivity implements URLInterceptorWebViewClient.IActionListener {

    private View offlineBar;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        offlineBar = findViewById(R.id.offline_bar);
        if (!(NetworkUtil.isConnected(this))) {
            AppConstants.offline_flag = true;
            invalidateOptionsMenu();
            showOfflineMessage();
            if(offlineBar!=null){
                offlineBar.setVisibility(View.VISIBLE);
            }
        }

        setupWebView();
    }

    private void setupWebView() {
        WebView webview = (WebView) findViewById(R.id.webview);
        URLInterceptorWebViewClient client = new URLInterceptorWebViewClient(webview);
        client.setActionListener(this);
    }

    @Override
    protected void onOnline() {
        offlineBar.setVisibility(View.GONE);
        hideOfflineMessage();
        invalidateOptionsMenu();
    }

    @Override
    protected void onOffline() {
        offlineBar.setVisibility(View.VISIBLE);
        showOfflineMessage();
        hideLoadingProgress();
        invalidateOptionsMenu();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(R.string.find_courses_title));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //Hide the download progress from Action bar
        MenuItem menuItem = menu.findItem(R.id.progress_download);
        menuItem.setVisible(false);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Hide the download progress from Action bar.
        //This has to be called in onCreateOptions as well
        MenuItem menuItem = menu.findItem(R.id.progress_download);
        menuItem.setVisible(false);
        return true;
    }

    /**
     * This function shows the offline mode message
     */
    private void showOfflineMessage(){
        ETextView offlineModeTv = (ETextView) findViewById(R.id.offline_mode_message);
        if(offlineModeTv!=null){
            offlineModeTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This function hides the offline mode message
     */
    private void hideOfflineMessage(){
        ETextView offlineModeTv = (ETextView) findViewById(R.id.offline_mode_message);
        if(offlineModeTv!=null){
            offlineModeTv.setVisibility(View.GONE);
        }
    }

    /**
     * This function shows the loading progress wheel
     * Show progress wheel while loading the web page
     */
    private void showLoadingProgress(){
        ProgressBar progressWheel = (ProgressBar) findViewById(R.id.progress_spinner);
        if(progressWheel!=null){
            progressWheel.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This function hides the loading progress wheel
     * Hide progress wheel after the web page completes loading
     */
    private void hideLoadingProgress(){
        ProgressBar progressWheel = (ProgressBar) findViewById(R.id.progress_spinner);
        if(progressWheel!=null){
            progressWheel.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClickCourseInfo(String pathId) {
        Router.getInstance().showCourseInfo(this, pathId);
    }

    @Override
    public void onClickEnroll(String courseId, boolean emailOptIn) {
        // TODO: api call and go back to "My Courses" or "My Videos"
    }
}
