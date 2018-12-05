package org.edx.mobile.tta.ui.splash;

import android.app.Activity;
import android.os.Handler;
import android.widget.Toast;

import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.login.SigninRegisterActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.view.MainDashboardActivity;

public class SplashViewModel extends BaseViewModel {

    private static final long DELAY = 2000;

    public SplashViewModel(BaseVMActivity activity) {
        super(activity);
        startRouting(activity);
    }

    private void startRouting(Activity activity){


        new Handler().postDelayed(() -> {
            if (mDataManager.getAppPref().isFirstLaunch()){
                Toast.makeText(activity, "First launch", Toast.LENGTH_SHORT).show();
                mDataManager.getAppPref().setFirstLaunch(false);
            } else {
                activity.finish();
                if (mDataManager.getLoginPrefs().getCurrentUserProfile() == null) {
                    ActivityUtil.gotoPage(activity, SigninRegisterActivity.class);
                } else {
                    Toast.makeText(activity, mDataManager.getLoginPrefs().getUsername(), Toast.LENGTH_SHORT).show();
                    ActivityUtil.gotoPage(activity, MainDashboardActivity.class);
                }
            }
        }, DELAY);

    }

}
