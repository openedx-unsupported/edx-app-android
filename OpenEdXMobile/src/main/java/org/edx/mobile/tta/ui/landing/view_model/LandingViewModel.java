package org.edx.mobile.tta.ui.landing.view_model;

import android.databinding.ObservableBoolean;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

import org.edx.mobile.R;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.tta.data.local.db.table.ContentStatus;
import org.edx.mobile.tta.event.ContentStatusReceivedEvent;
import org.edx.mobile.tta.event.ContentStatusesReceivedEvent;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.agenda.AgendaFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.feed.FeedFragment;
import org.edx.mobile.tta.ui.interfaces.SearchPageOpenedListener;
import org.edx.mobile.tta.ui.library.LibraryFragment;
import org.edx.mobile.tta.ui.profile.ProfileFragment;
import org.edx.mobile.tta.ui.search.SearchFragment;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class LandingViewModel extends BaseViewModel {

    private int selectedId = R.id.action_library;

    public ObservableBoolean navShiftMode = new ObservableBoolean();
    public ObservableBoolean offlineVisible = new ObservableBoolean();

    private List<ContentStatus> statuses;

    public BottomNavigationView.OnNavigationItemSelectedListener itemSelectedListener = item -> {
        if (item.getItemId() == selectedId){
            return true;
        }
        switch (item.getItemId()){
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
    };

    public LandingViewModel(BaseVMActivity activity) {
        super(activity);
        mDataManager.setWpProfileCache();
        navShiftMode.set(false);
        selectedId = R.id.action_library;
        showLibrary();
        onAppStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        onEventMainThread(new NetworkConnectivityChangeEvent());
    }

    public void showLibrary() {
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                LibraryFragment.newInstance(() -> selectedId = R.id.action_search),
                R.id.dashboard_fragment,
                LibraryFragment.TAG,
                false,
                null
        );
    }

    public void showFeed() {
//        mActivity.showShortSnack("Coming soon");
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new FeedFragment(),
                R.id.dashboard_fragment,
                FeedFragment.TAG,
                false,
                null
        );
    }

    public void showSearch(){
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new SearchFragment(),
                R.id.dashboard_fragment,
                SearchFragment.TAG,
                false,
                null
        );
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

    private void onAppStart(){
        mDataManager.updateNotifications(null);
        mDataManager.getMyContentStatuses(new OnResponseCallback<List<ContentStatus>>() {
            @Override
            public void onSuccess(List<ContentStatus> data) {
                statuses = data;
                EventBus.getDefault().postSticky(new ContentStatusesReceivedEvent(data));
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    public void selectLibrary(){
        selectedId = R.id.action_library;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NetworkConnectivityChangeEvent event){
        if (NetworkUtil.isConnected(mActivity)){
            offlineVisible.set(false);
        } else {
            offlineVisible.set(true);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ContentStatusReceivedEvent event){
        if (statuses == null){
            statuses = new ArrayList<>();
        }
        statuses.remove(event.getContentStatus());
        statuses.add(event.getContentStatus());
    }

    public void registerEventBus(){
        EventBus.getDefault().register(this);
    }

    public void unRegisterEventBus(){
        EventBus.getDefault().unregister(this);
    }
}
