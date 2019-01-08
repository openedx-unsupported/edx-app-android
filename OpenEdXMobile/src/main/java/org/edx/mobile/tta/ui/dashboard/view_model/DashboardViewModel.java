package org.edx.mobile.tta.ui.dashboard.view_model;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableInt;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.agenda.AgendaFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.library.LibraryFragment;
import org.edx.mobile.tta.ui.profile.ProfileFragment;
import org.edx.mobile.tta.utils.ActivityUtil;

public class DashboardViewModel extends BaseViewModel {

    public ObservableInt selectedId = new ObservableInt();

    public ObservableBoolean navShiftMode = new ObservableBoolean();

    public BottomNavigationView.OnNavigationItemSelectedListener itemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){
                case R.id.action_library:
                    showLibrary();
                    return true;
                case R.id.action_feed:
                    showFeed();
                    return true;
                case R.id.action_search:
                    showSearch();
                    return true;
                case R.id.action_agenda:
                    showAgenda();
                    return true;
                case R.id.action_profile:
                    showProfile();
                    return true;
                default:
                    return false;
            }
        }
    };

    public DashboardViewModel(BaseVMActivity activity) {
        super(activity);
        selectedId.set(R.id.action_library);
        navShiftMode.set(false);
    }

    public void showLibrary(){
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new LibraryFragment(),
                R.id.dashboard_fragment,
                LibraryFragment.TAG,
                false,
                null
        );
    }

    public void showFeed(){
        mActivity.showShortSnack("Feed coming soon");
    }

    public void showSearch(){
        mActivity.showShortSnack("Search coming soon");
    }

    public void showAgenda(){
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new AgendaFragment(),
                R.id.dashboard_fragment,
                AgendaFragment.TAG,
                false,
                null
        );
    }

    public void showProfile(){
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new ProfileFragment(),
                R.id.dashboard_fragment,
                ProfileFragment.TAG,
                false,
                null
        );
    }
}
