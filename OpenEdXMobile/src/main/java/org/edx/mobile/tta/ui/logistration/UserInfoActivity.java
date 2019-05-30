package org.edx.mobile.tta.ui.logistration;

import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.edx.mobile.R;
import org.edx.mobile.module.registration.model.RegistrationOption;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.custom.FormEditText;
import org.edx.mobile.tta.ui.custom.FormMultiSpinner;
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
    private FormMultiSpinner classTaughtSpinner;
    private FormSpinner dietSpinner;
    private FormEditText etPmis;
    private Button btn;
    private Toolbar toolbar;
    private UserInfoViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new UserInfoViewModel(this);
        binding(R.layout.t_activity_user_info, mViewModel);
        userInfoLayout = findViewById(R.id.user_info_fields_layout);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
        mViewModel.getData();
        getBlocks();
        getClasses();
        setupForm();
    }

    private void getBlocks() {
        if (mViewModel.currentState == null || mViewModel.currentDistrict == null) {
            mViewModel.blocks.clear();
            blockSpinner.setItems(mViewModel.blocks, null);
            return;
        }
        showLoading();
        mViewModel.getBlocks(
                new OnResponseCallback<List<RegistrationOption>>() {
                    @Override
                    public void onSuccess(List<RegistrationOption> data) {
                        hideLoading();
                        blockSpinner.setItems(mViewModel.blocks, null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        hideLoading();
                    }
                });
    }

    private void getClasses() {

        mViewModel.getClasses(new OnResponseCallback<List<RegistrationOption>>() {
            @Override
            public void onSuccess(List<RegistrationOption> data) {
                classTaughtSpinner.setItems(data, null);
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

    private void setupForm() {

        ViewUtil.addEmptySpace(userInfoLayout, (int) getResources().getDimension(R.dimen._14dp));

        etFirstName = ViewUtil.addFormEditText(userInfoLayout, "Name/नाम");
        etFirstName.setMandatory(true);

        stateSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "State/राज्य", mViewModel.states, null);
        stateSpinner.setMandatory(true);

        districtSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "District/" +
                "जिला", mViewModel.districts, null);
        districtSpinner.setMandatory(true);

        blockSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "Block/तहसील", mViewModel.blocks, null);
        blockSpinner.setMandatory(true);

        professionSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "Profession/व्यवसाय", mViewModel.professions, null);
        genderSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "Gender/लिंग", mViewModel.genders, null);

        classTaughtSpinner = ViewUtil.addMultiOptionSpinner(userInfoLayout, "Classes Taught/पढ़ाई गई कक्षा",
                mViewModel.classesTaught, null);
        classTaughtSpinner.setMandatory(true);

        etPmis = ViewUtil.addFormEditText(userInfoLayout, "PMIS Code/पी इम आइ इस कोड");
        etPmis.setShowTv(getApplicationContext().getString(R.string.please_insert_valide_pmis_code));
        dietSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "DIET Code/डी आइ इ टी कोड", mViewModel.dietCodes, null);
        btn = ViewUtil.addButton(userInfoLayout, "Sumbit");
        ViewUtil.addEmptySpace(userInfoLayout, (int) getResources().getDimension(R.dimen._50px));

        setListeners();
    }

    private void setListeners() {
        btn.setOnClickListener(v -> {
            if (!validate()) {
                return;
            }
            Bundle parameters = new Bundle();
            parameters.putString("name", etFirstName.getText().trim());
            parameters.putString("state", stateSpinner.getSelectedOption().getValue());
            parameters.putString("district", mViewModel.currentDistrict);
            parameters.putString("block", blockSpinner.getSelectedOption().getName());
            parameters.putString("title", professionSpinner.getSelectedOption().getValue());
            parameters.putString("gender", genderSpinner.getSelectedOption().getName());
            if (classTaughtSpinner.getSelectedOptions() != null) {
                StringBuilder builder = new StringBuilder();
                for (RegistrationOption option: classTaughtSpinner.getSelectedOptions()){
                    builder.append(mViewModel.classesSectionName).append("_").append(option.getName()).append(" ");
                }
                if (builder.length() > 0){
                    builder.deleteCharAt(builder.length() - 1);
                }
                parameters.putString("tag_label", builder.toString());
            }

            parameters.putString("pmis_code", etPmis.getText());
            parameters.putString("diet_code", dietSpinner.getSelectedOption().getName());
            mViewModel.submit(parameters);
        });

        stateSpinner.setOnItemSelectedListener((view, item) -> {
            if (item == null){
                mViewModel.currentState = null;
                mViewModel.districts.clear();
                districtSpinner.setItems(mViewModel.districts, null);
                mViewModel.dietCodes.clear();
                dietSpinner.setItems(mViewModel.dietCodes, null);
                return;
            }

            mViewModel.currentState = item.getName();

            mViewModel.districts.clear();
            mViewModel.districts = DataUtil.getDistrictsByStateName(mViewModel.currentState);
            districtSpinner.setItems(mViewModel.districts, null);

            mViewModel.dietCodes.clear();
            mViewModel.dietCodes = DataUtil.getAllDietCodesOfState(mViewModel.currentState);
            dietSpinner.setItems(mViewModel.dietCodes, null);
        });

        districtSpinner.setOnItemSelectedListener((view, item) -> {
            if (item != null) {
                mViewModel.currentDistrict = item.getName();
            } else {
                mViewModel.currentDistrict = null;
            }
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
