package org.edx.mobile.view.launch;

import android.support.annotation.NonNull;

import org.edx.mobile.util.Config;
import org.edx.mobile.view.ViewHoldingPresenter;

public class LaunchPresenter extends ViewHoldingPresenter<LaunchPresenter.LaunchViewInterface> {

    private final Config config;

    public LaunchPresenter(Config config) {
        this.config = config;
    }

    @Override
    public void attachView(@NonNull LaunchViewInterface view) {
        super.attachView(view);
        view.setCourseDiscoveryButton(config.isCourseDiscoveryOnLaunchEnabled());
    }

    public interface LaunchViewInterface {
        void setCourseDiscoveryButton(boolean enabled);
    }
}