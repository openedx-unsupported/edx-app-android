package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.data.local.db.ILocalDataSource;
import org.edx.mobile.tta.data.local.db.table.Program;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.landing.LandingActivity;
import org.edx.mobile.tta.ui.launch.SwipeLaunchActivity;
import org.edx.mobile.tta.ui.programs.selectprogram.SelectProgramActivity;
import org.edx.mobile.tta.ui.splash.SplashViewModel;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.tta.utils.BreadcrumbUtil;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.NetworkUtil;
import org.json.JSONObject;

import java.util.List;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import static org.edx.mobile.util.BrowserUtil.appPref;

// We are extending the normal Activity class here so that we can use Theme.NoDisplay, which does not support AppCompat activities
public class SplashActivity extends Activity {

    private static final long DELAY = 1500;

    protected final Logger logger = new Logger(getClass().getName());
    private Config config = new Config(MainApplication.instance());

    @Inject
    ILocalDataSource dgd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (!Config.FabricBranchConfig.isBranchEnabled(config.getFabricConfig())) {
            finish();
        }*/

        /*
        Recommended solution to avoid opening of multiple tasks of our app's launcher activity.
        For more info:
        - https://issuetracker.google.com/issues/36907463
        - https://stackoverflow.com/questions/4341600/how-to-prevent-multiple-instances-of-an-activity-when-it-is-launched-with-differ/
        - https://stackoverflow.com/questions/16283079/re-launch-of-activity-on-home-button-but-only-the-first-time/16447508#16447508
         */


        startRouting();
    }

    private void startRouting(){

        new Handler().postDelayed(() -> {
            finish();

            if (!isTaskRoot()) {
                final Intent intent = getIntent();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(intent.getAction())) {
                    return;
                }
            }

            final IEdxEnvironment environment = MainApplication.getEnvironment(this);
 /*       if (environment.getUserPrefs().getProfile() != null) {
            //environment.getRouter().showMainDashboard(SplashActivity.this);
            ActivityUtil.gotoPage(SplashActivity.this, LandingActivity.class);
        } else if (!environment.getConfig().isRegistrationEnabled()) {
            startActivity(environment.getRouter().getLogInIntent());
        } else {
            environment.getRouter().showLaunchScreen(SplashActivity.this);
        }*/


            if(appPref.isFirstLaunch()) {
                ActivityUtil.gotoPage(SplashActivity.this, SwipeLaunchActivity.class);
                appPref.setFirstLaunch(false);
                return;
            }

            if (environment.getUserPrefs().getProfile() != null) {
                //environment.getRouter().showMainDashboard(SplashActivity.this);
                ActivityUtil.gotoPage(SplashActivity.this, SelectProgramActivity.class);
            }
            else {
                environment.getRouter().showLaunchScreen(SplashActivity.this);
            }

        }, DELAY);

    }

    @Override
    public void onStart() {
        super.onStart();
        /*if (Config.FabricBranchConfig.isBranchEnabled(config.getFabricConfig())) {
            Branch.getInstance().initSession(new Branch.BranchReferralInitListener() {
                @Override
                public void onInitFinished(JSONObject referringParams, BranchError error) {
                    if (error == null) {
                        // params are the deep linked params associated with the link that the user
                        // clicked -> was re-directed to this app params will be empty if no data found
                    } else {
                        // Ignore the logging of errors occurred due to lack of network connectivity
                        if (NetworkUtil.isConnected(getApplicationContext())) {
                            logger.error(new Exception("Branch not configured properly, error:\n"
                                    + error.getMessage()), true);
                        }
                    }
                }
            }, this.getIntent().getData(), this);

            finish();
        }*/
    }

//    @Override
//    public void onNewIntent(Intent intent) {
//        this.setIntent(intent);
//    }
}
