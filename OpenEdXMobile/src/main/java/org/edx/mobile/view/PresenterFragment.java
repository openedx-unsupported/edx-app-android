package org.edx.mobile.view;

import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.test.PresenterInjector;

public abstract class PresenterFragment<P extends Presenter<V>, V> extends BaseFragment {

    protected P presenter;

    @VisibleForTesting
    protected V view;

    @NonNull
    abstract protected P createPresenter();

    @NonNull
    abstract protected V createView();

    @Override
    @CallSuper
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if(getParentFragment() == null) {
            // retain instance is inherited through the fragment tree
            // so don't need it for fragments with parents
            setRetainInstance(true);
        }
        if (null == presenter) {
            if (getContext().getApplicationContext() instanceof PresenterInjector) {
                //noinspection unchecked
                presenter = (P)((PresenterInjector) getContext().getApplicationContext()).getPresenter();
            }
            if (null == presenter) {
                presenter = createPresenter();
            }
        }
        // We are having life cycle issues/crash in low memory case when this fragment recreates.
        // To fix that, its better to call super.onCreate() once the presenter is created. Ref: LEARNER-2519.
        super.onCreate(savedInstanceState);
    }

    @Override
    @CallSuper
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        view = createView();
        presenter.attachView(view);
    }

    @Override
    @CallSuper
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
        view = null;
    }

    @Override
    @CallSuper
    public void onDestroy() {
        super.onDestroy();
        presenter.destroy();
    }
}

