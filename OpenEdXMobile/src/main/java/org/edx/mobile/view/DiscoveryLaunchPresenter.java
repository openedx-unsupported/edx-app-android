package org.edx.mobile.view;

import android.support.annotation.NonNull;

import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.Config;

public class DiscoveryLaunchPresenter extends ViewHoldingPresenter<DiscoveryLaunchPresenter.ViewInterface> {

    @NonNull
    private final LoginPrefs loginPrefs;

    @NonNull
    private final Config.EnrollmentConfig enrollmentConfig;

    public DiscoveryLaunchPresenter(@NonNull LoginPrefs loginPrefs, @NonNull Config.EnrollmentConfig enrollmentConfig) {
        this.loginPrefs = loginPrefs;
        this.enrollmentConfig = enrollmentConfig;
    }

    @Override
    public void attachView(@NonNull ViewInterface view) {
        super.attachView(view);
        view.setEnabledButtons(enrollmentConfig.isCourseDiscoveryEnabled());
    }

    public void onResume() {
        assert getView() != null;
        if (loginPrefs.getUsername() != null) {
            getView().navigateToMyCourses();
        }
    }

    public interface ViewInterface {
        void setEnabledButtons(boolean courseDiscoveryEnabled);

        void navigateToMyCourses();
    }
}
