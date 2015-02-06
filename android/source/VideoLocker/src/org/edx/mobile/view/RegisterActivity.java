package org.edx.mobile.view;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.view.custom.ETextView;

/**
 * Created by swapnalib on 5/2/15.
 */
public class RegisterActivity extends BaseFragmentActivity {

    private TextView optional_text,endUser_text;
    private EditText email_et,fullname_et,public_username_et,password_et,mailing_address_et,registering_reason_et;
    private LinearLayout panel;
    private Spinner country_spinner,education_spinner,gender_spinner,birth_spinner;
    private RelativeLayout create_account;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ETextView public_msg_tv=(ETextView)findViewById(R.id.public_username_msg);
        public_msg_tv.setText(Html.fromHtml(getString(R.string.public_username_msg)));
        initView();
        optionalTextVisibility();

    }


    private void optionalTextVisibility() {
        optional_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(optional_text.getText().toString().equalsIgnoreCase(getString(R.string.show_optional_text))) {
                    panel.setVisibility(v.VISIBLE);
                    optional_text.setText(getString(R.string.hide_optional_text));
                }
                else{
                    panel.setVisibility(v.GONE);
                    optional_text.setText(getString(R.string.show_optional_text));
                }
            }
        });

    }


    private void initView() {
        optional_text=(TextView)findViewById(R.id.optional_field_tv);
        panel=(LinearLayout)findViewById(R.id.optional_panel);
        email_et=(EditText)findViewById(R.id.email_et);
        fullname_et=(EditText)findViewById(R.id.fullname_et);
        public_username_et=(EditText)findViewById(R.id.public_username_et);
        password_et=(EditText)findViewById(R.id.password_et);
        mailing_address_et=(EditText)findViewById(R.id.mailing_address_et);
        registering_reason_et=(EditText)findViewById(R.id.registerening_reason_et);

        country_spinner=(Spinner)findViewById(R.id.country_spinner);
        education_spinner=(Spinner)findViewById(R.id.education_spinner);
        gender_spinner=(Spinner)findViewById(R.id.gender_spinner);
        birth_spinner=(Spinner)findViewById(R.id.birth_spinner);

        endUser_text=(TextView)findViewById(R.id.end_user_agreement_tv);
        endUser_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEulaDialog();

            }
        });
        create_account=(RelativeLayout)findViewById(R.id.createAccount_button_layout);
        create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }

    private void showEulaDialog() {
        showWebDialog(getString(R.string.eula_file_link), true,
                getString(R.string.end_user_title));
    }
}
