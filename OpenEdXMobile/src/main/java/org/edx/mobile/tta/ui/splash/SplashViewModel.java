package org.edx.mobile.tta.ui.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.tta.data.constants.Constants;
import org.edx.mobile.tta.data.enums.SurveyType;
import org.edx.mobile.tta.data.local.db.table.Program;
import org.edx.mobile.tta.data.local.db.table.Section;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.landing.LandingActivity;
import org.edx.mobile.tta.ui.launch.SwipeLaunchActivity;
import org.edx.mobile.tta.ui.programs.selectSection.SelectSectionActivity;
import org.edx.mobile.tta.ui.programs.selectprogram.SelectProgramActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.tta.wordpress_client.util.ConnectCookieHelper;

import java.util.List;

import static org.edx.mobile.util.BrowserUtil.appPref;

public class SplashViewModel extends BaseViewModel {

    private static final long DELAY = 2000;

    public SplashViewModel(BaseVMActivity activity) {
        super(activity);
        mDataManager.onAppStart();
    }

    public void startRouting(Activity activity) {

        new Handler().postDelayed(() -> {
//            activity.finish();
//            if (mDataManager.getAppPref().isFirstLaunch()){
//                ActivityUtil.gotoPage(activity, SwipeLaunchActivity.class);
//                mDataManager.getAppPref().setFirstLaunch(false);
//            } else {
//                if (mDataManager.getLoginPrefs().getCurrentUserProfile() == null) {
//                    ActivityUtil.gotoPage(activity, SigninRegisterActivity.class);
//                } else {
//                    performBackgroundTasks();
//                   /* if (mDataManager.getLoginPrefs().getCurrentUserProfile().name == null ||
//                            mDataManager.getLoginPrefs().getCurrentUserProfile().name.equals("") ||
//                            mDataManager.getLoginPrefs().getCurrentUserProfile().name.equals(mDataManager.getLoginPrefs().getUsername())
//                    ) {
//                        ActivityUtil.gotoPage(mActivity, UserInfoActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    } else {
//                        ActivityUtil.gotoPage(activity, LandingActivity.class);
//                    }*/
//
//                    ActivityUtil.gotoPage(activity, LandingActivity.class);
//
//                    mActivity.analytic.addMxAnalytics_db("TA App open", Action.AppOpen,
//                            Page.LoginPage.name(), Source.Mobile, null);
//
//                }
//            }

            if (!activity.isTaskRoot()) {
                final Intent intent = mActivity.getIntent();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(intent.getAction())) {
                    return;
                }
            }

            final IEdxEnvironment environment = MainApplication.getEnvironment(mActivity);
 /*       if (environment.getUserPrefs().getProfile() != null) {
            //environment.getRouter().showMainDashboard(SplashActivity.this);
            ActivityUtil.gotoPage(SplashActivity.this, LandingActivity.class);
        } else if (!environment.getConfig().isRegistrationEnabled()) {
            startActivity(environment.getRouter().getLogInIntent());
        } else {
            environment.getRouter().showLaunchScreen(SplashActivity.this);
        }*/


            if (appPref.isFirstLaunch()) {
                ActivityUtil.gotoPage(mActivity, SwipeLaunchActivity.class);
                appPref.setFirstLaunch(false);
                return;
            }

            if (environment.getUserPrefs().getProfile() != null) {
                //environment.getRouter().showMainDashboard(SplashActivity.this);
                mDataManager.getPrograms(new OnResponseCallback<List<Program>>() {
                    @Override
                    public void onSuccess(List<Program> data) {
                        if (data.size() == 0){
                            ActivityUtil.gotoPage(mActivity, LandingActivity.class);
                            mActivity.finish();
                        }
                        if (data.size() == 1) {
                            mDataManager.getLoginPrefs().setProgramId(data.get(0).getId());
                            mDataManager.getLoginPrefs().setProgramTitle(data.get(0).getTitle());
                            Constants.isSinglePrg = true;
                            getSection();
                        } else {
                            ActivityUtil.gotoPage(mActivity, SelectProgramActivity.class,
                                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        }

                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        mDataManager.getLoginPrefs().setProgramId("");
                        ActivityUtil.gotoPage(mActivity, LandingActivity.class,
                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    }
                });
            } else {
                environment.getRouter().showLaunchScreen(mActivity);
            }
        }, DELAY);

    }

    private void getSection() {
        mDataManager.getSections(mDataManager.getLoginPrefs().getProgramId(), new OnResponseCallback<List<Section>>() {
            @Override
            public void onSuccess(List<Section> data) {
                if (data.size() == 0){
                    ActivityUtil.gotoPage(mActivity, LandingActivity.class);
                    mActivity.finish();
                }
                else if (data.size() == 1) {
                    mDataManager.getLoginPrefs().setSectionId(data.get(0).getId());
                    mDataManager.getLoginPrefs().setRole(data.get(0).getRole());

                    ActivityUtil.gotoPage(mActivity, LandingActivity.class);
                    Constants.isSingleRow = true;
                    mActivity.finish();
                } else {
                    ActivityUtil.gotoPage(mActivity, SelectSectionActivity.class);
                    mActivity.finish();
                }
//                for (Section unit: data){
//                    if (!selectedSections.contains(unit)){
//                        selectedSections.add(unit);
//                    }
//                }
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                mDataManager.getLoginPrefs().setSectionId("");
                ActivityUtil.gotoPage(mActivity, SelectSectionActivity.class);
                mActivity.finish();
            }
        });
    }

    private void performBackgroundTasks() {
        mDataManager.setCustomFieldAttributes(null);
        ConnectCookieHelper cHelper = new ConnectCookieHelper();
        if (cHelper.isCookieExpire()) {
            mDataManager.setConnectCookies();
        }
        mDataManager.checkSurvey(mActivity, SurveyType.Login);
    }

}
