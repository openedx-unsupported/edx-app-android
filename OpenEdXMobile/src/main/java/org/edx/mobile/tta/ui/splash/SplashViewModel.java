package org.edx.mobile.tta.ui.splash;

import android.app.Activity;
import android.os.Handler;

import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.dashboard.DashboardActivity;
import org.edx.mobile.tta.ui.launch.SwipeLaunchActivity;
import org.edx.mobile.tta.ui.logistration.SigninRegisterActivity;
import org.edx.mobile.tta.ui.logistration.UserInfoActivity;
import org.edx.mobile.tta.utils.ActivityUtil;

public class SplashViewModel extends BaseViewModel {

    private static final long DELAY = 2000;

    public SplashViewModel(BaseVMActivity activity) {
        super(activity);
        startRouting(activity);
    }

    private void startRouting(Activity activity){


        new Handler().postDelayed(() -> {
            activity.finish();
            if (mDataManager.getAppPref().isFirstLaunch()){
                ActivityUtil.gotoPage(activity, SwipeLaunchActivity.class);
                mDataManager.getAppPref().setFirstLaunch(false);
            } else {
                if (mDataManager.getLoginPrefs().getCurrentUserProfile() == null) {
                    ActivityUtil.gotoPage(activity, SigninRegisterActivity.class);
                } else {
                    ActivityUtil.gotoPage(activity, DashboardActivity.class);
                }
            }
        }, DELAY);

    }

}
