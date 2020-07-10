package org.edx.mobile.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.Config;

public class DiscoveryLaunchPresenter extends ViewHoldingPresenter<DiscoveryLaunchPresenter.ViewInterface> {

    @NonNull
    private final LoginPrefs loginPrefs;

    @Nullable
    IEdxEnvironment environment;

    public DiscoveryLaunchPresenter(@NonNull LoginPrefs loginPrefs, @NonNull IEdxEnvironment environment) {
        this.loginPrefs = loginPrefs;
        this.environment = environment;
    }

    @Override
    public void attachView(@NonNull ViewInterface view) {
        super.attachView(view);
        if (environment.getConfig().getDiscoveryConfig() != null) {
            Config.CourseDiscoveryConfig courseDiscoveryConfig = environment.getConfig().getDiscoveryConfig().getCourseDiscoveryConfig();
            Config.ProgramDiscoveryConfig programDiscoveryConfig = environment.getConfig().getDiscoveryConfig().getProgramDiscoveryConfig();

            view.setEnabledButtons(courseDiscoveryConfig != null && courseDiscoveryConfig.isDiscoveryEnabled(),
                    programDiscoveryConfig != null && programDiscoveryConfig.isDiscoveryEnabled(environment));
        } else {
            view.setEnabledButtons(false, false);
        }
    }

    public void onResume() {
        assert getView() != null;
        if (loginPrefs.getUsername() != null) {
            getView().navigateToMyCourses();
        }
    }

    public interface ViewInterface {
        void setEnabledButtons(boolean courseDiscoveryEnabled, boolean programDiscoveryEnabled);

        void navigateToMyCourses();
    }
}
