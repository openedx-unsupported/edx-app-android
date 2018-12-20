package org.edx.mobile.tta.ui.dashboard.view_model;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.listing.ListingFragment;

public class DashboardViewModel extends BaseViewModel {
    public DashboardViewModel(BaseVMActivity activity) {
        super(activity);
        showLibrary();
    }

    public void showLibrary(){
        mActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.dashboard_fragment, new ListingFragment(), ListingFragment.TAG)
                .commit();
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
