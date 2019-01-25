package org.edx.mobile.tta.ui.logistration;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.LinearLayout;

import org.edx.mobile.R;
import org.edx.mobile.module.registration.model.RegistrationOption;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.custom.FormEditText;
import org.edx.mobile.tta.ui.custom.FormSpinner;
import org.edx.mobile.tta.ui.logistration.view_model.UserInfoViewModel;
import org.edx.mobile.tta.utils.DataUtil;
import org.edx.mobile.tta.utils.ViewUtil;

import java.util.List;

public class UserInfoActivity extends BaseVMActivity {
    private LinearLayout userInfoLayout;

    private FormEditText etFirstName;
    private FormSpinner stateSpinner;
    private FormSpinner districtSpinner;
    private FormSpinner blockSpinner;
    private FormSpinner professionSpinner;
    private FormSpinner genderSpinner;
    private FormSpinner classTaughtSpinner;
    private FormSpinner dietSpinner;
    private FormEditText etPmis;
    private Button btn;

    private UserInfoViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new UserInfoViewModel(this);
        binding(R.layout.t_activity_user_info, mViewModel);
        userInfoLayout = findViewById(R.id.user_info_fields_layout);

        mViewModel.getData();
        getBlocks();
        setupForm();
    }

    private void getBlocks(){
        if (mViewModel.currentState == null || mViewModel.currentDistrict == null){
            return;
        }

        mViewModel.getBlocks(
                new OnResponseCallback<List<RegistrationOption>>() {
                    @Override
                    public void onSuccess(List<RegistrationOption> data) {
                        blockSpinner.setItems(mViewModel.blocks, mViewModel.blocks.get(0));
                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });
    }

    private void setupForm(){
        ViewUtil.addHeading(userInfoLayout, "Additional Information");
        ViewUtil.addSubHeading(userInfoLayout, "Please enter the following details");
        ViewUtil.addEmptySpace(userInfoLayout, (int) getResources().getDimension(R.dimen._14dp));

        etFirstName = ViewUtil.addFormEditText(userInfoLayout, "Full Name");
        etFirstName.setMandatory(true);

        stateSpinner = ViewUtil.addOptionSpinner(userInfoLayout,"Select State", mViewModel.states, null);
        stateSpinner.setMandatory(true);

        districtSpinner = ViewUtil.addOptionSpinner(userInfoLayout,"Select District", mViewModel.districts, null);
        districtSpinner.setMandatory(true);

        blockSpinner = ViewUtil.addOptionSpinner(userInfoLayout,"Select Block", mViewModel.blocks, null);
        professionSpinner = ViewUtil.addOptionSpinner(userInfoLayout,"Select Profession", mViewModel.professions, null);
        genderSpinner = ViewUtil.addOptionSpinner(userInfoLayout,"Select Gender", mViewModel.genders, null);
        classTaughtSpinner = ViewUtil.addOptionSpinner(userInfoLayout,"Select Class Taught", mViewModel.classesTaught, null);
        etPmis = ViewUtil.addFormEditText(userInfoLayout, "PMIS Code");
        dietSpinner = ViewUtil.addOptionSpinner(userInfoLayout,"Select DIET Code", mViewModel.dietCodes, null);
        btn = ViewUtil.addButton(userInfoLayout, "Done");
        ViewUtil.addEmptySpace(userInfoLayout, (int) getResources().getDimension(R.dimen._50px));

        setListeners();
    }

    private void setListeners(){
        btn.setOnClickListener(v -> {
            if (!validate()){
                return;
            }
            Bundle parameters = new Bundle();
            parameters.putString("name", etFirstName.getText());
            parameters.putString("state", mViewModel.currentState);
            parameters.putString("district", mViewModel.currentDistrict);
            parameters.putString("block", blockSpinner.getSelectedOption().getName());
            parameters.putString("title", professionSpinner.getSelectedOption().getName());
            parameters.putString("gender", genderSpinner.getSelectedOption().getName());
            parameters.putString("classes_taught", classTaughtSpinner.getSelectedOption().getName());
            parameters.putString("pmis_code", etPmis.getText());
            parameters.putString("diet_code", dietSpinner.getSelectedOption().getName());
            mViewModel.submit(parameters);
        });

        stateSpinner.setOnItemSelectedListener((view, item) -> {
            mViewModel.currentState = item.getName();
            mViewModel.districts.clear();
            mViewModel.districts = DataUtil.getDistrictsByStateName(mViewModel.currentState);
            districtSpinner.setItems(mViewModel.districts, mViewModel.districts.get(0));
        });

        districtSpinner.setOnItemSelectedListener((view, item) -> {
            mViewModel.currentDistrict = item.getName();
            getBlocks();
        });
    }

    private boolean validate(){
        boolean valid = true;
        if (!etFirstName.validate()){
            valid = false;
            etFirstName.setError("Required");
        }
        if (!stateSpinner.validate()){
            valid = false;
            stateSpinner.setError("Required");
        }
        if (!districtSpinner.validate()){
            valid = false;
            districtSpinner.setError("Required");
        }
        if (!blockSpinner.validate()){
            valid = false;
            blockSpinner.setError("Required");
        }
        if (!professionSpinner.validate()){
            valid = false;
            professionSpinner.setError("Required");
        }
        if (!genderSpinner.validate()){
            valid = false;
            genderSpinner.setError("Required");
        }
        if (!classTaughtSpinner.validate()){
            valid = false;
            classTaughtSpinner.setError("Required");
        }
        if (!etPmis.validate()){
            valid = false;
            etPmis.setError("Required");
        }
        if (!dietSpinner.validate()){
            valid = false;
            dietSpinner.setError("Required");
        }

        return valid;
    }
}
