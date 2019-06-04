package org.edx.mobile.tta.ui.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import org.edx.mobile.tta.analytics.analytics_enums.Action;
import org.edx.mobile.tta.analytics.analytics_enums.Page;
import org.edx.mobile.tta.analytics.analytics_enums.Source;
import org.edx.mobile.tta.data.enums.SurveyType;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.landing.LandingActivity;
import org.edx.mobile.tta.ui.launch.SwipeLaunchActivity;
import org.edx.mobile.tta.ui.logistration.SigninRegisterActivity;
import org.edx.mobile.tta.ui.logistration.UserInfoActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.tta.wordpress_client.util.ConnectCookieHelper;

public class SplashViewModel extends BaseViewModel {

    private static final long DELAY = 2000;

    public SplashViewModel(BaseVMActivity activity) {
        super(activity);
        mDataManager.onAppStart();
    }

    public void startRouting(Activity activity){

        new Handler().postDelayed(() -> {
            activity.finish();
            if (mDataManager.getAppPref().isFirstLaunch()){
                ActivityUtil.gotoPage(activity, SwipeLaunchActivity.class);
                mDataManager.getAppPref().setFirstLaunch(false);
            } else {
                if (mDataManager.getLoginPrefs().getCurrentUserProfile() == null) {
                    ActivityUtil.gotoPage(activity, SigninRegisterActivity.class);
                } else {
                    performBackgroundTasks();
                    if (mDataManager.getLoginPrefs().getCurrentUserProfile().name == null ||
                            mDataManager.getLoginPrefs().getCurrentUserProfile().name.equals("") ||
                            mDataManager.getLoginPrefs().getCurrentUserProfile().name.equals(mDataManager.getLoginPrefs().getUsername())
                    ) {
                        ActivityUtil.gotoPage(mActivity, UserInfoActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    } else {
                        ActivityUtil.gotoPage(activity, LandingActivity.class);
                    }

                    mActivity.analytic.addMxAnalytics_db("TA App open", Action.AppOpen,
                            Page.LoginPage.name(), Source.Mobile, null);

                }
            }
        }, DELAY);

    }

    private void performBackgroundTasks(){
        mDataManager.setCustomFieldAttributes(null);
        ConnectCookieHelper cHelper=new ConnectCookieHelper();
        if (cHelper.isCookieExpire()) {
            mDataManager.setConnectCookies();
        }
        mDataManager.checkSurvey(mActivity, SurveyType.Login);
    }

}
