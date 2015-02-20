package org.edx.mobile.view;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.api.RegisterResponse;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.registration.model.RegistrationAgreement;
import org.edx.mobile.module.registration.model.RegistrationDescription;
import org.edx.mobile.module.registration.model.RegistrationFieldType;
import org.edx.mobile.module.registration.model.RegistrationFormField;
import org.edx.mobile.module.registration.view.IRegistrationFieldView;
import org.edx.mobile.task.RegisterTask;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.custom.ETextView;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends BaseFragmentActivity {

    private RelativeLayout createAccountBtn;
    private LinearLayout requiredFieldsLayout;
    private LinearLayout optionalFieldsLayout;
    private LinearLayout agreementLayout;
    private ETextView createAccountTv;
    private List<IRegistrationFieldView> mFieldViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_transition);

        //The onTick method need not be run in the RegisterActivity
        runOnTick = false;

        try{
            segIO.screenViewsTracking(ISegment.Values.LAUNCH_ACTIVITY);
        }catch(Exception e){
            logger.error(e);
        }

        createAccountBtn = (RelativeLayout) findViewById(R.id.createAccount_button_layout);
        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

        createAccountTv = (ETextView) findViewById(R.id.create_account_tv);
        requiredFieldsLayout = (LinearLayout) findViewById(R.id.required_fields_layout);
        optionalFieldsLayout = (LinearLayout) findViewById(R.id.optional_fields_layout);
        agreementLayout = (LinearLayout) findViewById(R.id.layout_agreement);
        final ETextView optional_text=(ETextView)findViewById(R.id.optional_field_tv);
        optional_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(optionalFieldsLayout.getVisibility() == View.VISIBLE) {
                    optionalFieldsLayout.setVisibility(v.GONE);
                    optional_text.setText(getString(R.string.show_optional_text));
                }
                else{
                    optionalFieldsLayout.setVisibility(v.VISIBLE);
                    optional_text.setText(getString(R.string.hide_optional_text));
                }
            }
        });

        RelativeLayout closeButtonLayout = (RelativeLayout)
                findViewById(R.id.actionbar_close_btn_layout);
        if(closeButtonLayout!=null){
            closeButtonLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        ETextView customTitle = (ETextView) findViewById(R.id.activity_title);
        if(customTitle!=null){
            customTitle.setText(getString(R.string.register_title));
        }

        AppConstants.offline_flag = !NetworkUtil.isConnected(this);

        setupRegistrationForm();
        hideSoftKeypad();
    }

    public void showAgreement(RegistrationAgreement agreement) {
        showWebDialog(agreement.getLink(),
                true,
                agreement.getText());
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom);
    }

    //Enable all the Fields(Views) which were disabled
    private void setElementsEnabled(){
        createButtonEnabled();
        for (IRegistrationFieldView v : mFieldViews) {
            v.setEnabled(true);
        }
    }

    //Disable the Fields(Views) when doing server call
    private void setElementsDisabled(){
        createButtonDisabled();
        for (IRegistrationFieldView v : mFieldViews) {
            v.setEnabled(false);
        }
    }

    //Disable the Create button during server call
    private void createButtonDisabled() {
        createAccountBtn.setBackgroundResource(R.drawable.new_bt_signin_active);
        createAccountBtn.setEnabled(false);
        createAccountTv.setText(getString(R.string.creating_account_text));
    }

    //Enable the Create button during server call
    private void createButtonEnabled() {
        createAccountBtn.setBackgroundResource(R.drawable.bt_signin_active);
        createAccountBtn.setEnabled(true);
        createAccountTv.setText(getString(R.string.create_account_text));
    }

    private void setupRegistrationForm() {
        try {
            RegistrationDescription form = new Api(this).getRegistrationDescription();

            LayoutInflater inflater = getLayoutInflater();

            List<RegistrationFormField> agreements = new ArrayList<>();

            for (RegistrationFormField field : form.getFields()) {
                if (field.getFieldType().equals(RegistrationFieldType.CHECKBOX)
                        && field.getAgreement() != null) {
                    // this is agreement field
                    // this must be added at the end of the form
                    // hold on it
                    agreements.add(field);
                }
                else {
                    IRegistrationFieldView fieldView = IRegistrationFieldView.Factory.getInstance(inflater, field);
                    if (fieldView != null) mFieldViews.add(fieldView);
                }
            }

            // add required and optional fields to the window
            for (IRegistrationFieldView v : mFieldViews) {
                if (v.getField().isRequired()) {
                    requiredFieldsLayout.addView(v.getView());
                }
                else {
                    optionalFieldsLayout.addView(v.getView());
                }
            }

            // add agreement fields to the window if available
            for (RegistrationFormField agreement : agreements) {
                IRegistrationFieldView agreementView = IRegistrationFieldView.Factory.getInstance(inflater, agreement);
                agreementView.setActionListener(new IRegistrationFieldView.IActionListener() {
                    @Override
                    public void onClickAgreement(RegistrationAgreement agreement) {
                        showAgreement(agreement);
                    }
                });
                agreementLayout.addView(agreementView.getView());
            }

            // request rendering of the layouts
            requiredFieldsLayout.requestLayout();
            optionalFieldsLayout.requestLayout();
            agreementLayout.requestLayout();

            // enable all the views
            setElementsEnabled();
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    private void createAccount() {
        if(!AppConstants.offline_flag){
            ScrollView scrollView = (ScrollView) findViewById(R.id.scrollview);

            boolean hasError = false;
            // prepare query (POST body)
            Bundle parameters = new Bundle();
            for (IRegistrationFieldView v : mFieldViews) {
                if (v.isValidInput()) {
                    if (v.hasValue()) {
                        // we submit the field only if it provides a value
                        parameters.putString(v.getField().getName(), v.getCurrentValue().getAsString());
                    }
                }
                else {
                    if (!hasError) {
                        // this is the first input field with error, so focus on it
                        scrollToView(scrollView, v.getView());
                    }
                    hasError = true;
                }
            }

            // set honor_code and terms_of_service to true
            parameters.putString("honor_code", "true");
            parameters.putString("terms_of_service", "true");

            // do NOT proceed if validations are failed
            if (hasError) {  return;  }

            setElementsDisabled();
            showProgress();

            RegisterTask task = new RegisterTask(this, parameters) {

                @Override
                public void onFinish(RegisterResponse result) {
                    if(result!=null){
                        logger.debug("registration success=" + result.isSuccess());
                        setElementsEnabled();
                        hideProgress();

                        if ( !result.isSuccess()) {
                            String errorMessage = result.getValue();
                            if(errorMessage == null || errorMessage.isEmpty()){
                                errorMessage = getString(R.string.sign_up_error);
                            }
                            sendBroadcastFlyingErrorMessage(null,errorMessage);
                        } else {
                            AuthResponse auth = getAuth();
                            if (auth != null && auth.isSuccess()) {
                                // launch my courses screen
                                Router.getInstance().showMyCourses(RegisterActivity.this);
                            } else {
                                sendBroadcastFlyingErrorMessage(null, getString(R.string.sign_up_error));
                            }
                        }
                    }else{
                        setElementsEnabled();
                        hideProgress();
                    }
                }

                @Override
                public void onException(Exception ex) {
                    logger.error(ex);
                    setElementsEnabled();
                    hideProgress();
                }
            };
            task.execute();
        }else {
            sendBroadcastFlyingErrorMessage(getString(R.string.no_connectivity),getString(R.string.network_not_connected));
        }
    }

    /**
     * Scrolls to the top of the given View in the given ScrollView.
     * @param scrollView
     * @param view
     */
    public static void scrollToView(final ScrollView scrollView, final View view) {

        // View needs a focus
        view.requestFocus();

        // Determine if scroll needs to happen
        final Rect scrollBounds = new Rect();
        scrollView.getHitRect(scrollBounds);
        if (!view.getLocalVisibleRect(scrollBounds)) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    scrollView.smoothScrollTo(0, view.getTop());
                }
            });
        }
    }

    private void showProgress() {
        View progress = findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);
        createAccountTv.setText(getString(R.string.creating_account_text));
    }

    private void hideProgress() {
        View progress = findViewById(R.id.progress);
        progress.setVisibility(View.GONE);
        createAccountTv.setText(getString(R.string.create_account_text));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Launch screen doesn't have any menu
        return true;
    }
}
