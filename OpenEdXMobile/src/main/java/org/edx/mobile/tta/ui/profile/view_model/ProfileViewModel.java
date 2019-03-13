package org.edx.mobile.tta.ui.profile.view_model;

import android.content.Context;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;


public class ProfileViewModel extends BaseViewModel {
    public ProfileViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
    }

    public void logout(){
        mDataManager.logout();
    }

}
