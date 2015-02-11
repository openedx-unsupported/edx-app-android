package org.edx.mobile.view;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.api.RegisterResponse;
import org.edx.mobile.model.registration.RegistrationDescription;
import org.edx.mobile.model.registration.RegistrationFormField;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.task.RegisterTask;
import org.edx.mobile.view.custom.ETextView;
import org.edx.mobile.view.registration.IRegistrationFieldView;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends BaseFragmentActivity {

    private RelativeLayout createAccountBtn;
    private LinearLayout requiredFieldsLayout;
    private LinearLayout optionalFieldsLayout;
    private ETextView createAccountTv;
    private List<IRegistrationFieldView> mFieldViews = new ArrayList<>();
    private View eulaLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_transition);

        try{
            segIO.screenViewsTracking(ISegment.Values.LAUNCH_ACTIVITY);
        }catch(Exception e){
            logger.error(e);
        }

        eulaLink = findViewById(R.id.end_user_agreement_tv);

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

        ImageButton closeButton = (ImageButton) findViewById(R.id.actionbar_close_btn);
        if(closeButton!=null){
            closeButton.setOnClickListener(new View.OnClickListener() {
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

        ETextView eulaTv = (ETextView) findViewById(R.id.end_user_agreement_tv);
        eulaTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEulaDialog();
            }
        });

        setupRegistrationForm();

        hideSoftKeypad();
    }

    public void showEulaDialog() {
        showWebDialog(getString(R.string.eula_file_link), true,
                getString(R.string.end_user_title));
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom);
    }

    //Enable all the Fields(Views) which were disabled
    private void setElementsEnabled(){
        createButtonEnabled();
        eulaLink.setEnabled(true);
        for (IRegistrationFieldView v : mFieldViews) {
            v.setEnabled(true);
        }
    }

    //Disable the Fields(Views) when doing server call
    private void setElementsDisabled(){
        createButtonDisabled();
        eulaLink.setEnabled(false);
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

            for (RegistrationFormField field : form.getFields()) {
                IRegistrationFieldView fieldView = IRegistrationFieldView.Factory.getInstance(inflater, field);
                if (fieldView != null)  mFieldViews.add(fieldView);
            }

            for (IRegistrationFieldView v : mFieldViews) {
                if (v.getField().isRequired()) {
                    requiredFieldsLayout.addView(v.getView());
                }
                else {
                    optionalFieldsLayout.addView(v.getView());
                }
            }

            requiredFieldsLayout.requestLayout();
            optionalFieldsLayout.requestLayout();
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    private void createAccount() {
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollview);

        boolean hasError = false;
        // prepare query (POST body)
        Bundle parameters = new Bundle();
        for (IRegistrationFieldView v : mFieldViews) {
            if (v.isValidInput()) {
                if (v.hasValue()) {
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
                logger.debug("registration success=" + result.isSuccess());
                setElementsEnabled();
                hideProgress();

                if ( !result.isSuccess()) {
                    sendBroadcastFlyingMessage(result.getValue());
                } else {
                    AuthResponse auth = getAuth();
                    if (auth != null && auth.isSuccess()) {
                        // launch my courses screen
                        Router.getInstance().showMyCourses(RegisterActivity.this);
                    } else {
                        sendBroadcastFlyingMessage(getString(R.string.login_error));
                    }
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
}
