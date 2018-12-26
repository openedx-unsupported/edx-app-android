package org.edx.mobile.tta.ui.dashboard.view_model;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.listing.ListingFragment;
import org.edx.mobile.tta.utils.ActivityUtil;

public class DashboardViewModel extends BaseViewModel {
    public DashboardViewModel(BaseVMActivity activity) {
        super(activity);
        showLibrary();
    }

    public void showLibrary(){
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new ListingFragment(),
                R.id.dashboard_fragment,
                ListingFragment.TAG,
                false,
                null
        );
    }

    public void showFeed(){

    }

    public void showSearch(){

    }

    public void showAgenda(){

    }

    public void showProfile(){

    }
}
