package org.edx.mobile.test;

import androidx.annotation.Nullable;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.view.Presenter;

public class EdxInstrumentationTestApplication extends MainApplication implements PresenterInjector {

    @Nullable
    private Presenter<?> nextPresenter = null;

    @Nullable
    @Override
    public Presenter<?> getPresenter() {
        try {
            return nextPresenter;
        } finally {
            nextPresenter = null;
        }
    }

    public void setNextPresenter(@Nullable Presenter<?> nextPresenter) {
        this.nextPresenter = nextPresenter;
    }
}
