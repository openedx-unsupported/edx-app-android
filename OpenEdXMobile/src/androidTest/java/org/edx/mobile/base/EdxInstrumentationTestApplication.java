package org.edx.mobile.base;

import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import org.edx.mobile.test.PresenterInjector;
import org.edx.mobile.view.Presenter;

public class EdxInstrumentationTestApplication extends MultiDexApplication implements PresenterInjector {

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
