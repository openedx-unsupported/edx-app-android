package org.edx.mobile.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.model.api.RegisterResponse;
import org.edx.mobile.model.registration.RegistrationFieldType;
import org.edx.mobile.model.registration.RegistrationForm;
import org.edx.mobile.model.registration.RegistrationFormField;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.task.RegisterTask;
import org.edx.mobile.view.custom.ETextView;
import org.edx.mobile.view.registration.IRegistrationFieldView;
import org.edx.mobile.view.registration.RegistrationEditTextView;
import org.edx.mobile.view.registration.RegistrationSpinnerView;
import org.edx.mobile.view.registration.RegistrationTextAreaView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class RegisterActivity extends BaseFragmentActivity {

    private RelativeLayout createAccountBtn;
    private LinearLayout requiredFieldsLayout;
    private LinearLayout optionalFieldsLayout;
    private ETextView createAccountTv;
    private List<IRegistrationFieldView> mFieldViews = new ArrayList<>();

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
                if(optional_text.getText().toString().equalsIgnoreCase(getString(R.string.show_optional_text))) {
                    optionalFieldsLayout.setVisibility(v.VISIBLE);
                    optional_text.setText(getString(R.string.hide_optional_text));
                }
                else{
                    optionalFieldsLayout.setVisibility(v.GONE);
                    optional_text.setText(getString(R.string.show_optional_text));
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
    }

    //Disable the Fields(Views) when doing server call
    private void setElementsDisabled(){
        createButtonDisabled();
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
            InputStream in = getAssets().open("config/registration_form.json");
            Gson gson = new Gson();
            RegistrationForm form = gson.fromJson(new InputStreamReader(in), RegistrationForm.class);

            LayoutInflater inflater = getLayoutInflater();

            for (RegistrationFormField field : form.getFields()) {
                IRegistrationFieldView fieldView = null;

                if (field.getFieldType().equalsIgnoreCase(RegistrationFieldType.EMAIL.toString())
                        || field.getFieldType().equalsIgnoreCase(RegistrationFieldType.PASSWORD.toString())
                        || field.getFieldType().equalsIgnoreCase(RegistrationFieldType.TEXT.toString())) {
                    View view = inflater.inflate(R.layout.view_register_edit_text, null);
                    fieldView = new RegistrationEditTextView(field, view);
                }
                else if (field.getFieldType().equalsIgnoreCase(RegistrationFieldType.TEXTAREA.toString())) {
                    View view = inflater.inflate(R.layout.view_register_text_area, null);
                    fieldView = new RegistrationTextAreaView(field, view);
                }
                else if (field.getFieldType().equalsIgnoreCase(RegistrationFieldType.MULTI.toString())) {
                    View view = inflater.inflate(R.layout.view_register_spinner, null);
                    fieldView = new RegistrationSpinnerView(field, view);
                }

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
        // TODO: validate

        // TODO: prepare query (POST body)
        Bundle parameters = new Bundle();
        for (IRegistrationFieldView v : mFieldViews) {
            if (v.hasValue()) {
                parameters.putString(v.getField().getName(), v.getCurrentValue().getAsString());
            }
        }

        RegisterTask task = new RegisterTask(this, parameters) {

            @Override
            public void onFinish(RegisterResponse result) {
                logger.debug("registration success=" + result.isSuccess());
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
            }
        };
        task.execute();
    }
}
