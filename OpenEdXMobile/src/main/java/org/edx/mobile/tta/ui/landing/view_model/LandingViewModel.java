package org.edx.mobile.tta.ui.landing.view_model;

import android.databinding.ObservableBoolean;
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

public class LandingViewModel extends BaseViewModel {

    private int selectedId = R.id.action_library;

    public ObservableBoolean navShiftMode = new ObservableBoolean();

    public BottomNavigationView.OnNavigationItemSelectedListener itemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (item.getItemId() == selectedId) {
                return true;
            }
            switch (item.getItemId()) {
                case R.id.action_library:
                    selectedId = R.id.action_library;
                    showLibrary();
                    return true;
                case R.id.action_feed:
                    selectedId = R.id.action_feed;
                    showFeed();
                    return true;
                case R.id.action_search:
                    selectedId = R.id.action_search;
                    showSearch();
                    return true;
                case R.id.action_agenda:
                    selectedId = R.id.action_agenda;
                    showAgenda();
                    return true;
                case R.id.action_profile:
                    selectedId = R.id.action_profile;
                    showProfile();
                    return true;
                default:
                    selectedId = R.id.action_library;
                    showLibrary();
                    return true;
            }
        }
    };

    public LandingViewModel(BaseVMActivity activity) {
        super(activity);
        mDataManager.setWpProfileCache();
        navShiftMode.set(false);
        selectedId = R.id.action_library;
        showLibrary();
    }

    public void showLibrary() {
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new LibraryFragment(), R.id.dashboard_fragment,
                LibraryFragment.TAG,
                false,
                null
        );
    }

    public void showFeed() {
        mActivity.showShortSnack("Feed coming soon");
    }

    public void showSearch() {
        mActivity.showShortSnack("Search coming soon");
    }

    public void showAgenda() {
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new AgendaFragment(),
                R.id.dashboard_fragment,
                AgendaFragment.TAG,
                false,
                null
        );
    }

    public void showProfile() {
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
