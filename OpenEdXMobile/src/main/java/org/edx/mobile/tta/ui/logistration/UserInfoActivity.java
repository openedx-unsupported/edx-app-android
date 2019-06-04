package org.edx.mobile.tta.ui.logistration;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.edx.mobile.R;
import org.edx.mobile.module.registration.model.RegistrationOption;
import org.edx.mobile.tta.data.model.authentication.FieldInfo;
import org.edx.mobile.tta.data.model.authentication.Profession;
import org.edx.mobile.tta.data.model.authentication.StateCustomAttribute;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.custom.FormEditText;
import org.edx.mobile.tta.ui.custom.FormMultiSpinner;
import org.edx.mobile.tta.ui.custom.FormSpinner;
import org.edx.mobile.tta.ui.interfaces.OnTaItemClickListener;
import org.edx.mobile.tta.ui.logistration.view_model.UserInfoViewModel;
import org.edx.mobile.tta.utils.DataUtil;
import org.edx.mobile.tta.utils.ViewUtil;

import java.util.ArrayList;
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
    private FormMultiSpinner skillsSpinner;
    private FormSpinner dietSpinner;
    private FormEditText etPmis;
    private Button btn;
    private Toolbar toolbar;
    private UserInfoViewModel mViewModel;

    private String tagLabel;
    private FieldInfo fieldInfo;
    private String pmisError;

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
        getCustomFieldAttributes();
        mViewModel.getData();
        getBlocks();
        getClassesAndSkills();
        setupForm();
    }

    private void getCustomFieldAttributes(){

        mViewModel.getDataManager().getCustomFieldAttributes(new OnResponseCallback<FieldInfo>() {
            @Override
            public void onSuccess(FieldInfo data) {
                fieldInfo = data;
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

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

    private void getClassesAndSkills() {

        mViewModel.getClassesAndSkills(new OnResponseCallback<List<RegistrationOption>>() {
            @Override
            public void onSuccess(List<RegistrationOption> data) {
                classTaughtSpinner.setItems(data, null);
            }

            @Override
            public void onFailure(Exception e) {

            }
        }, new OnResponseCallback<List<RegistrationOption>>() {
            @Override
            public void onSuccess(List<RegistrationOption> data) {
                List<RegistrationOption> selectedOptions = null;
                if (tagLabel != null && tagLabel.length() > 0){
                    selectedOptions = new ArrayList<>();
                    for (String chunk: tagLabel.split(" ")){
                        String[] duet = chunk.split("_");
                        if (duet[0].equals(mViewModel.skillSectionName)){
                            selectedOptions.add(new RegistrationOption(duet[1], duet[1]));
                        }
                    }
                }
                skillsSpinner.setItems(data, selectedOptions);
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

        districtSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "District/जिला", mViewModel.districts, null);
        districtSpinner.setMandatory(true);

        blockSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "Block/तहसील", mViewModel.blocks, null);
        blockSpinner.setMandatory(true);

        professionSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "Profession/व्यवसाय", mViewModel.professions, null);
        professionSpinner.setMandatory(true);

        genderSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "Gender/लिंग", mViewModel.genders, null);
        genderSpinner.setMandatory(true);

        classTaughtSpinner = ViewUtil.addMultiOptionSpinner(userInfoLayout, "Classes Taught/पढ़ाई गई कक्षा",
                mViewModel.classesTaught, null);
        classTaughtSpinner.setMandatory(true);

        skillsSpinner = ViewUtil.addMultiOptionSpinner(userInfoLayout, "Skills/कौशल",
                mViewModel.skills, null);
        skillsSpinner.setMandatory(true);

        etPmis = ViewUtil.addFormEditText(userInfoLayout, "PMIS Code/पी इम आइ इस कोड");
        setCustomField(mViewModel.currentState, mViewModel.currentProfession);

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
            parameters.putString("gender", genderSpinner.getSelectedOption().getValue());

            StringBuilder builder = new StringBuilder();
            if (classTaughtSpinner.getSelectedOptions() != null) {
                for (RegistrationOption option: classTaughtSpinner.getSelectedOptions()){
                    builder.append(mViewModel.classesSectionName).append("_").append(option.getName()).append(" ");
                }
            }
            if (skillsSpinner.getSelectedOptions() != null) {
                for (RegistrationOption option: skillsSpinner.getSelectedOptions()){
                    builder.append(mViewModel.skillSectionName).append("_").append(option.getName()).append(" ");
                }
            }
            if (builder.length() > 0){
                builder.deleteCharAt(builder.length() - 1);
            }
            parameters.putString("tag_label", builder.toString());

            if (etPmis.isVisible()) {
                parameters.putString("pmis_code", etPmis.getText());
            }
            parameters.putString("diet_code", dietSpinner.getSelectedOption().getName());
            mViewModel.submit(parameters);
        });

        stateSpinner.setOnItemSelectedListener((view, item) -> {
            if (item == null){
                mViewModel.currentState = null;
                setCustomField(mViewModel.currentState, mViewModel.currentProfession);
                mViewModel.districts.clear();
                districtSpinner.setItems(mViewModel.districts, null);
                mViewModel.dietCodes.clear();
                dietSpinner.setItems(mViewModel.dietCodes, null);
                return;
            }

            mViewModel.currentState = item.getName();
            setCustomField(mViewModel.currentState, mViewModel.currentProfession);

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

        professionSpinner.setOnItemSelectedListener((view, item) -> {
            if (item != null) {
                mViewModel.currentProfession = item.getName();
            } else {
                mViewModel.currentProfession = null;
            }
            setCustomField(mViewModel.currentState, mViewModel.currentProfession);
        });
    }

    private void setCustomField(String stateName, String professionName){
        if (etPmis == null){
            return;
        }
        if (stateName == null || stateName.equals("") ||
                professionName == null || professionName.equals("") ||
                fieldInfo == null){
            etPmis.setVisibility(View.GONE);
            return;
        }

        for (StateCustomAttribute attribute: fieldInfo.getStateCustomAttribute()){
            if (stateName.equalsIgnoreCase(attribute.getState())){
                for (Profession profession: attribute.getProfession()){
                    if (professionName.equalsIgnoreCase(profession.getValue())){
                        etPmis.setHint(attribute.getLabel());
                        etPmis.setSubLabel(attribute.getHelptext());
                        etPmis.setVisibility(View.VISIBLE);
                        pmisError = attribute.getPlaceholder();
                    }
                }
            }
        }

        etPmis.setVisibility(View.GONE);
    }

    private boolean validate(){
        boolean valid = true;
        if (!etFirstName.validate() ||
                (mViewModel.getDataManager().getLoginPrefs().getUsername() != null &&
                        etFirstName.getText().trim().equals(mViewModel.getDataManager().getLoginPrefs().getUsername()))){
            valid = false;
            etFirstName.setError(getString(R.string.error_name));
        }
        if (!stateSpinner.validate()){
            valid = false;
            stateSpinner.setError(getString(R.string.error_state));
        }
        if (!districtSpinner.validate()){
            valid = false;
            districtSpinner.setError(getString(R.string.error_district));
        }
        if (!blockSpinner.validate()){
            valid = false;
            blockSpinner.setError(getString(R.string.error_block));
        }
        if (!professionSpinner.validate()){
            valid = false;
            professionSpinner.setError(getString(R.string.error_profession));
        }
        if (!genderSpinner.validate()){
            valid = false;
            genderSpinner.setError(getString(R.string.error_gender));
        }
        if (!classTaughtSpinner.validate()){
            valid = false;
            classTaughtSpinner.setError(getString(R.string.error_classes));
        }
        if (!skillsSpinner.validate()){
            valid = false;
            skillsSpinner.setError(getString(R.string.error_skills));
        }
        if (etPmis.isVisible() && !etPmis.validate()){
            valid = false;
            etPmis.setError(pmisError);
        }
        if (!dietSpinner.validate()){
            valid = false;
            dietSpinner.setError(getString(R.string.error_diet));
        }

        return valid;
    }

}
