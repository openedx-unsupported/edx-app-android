package org.humana.mobile.tta.ui.landing.view_model;

import android.app.AlertDialog;
import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.net.Uri;
import android.support.design.widget.BottomNavigationView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.humana.mobile.R;
import org.humana.mobile.event.NetworkConnectivityChangeEvent;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.data.app_update.UpdateType;
import org.humana.mobile.tta.data.local.db.table.ContentStatus;
import org.humana.mobile.tta.data.model.UpdateResponse;
import org.humana.mobile.tta.event.ContentStatusReceivedEvent;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.agenda.AgendaFragment;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.ui.feed.FeedFragment;
import org.humana.mobile.tta.ui.library.LibraryFragment;
import org.humana.mobile.tta.ui.profile.ProfileFragment;
import org.humana.mobile.tta.ui.search.SearchFragment;
import org.humana.mobile.tta.utils.ActivityUtil;
import org.humana.mobile.util.NetworkUtil;
import org.humana.mobile.view.AccountFragment;
import org.humana.mobile.view.MyCoursesListFragment;
import org.humana.mobile.view.dialog.NativeFindCoursesFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.greenrobot.event.EventBus;

public class LandingViewModel extends BaseViewModel {

    private int selectedId = R.id.action_library;

    public ObservableBoolean navShiftMode = new ObservableBoolean();
    public ObservableBoolean offlineVisible = new ObservableBoolean();

    public ObservableField<String> tooltipText = new ObservableField<>();
    public ObservableInt tooltipGravity = new ObservableInt();
    public ObservableInt toolPosition = new ObservableInt();

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
//            case R.id.action_agenda:
//                selectedId = R.id.action_agenda;
//                showAgenda();
//                return true;
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
        mActivity.showLoading();

        if (!mDataManager.getLoginPrefs().isScheduleTootipSeen()) {
            setToolTip();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        onEventMainThread(new NetworkConnectivityChangeEvent());
        if (mDataManager.getLoginPrefs().getNotificationSeen() != null) {
            if (mDataManager.getLoginPrefs().getNotificationSeen().equals(Constants.NOTIFICATION_RECIEVED)) {
                mDataManager.getLoginPrefs().setNotificationSeen(Constants.NOTIFICATION_NOT_SEEN);
            }
        }
    }

    public void showLibrary() {
        mActivity.showLoading();
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                LibraryFragment.newInstance(() -> selectedId = R.id.action_search),
                R.id.dashboard_fragment,
                LibraryFragment.TAG,
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
                new AccountFragment(),
                R.id.dashboard_fragment,
                ProfileFragment.TAG,
                false,
                null
        );
    }


    public void selectLibrary() {
        selectedId = R.id.action_library;
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

    // App Update
    //hit the api one a day
    //store the latest version info in login pref
    //refresh latest version info every day.
    //now check the show update panel by checking the current and api latest version app

    private void getAppUpdate() {
        mDataManager.getUpdatedVersion(new OnResponseCallback<UpdateResponse>() {
            @Override
            public void onSuccess(UpdateResponse res) {
                if (res == null || res.version_code == null)
                    return;
                //set last update date time
                mDataManager.getAppPref().setUpdateSeenDate(Calendar.getInstance().getTime().toString());

                //store latest version info ,in-case user go to play store and come back without update.
                mDataManager.getLoginPrefs().storeLatestAppInfo(res);
                if (res.getVersion_code() > mDataManager.getCurrent_vCode()) {
                    decideUpdateUI(res);
                }
            }

            @Override
            public void onFailure(Exception e) {
            }
        }, mDataManager.getCurrentV_name(), mDataManager.getCurrent_vCode());
    }

    private void decideUpdateUI(UpdateResponse res) {
        if (res == null || res.version_code == null)
            return;

        String mFinal_notes = new String();
        if (res.getRelease_note() == null || res.getRelease_note().isEmpty())
            mFinal_notes = Constants.DefaultUpdateMessage;
        else
            mFinal_notes = res.getRelease_note();

        if (res.getStatus().toLowerCase().equals(UpdateType.FLEXIBLE.toString().toLowerCase())) {
            showFlexibleUpdate(mFinal_notes);

        } else if (res.getStatus().toLowerCase().equals(UpdateType.IMMEDIATE.toString().toLowerCase())) {
            showImmediateUpdate(mFinal_notes);
        }
    }

    private void showFlexibleUpdate(String notes_html) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater i = mActivity.getLayoutInflater();

        View v = i.inflate(R.layout.alert_dialog_layout, null);
        TextView title = v.findViewById(R.id.title);
        title.setText("An Update is Available !!");

        Button mbtn_delay = v.findViewById(R.id.btn_delay);
        Button mbtn_update = v.findViewById(R.id.btn_update);

        WebView flxible_notes_wv = v.findViewById(R.id.notes_flxible_wv);

        flxible_notes_wv.loadData(notes_html,
                "text/html", "UTF-8");

        builder.setView(v);
        AlertDialog dialog = builder.create();

        mbtn_delay.setOnClickListener(v1 -> {
            Constants.IsUpdateDelay = true;
            dialog.dismiss();
        });

        mbtn_update.setOnClickListener(v12 -> {
            mActivity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=org.tta.mobile")));
            dialog.dismiss();
        });

        builder.setCancelable(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showImmediateUpdate(String notes_html) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater i = mActivity.getLayoutInflater();

        View v = i.inflate(R.layout.alert_dialog_full_screen, null);
        WebView immediate_notes_wv = v.findViewById(R.id.immediate_notes_wv);

        immediate_notes_wv.loadData(notes_html,
                "text/html", "UTF-8");

        TextView title = v.findViewById(R.id.tv_title);
        title.setText("An Update is Available !!");

        Button mbtn_update = v.findViewById(R.id.btn_update);
        ImageView miv_close = v.findViewById(R.id.iv_close);

        builder.setView(v);
        AlertDialog dialog = builder.create();

        mbtn_update.setOnClickListener(v12 -> {
            mActivity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=org.tta.mobile")));
            dialog.dismiss();
        });

        miv_close.setOnClickListener(v1 -> {
            mActivity.finishAffinity();
            System.exit(0);
        });

        builder.setCancelable(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void setToolTip() {
        toolPosition.set(1);
        tooltipGravity.set(Gravity.TOP);
        tooltipText.set("Programs");
    }
}
