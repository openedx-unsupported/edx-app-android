package org.edx.mobile.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.edx.mobile.R;
import org.edx.mobile.databinding.ActivitySingleFragmentBaseBinding;
import org.edx.mobile.view.AuthPanelUtils;
import org.edx.mobile.view.common.MessageType;
import org.edx.mobile.view.common.TaskProcessCallback;

public abstract class BaseSingleFragmentActivity extends BaseFragmentActivity implements TaskProcessCallback {

    public static final String FIRST_FRAG_TAG = "first_frag";
    private ActivitySingleFragmentBaseBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySingleFragmentBaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        addToolbar();
        super.setToolbarAsActionBar();
    }

    /**
     * It will add the custom toolbar in the activity's layout.
     * <p>
     * Toolbar addition will be done by finding a placeholder view with id
     * {@link R.id#toolbar_placeholder R.id.toolbar_placeholder} and then replacing it with an
     * inflated custom toolbar layout. Custom toolbar layout will be obtained from
     * {@link BaseSingleFragmentActivity#getToolbarLayoutId()} function.
     * </p>
     */
    private void addToolbar() {
        final ViewGroup parent = (ViewGroup) binding.toolbarPlaceholder.getParent();
        final int index = parent.indexOfChild(binding.toolbarPlaceholder);
        parent.removeView(binding.toolbarPlaceholder);
        int toolbarLayoutId = getToolbarLayoutId();
        if (toolbarLayoutId >= 0) {
            final View toolbar = getLayoutInflater().inflate(toolbarLayoutId, parent, false);
            parent.addView(toolbar, index);
        }
    }

    @LayoutRes
    protected int getToolbarLayoutId() {
        return R.layout.toolbar;
    }

    @Override
    protected void onResume() {
        super.onResume();
        AuthPanelUtils.configureAuthPanel(findViewById(R.id.auth_panel), environment);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null) {
            try {
                this.loadFirstFragment();
            } catch (Exception e) {
                logger.error(e);
            }
        }

    }

    private void loadFirstFragment() throws Exception {
        Fragment singleFragment = getFirstFragment();

        //this activity will only ever hold this lone fragment, so we
        // can afford to retain the instance during activity recreation
        singleFragment.setRetainInstance(true);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.my_groups_list_container, singleFragment, FIRST_FRAG_TAG);
        fragmentTransaction.disallowAddToBackStack();
        fragmentTransaction.commit();
    }

    public abstract Fragment getFirstFragment();

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final BaseFragment baseFragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(FIRST_FRAG_TAG);
        if (baseFragment != null) {
            baseFragment.onNewIntent(intent);
        }
    }

    protected void showLoadingProgress() {
        binding.loadingIndicator.loadingIndicator.setVisibility(View.VISIBLE);
    }

    protected void hideLoadingProgress() {
        binding.loadingIndicator.loadingIndicator.setVisibility(View.GONE);
    }

    /**
     * implements TaskProcessCallback
     */
    public void startProcess() {
        showLoadingProgress();
    }

    /**
     * implements TaskProcessCallback
     */
    public void finishProcess() {
        hideLoadingProgress();
    }

    public void onMessage(@NonNull MessageType messageType, @NonNull String message) {
        //TODO - -we need to define different UI message view for different message type?
        switch (messageType) {
            case FLYIN_ERROR:
                this.showErrorMessage("", message);
                break;
            case FLYIN_WARNING:
            case FLYIN_INFO:
                this.showInfoMessage(message);
                break;
            case ERROR:
            case WARNING:
            case INFO:
                this.showMessageInSitu(message);
                break;
            case EMPTY:
                this.hideMessageInSitu();
                break;
            case DIALOG:
                this.showAlertDialog(null, message);
        }
    }

    protected void showMessageInSitu(String message) {
        binding.centerMessageBox.setVisibility(View.VISIBLE);
        binding.centerMessageBox.setText(message);
    }

    protected void hideMessageInSitu() {
        binding.centerMessageBox.setVisibility(View.GONE);
    }

    public ActivitySingleFragmentBaseBinding getBinding() {
        return binding;
    }
}
