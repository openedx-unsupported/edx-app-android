package org.edx.mobile.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.view.AuthPanelUtils;
import org.edx.mobile.view.common.MessageType;
import org.edx.mobile.view.common.TaskProcessCallback;

import roboguice.inject.InjectView;

public abstract class BaseSingleFragmentActivity extends BaseFragmentActivity implements TaskProcessCallback {

    public static final String FIRST_FRAG_TAG = "first_frag";

    @InjectView(R.id.loading_indicator)
    @Nullable
    ProgressBar progressSpinner;

    @InjectView(R.id.center_message_box)
    @Nullable
    TextView centerMessageBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment_base);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowHomeEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setIcon(android.R.color.transparent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AuthPanelUtils.configureAuthPanel(findViewById(R.id.auth_panel), environment);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null){
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

    protected void showLoadingProgress(){
        if ( progressSpinner != null ){
            progressSpinner.setVisibility(View.VISIBLE);
        }
    }

    protected void hideLoadingProgress(){
        if ( progressSpinner != null ){
            progressSpinner.setVisibility(View.GONE);
        }
    }

    /**
     * implements TaskProcessCallback
     */
    public void startProcess(){
        showLoadingProgress();
    }
    /**
     * implements TaskProcessCallback
     */
    public void finishProcess(){
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

    protected void showMessageInSitu(String message){
        if ( centerMessageBox != null ){
            centerMessageBox.setVisibility( View.VISIBLE );
            centerMessageBox.setText(message);
        }
    }

    protected void hideMessageInSitu(){
        if ( centerMessageBox != null ){
            centerMessageBox.setVisibility(View.GONE);
        }
    }
}
