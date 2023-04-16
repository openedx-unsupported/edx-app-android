package org.edx.mobile.view;

import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.http.notifications.SnackbarErrorNotification;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.interfaces.SnackbarStatusListener;
import org.edx.mobile.util.NetworkUtil;
import org.greenrobot.eventbus.EventBus;

/**
 * Provides support for offline mode handling within an Activity.
 * <br/>
 * Ensures that no two types of errors appear at the same time in an Activity e.g. if
 * {@link FullScreenErrorNotification} is already appearing in an activity
 * {@link SnackbarErrorNotification} should never appear until and unless the
 * {@link FullScreenErrorNotification} is hidden.
 */
public abstract class OfflineSupportBaseActivity<VB extends ViewDataBinding> extends BaseFragmentActivity
        implements SnackbarStatusListener, RefreshListener {

    protected VB binding;

    private SnackbarErrorNotification snackbarErrorNotification;
    private boolean isFullScreenErrorVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, getViewResourceId());
        snackbarErrorNotification = new SnackbarErrorNotification(binding.getRoot());
    }

    @Override
    public void hideSnackBar() {
        snackbarErrorNotification.hideError();
    }

    @Override
    public void resetSnackbarVisibility(boolean fullScreenErrorVisibility) {
        this.isFullScreenErrorVisible = fullScreenErrorVisibility;
        final boolean isNetworkConnected = NetworkUtil.isConnected(this);
        if (fullScreenErrorVisibility || isNetworkConnected) {
            snackbarErrorNotification.hideError();
        } else {
            snackbarErrorNotification.showOfflineError(this);
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        if (NetworkUtil.isConnected(this)) {
            snackbarErrorNotification.hideError();
        } else if (!isFullScreenErrorVisible) {
            snackbarErrorNotification.showOfflineError(this);
        }
    }

    @Override
    public void onRefresh() {
        EventBus.getDefault().post(getRefreshEvent());
    }

    /**
     * Provides an object that we will be needing to fire on {@link EventBus} whenever some screen state
     * needs to be refreshed.
     *
     * @return The event object.
     */
    public abstract Object getRefreshEvent();

    /**
     * Provides the layout resource id of the layout to be used by inherited activity.
     *
     * @return The event object.
     */
    public abstract @LayoutRes
    int getViewResourceId();
}
