package org.edx.mobile.test;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.edx.mobile.view.Presenter;

@VisibleForTesting
public interface PresenterInjector {
    @Nullable
    Presenter<?> getPresenter();
}
