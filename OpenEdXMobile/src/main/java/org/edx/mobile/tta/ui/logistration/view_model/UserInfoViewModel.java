package org.edx.mobile.tta.ui.logistration.view_model;

import android.content.Intent;
import android.os.Bundle;

import org.edx.mobile.R;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.registration.model.RegistrationOption;
import org.edx.mobile.tta.data.model.profile.UpdateMyProfileResponse;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.task.profile.UpdateMyProfileTask;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.landing.LandingActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.tta.utils.DataUtil;

import java.util.ArrayList;
import java.util.List;

public class UserInfoViewModel extends BaseViewModel {

    public List<RegistrationOption> states;
    public List<RegistrationOption> districts;
    public List<RegistrationOption> blocks;
    public List<RegistrationOption> professions;
    public List<RegistrationOption> genders;
    public List<RegistrationOption> classesTaught;
    public List<RegistrationOption> dietCodes;

    public String currentState, currentDistrict;

    public UserInfoViewModel(BaseVMActivity activity) {
        super(activity);
    }

    public void getBlocks(OnResponseCallback<List<RegistrationOption>> callback){
        if (blocks == null){
            blocks = new ArrayList<>();
        }
        Bundle parameters = new Bundle();

        parameters.putString("state",currentState);
        parameters.putString("district",currentDistrict);

        mDataManager.getBlocks(callback, parameters, blocks);
    }

    public void submit(Bundle parameters){
        mActivity.showLoading();

        new UpdateMyProfileTask(mActivity, parameters,mDataManager.getLoginPrefs().getUsername())
        {
            @Override
            protected void onSuccess(UpdateMyProfileResponse response) throws Exception {
                super.onSuccess(response);
                mActivity.hideLoading();

                //for cache consistency
                ProfileModel profileModel = new ProfileModel();
                profileModel.name = response.getName();
                profileModel.email = response.getEmail();
                profileModel.gender=response.getGender();

                profileModel.title=response.getTitle();
                profileModel.classes_taught=response.getClasses_taught();
                profileModel.state=response.getState();
                profileModel.district=response.getDistrict();
                profileModel.block=response.getBlock();
                profileModel.pmis_code=response.getPMIS_code();
                profileModel.diet_code=response.getDIETCode();

                //for analytics update
                /*Analytic aHelper=new Analytic();
                aHelper.updateAnalytics(getApplicationContext(), aHelper.getAnalyticParams(loginPrefs.getUsername(),
                        "RegisterComplete", Action.Registration,
                        String.valueOf(Page.RegistrationPage), Source.Mobile));*/

                mDataManager.getLoginPrefs().setCurrentUserProfileInCache(profileModel);

                if(response.getRegistrationError()!=null &&
                        response.getRegistrationError().getField_name()!=null)
                {
                    mActivity.showErrorDialog(response.getRegistrationError().getTitle(),response.getRegistrationError().getMessage());
                }
                else
                {
                    mActivity.showLongSnack(mActivity.getString(R.string.registered_successfully));
                    ActivityUtil.gotoPage(mActivity, LandingActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mActivity.finish();
                }
            }

            @Override
            protected void onException(Exception ex) {
                mActivity.hideLoading();
                mActivity.showErrorDialog(null,"Your action could not be completed");
            }
        }.execute();

    }

    public void getData() {
        states = DataUtil.getAllStates();
        currentState = states.get(0).getName();
        districts = DataUtil.getDistrictsByStateName(states.get(0).getName());
        currentDistrict = districts.get(0).getName();
        professions = DataUtil.getAllProfessions();
        genders = DataUtil.getAllGenders();
        classesTaught = DataUtil.getAllClassesTaught();
        dietCodes = DataUtil.getAllDietCodes();
    }
}