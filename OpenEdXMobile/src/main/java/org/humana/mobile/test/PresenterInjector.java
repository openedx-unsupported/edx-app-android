package org.humana.mobile.test;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import org.humana.mobile.view.Presenter;

@VisibleForTesting
public interface PresenterInjector {
    @Nullable
    Presenter<?> getPresenter();
}
