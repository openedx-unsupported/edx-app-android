package org.edx.mobile.test;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import org.edx.mobile.view.Presenter;

@VisibleForTesting
public interface PresenterInjector {
    @Nullable
    Presenter<?> getPresenter();
}
