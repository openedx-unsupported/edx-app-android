package org.edx.mobile.tta.ui.deep_link;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import org.edx.mobile.R;
import org.edx.mobile.event.NewVersionAvailableEvent;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.deep_link.view_model.DeepLinkViewModel;
import org.edx.mobile.tta.ui.landing.LandingActivity;
import org.edx.mobile.tta.ui.logistration.SigninRegisterActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.tta.utils.BreadcrumbUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import de.greenrobot.event.EventBus;

import static org.edx.mobile.util.BrowserUtil.loginPrefs;

public class DeepLinkActivity extends BaseVMActivity {
    private static final int RANK = 0;

    public static final String EXTRA_PATH = "path";
    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_ISPUSH = "isPush";

    private  String path;
    private  String type;
    private boolean ispush=false;

    private DeepLinkViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new DeepLinkViewModel(this);
        binding(R.layout.t_activity_deep_link, viewModel);

        if (loginPrefs == null || loginPrefs.getUsername() == null || loginPrefs.getUsername().equals("")) {
            ActivityUtil.gotoPage(this, SigninRegisterActivity.class);
            this.finish();
            return;
        }

        //region get data form push notification if exist
        Intent intent = getIntent();
        Bundle push_notification_extras = intent.getExtras();
        if (push_notification_extras != null) {

            if (push_notification_extras.containsKey(EXTRA_ISPUSH)) {
                ispush = push_notification_extras.getBoolean(EXTRA_ISPUSH);
            }

            if (ispush) {
                if (push_notification_extras.containsKey(EXTRA_PATH)) {
                    path =push_notification_extras.getString(EXTRA_PATH);
                }

                if (push_notification_extras.containsKey(EXTRA_TYPE)) {
                    type = push_notification_extras.getString(EXTRA_TYPE);
                }

                if (type != null && path != null){
                    if (type.equalsIgnoreCase("course")){

                    } else if (type.equalsIgnoreCase("connect")){

                        String slug = getSlug(path);


                    } else {
                        ActivityUtil.gotoPage(this, LandingActivity.class);
                        this.finish();
                    }
                } else {
                    ActivityUtil.gotoPage(this, LandingActivity.class);
                    this.finish();
                }
            } else {
                onClickLink();
            }
        } else {
            onClickLink();
        }
        viewModel.getDataManager().getEdxEnvironment().getAnalyticsRegistry().trackScreenView(getString(R.string.label_my_courses));

    }

    private void onClickLink(){
        Intent intent = getIntent();

        if (intent.getData() == null || intent.getData().getEncodedPath() == null) {
            ActivityUtil.gotoPage(this, LandingActivity.class);
            this.finish();
        } else {
            long contentId = extractContentId(intent.getData());
            if (contentId > 0) {
                viewModel.fetchContent(contentId);
            } else if (intent.getData().getEncodedPath().split("/").length > 1 &&
                    intent.getData().getEncodedPath().split("/")[2] != null &&
                    !intent.getData().getEncodedPath().split("/")[2].equals("")) {

                String host_url;
                String connect_url;
                String edx_url;

                connect_url = viewModel.getDataManager().getConfig().getConnectUrl();
                if (!connect_url.endsWith("/"))
                    connect_url = connect_url + "/";

                edx_url = viewModel.getDataManager().getConfig().getApiHostURL();
                if (!edx_url.endsWith("/"))
                    edx_url = edx_url + "/";

                host_url = intent.getData().getScheme() + "://" + intent.getData().getHost();
                if (!host_url.endsWith("/"))
                    host_url = host_url + "/";

                if (host_url.equals(connect_url) || host_url.equals("http://www.connect.theteacherapp.org/") ||
                        host_url.equals("http://connect.theteacherapp.org/"))
                    type = "connect";
                else if (host_url.equals(edx_url) || host_url.equals("http://www.theteacherapp.org/") ||
                        host_url.equals("http://theteacherapp.org/"))
                    type = "course";
                else {
                    ActivityUtil.gotoPage(this, LandingActivity.class);
                    this.finish();
                    return;
                }

                if (type.equals("course"))
                    path = intent.getData().getEncodedPath().split("/")[2];
                else if(type.equals("connect"))
                    path = urldecode(intent.getData().getEncodedPath().split("/")[2]);

                if (path != null && !path.equals("")){
                    viewModel.fetchContent(path);
                } else {
                    ActivityUtil.gotoPage(this, LandingActivity.class);
                    this.finish();
                }

            } else {
                ActivityUtil.gotoPage(this, LandingActivity.class);
                this.finish();
            }
        }
    }

    private long extractContentId(Uri uri) {
        try {
            return Long.parseLong(uri.getQueryParameter(Constants.KEY_CONTENT_ID));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String urldecode(String encoded)
    {
        try
        {
            return URLDecoder.decode(encoded, "utf-8");
        }
        catch(UnsupportedEncodingException e)
        {
            Log.d("deeplink ",e.toString());
        }
        return null;
    }

    private String getSlug(String path)
    {
        String mslug=new String();
        //http://connect.theteacherapp.org/contests/resourcecontest2/

        String[] slugArr=path.split("/");
        mslug=slugArr[slugArr.length-1];

        return mslug;
    }

    @Override
    protected void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.appopen.name()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            /* This is the main Activity, and is where the new version availability
             * notifications are being posted. These events are posted as sticky so
             * that they can be compared against new instances of them to be posted
             * in order to determine whether it has new information content. The
             * events have an intrinsic property to mark them as consumed, in order
             * to not have to remove the sticky events (and thus lose the last
             * posted event information). Finishing this Activity should be
             * considered as closing the current session, and the notifications
             * should be reposted on a new session. Therefore, we clear the session
             * information by removing the sticky new version availability events
             * from the event bus.
             */
            EventBus.getDefault().removeStickyEvent(NewVersionAvailableEvent.class);
        }
    }
}
