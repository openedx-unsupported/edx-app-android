package org.humana.mobile.tta.ui.programs.selectprogram.viewmodel;

import android.databinding.ObservableBoolean;
import android.support.design.widget.BottomNavigationView;

import org.humana.mobile.R;
import org.humana.mobile.event.NetworkConnectivityChangeEvent;
import org.humana.mobile.tta.data.local.db.table.ContentStatus;
import org.humana.mobile.tta.event.ContentStatusReceivedEvent;
import org.humana.mobile.tta.ui.agenda.AgendaFragment;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.ui.feed.FeedFragment;
import org.humana.mobile.tta.ui.library.LibraryFragment;
import org.humana.mobile.tta.ui.profile.ProfileFragment;
import org.humana.mobile.tta.ui.programs.selectprogram.SelectProgramFragment;
import org.humana.mobile.tta.ui.search.SearchFragment;
import org.humana.mobile.tta.utils.ActivityUtil;
import org.humana.mobile.util.NetworkUtil;
import org.humana.mobile.view.AccountFragment;
import org.humana.mobile.view.MyCoursesListFragment;
import org.humana.mobile.view.dialog.NativeFindCoursesFragment;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class ProgramViewModel extends BaseViewModel {

    private int selectedId = R.id.action_library;

    public ObservableBoolean navShiftMode = new ObservableBoolean();
    public ObservableBoolean offlineVisible = new ObservableBoolean();

    private List<ContentStatus> statuses;

    public BottomNavigationView.OnNavigationItemSelectedListener itemSelectedListener = item -> {
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

            case R.id.action_profile:
                selectedId = R.id.action_profile;
                showProfile();
                return true;
            default:
                selectedId = R.id.action_library;
                showLibrary();
                return true;
        }
    };

    public ProgramViewModel(BaseVMActivity activity) {
        super(activity);
        mDataManager.setWpProfileCache();
        navShiftMode.set(false);
        selectedId = R.id.action_library;
        showLibrary();
        mActivity.showLoading();
    }

    @Override
    public void onResume() {
        super.onResume();
        onEventMainThread(new NetworkConnectivityChangeEvent());
    }

    public void showLibrary() {
        mActivity.showLoading();
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new SelectProgramFragment(),
                R.id.dashboard_fragment,
                SelectProgramFragment.TAG,
                false,
                null
        );
        mActivity.hideLoading();
    }

    public void showFeed() {
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new MyCoursesListFragment(),
                R.id.dashboard_fragment,
                FeedFragment.TAG,
                false,
                null
        );
        mActivity.hideLoading();
    }

    public void showSearch() {
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new NativeFindCoursesFragment(),
                R.id.dashboard_fragment,
                SearchFragment.TAG,
                false,
                null
        );
        mActivity.hideLoading();

    }


    public void showProfile() {
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new AccountFragment(),
                R.id.dashboard_fragment,
                ProfileFragment.TAG,
                false,
                null
        );
        mActivity.hideLoading();

    }



    @SuppressWarnings("unused")
    public void onEventMainThread(NetworkConnectivityChangeEvent event) {
        if (NetworkUtil.isConnected(mActivity)) {
            offlineVisible.set(false);
        } else {
            offlineVisible.set(true);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ContentStatusReceivedEvent event) {
        if (statuses == null) {
            statuses = new ArrayList<>();
        }
        statuses.remove(event.getContentStatus());
        statuses.add(event.getContentStatus());
    }

    public void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
    }
}
