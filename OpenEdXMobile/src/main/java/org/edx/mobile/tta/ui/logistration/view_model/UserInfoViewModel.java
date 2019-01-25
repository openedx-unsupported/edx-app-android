package org.edx.mobile.tta.ui.logistration.view_model;

import android.os.Bundle;

import org.edx.mobile.module.registration.model.RegistrationOption;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.task.profile.GetUserAddressTask;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.logistration.model.UserAddressResponse;
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
        mActivity.show();
        Bundle parameters = new Bundle();

        parameters.putString("state",currentState);
        parameters.putString("district",currentDistrict);

        new GetUserAddressTask(mActivity, parameters){
            @Override
            protected void onSuccess(UserAddressResponse userAddressResponse) throws Exception {
                super.onSuccess(userAddressResponse);
                mActivity.hide();
                blocks.clear();
                if (userAddressResponse != null && userAddressResponse.getBlock() != null){
                    for (Object o: userAddressResponse.getBlock()){
                        blocks.add(new RegistrationOption(o.toString(), o.toString()));
                    }
                    callback.onSuccess(blocks);
                }
            }

            @Override
            protected void onException(Exception ex) {
                mActivity.hide();
                callback.onFailure(ex);
            }
        }.execute();
    }

    public void submit(Bundle parameters){
        mActivity.showShortSnack(parameters.toString());
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
