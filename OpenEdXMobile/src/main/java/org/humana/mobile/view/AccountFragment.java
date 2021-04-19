package org.humana.mobile.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ObservableLong;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;
import com.google.inject.Inject;
import com.lib.mxcalendar.models.Event;

import org.humana.mobile.BuildConfig;
import org.humana.mobile.R;
import org.humana.mobile.base.BaseFragment;
import org.humana.mobile.core.IEdxEnvironment;
import org.humana.mobile.databinding.FragmentAccountBinding;
import org.humana.mobile.module.prefs.LoginPrefs;
import org.humana.mobile.tta.data.DataManager;
import org.humana.mobile.tta.data.constants.Constants;
import org.humana.mobile.tta.data.local.db.table.Program;
import org.humana.mobile.tta.data.local.db.table.Section;
import org.humana.mobile.tta.data.model.program.EventNotificationCount;
import org.humana.mobile.tta.data.model.program.NotificationCountResponse;
import org.humana.mobile.tta.event.CourseEnrolledEvent;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.tutorials.MxTooltip;
import org.humana.mobile.tta.ui.feed.NotificationsFragment;
import org.humana.mobile.tta.ui.landing.LandingActivity;
import org.humana.mobile.tta.ui.programs.notifications.NotificationActivity;
import org.humana.mobile.tta.ui.programs.selectSection.SelectSectionActivity;
import org.humana.mobile.tta.ui.programs.selectprogram.SelectProgramActivity;
import org.humana.mobile.tta.utils.ActivityUtil;
import org.humana.mobile.tta.utils.BottomNavigationViewHelper;
import org.humana.mobile.util.Config;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


public class AccountFragment extends BaseFragment {
    private static final String TAG = AccountFragment.class.getCanonicalName();
    private FragmentAccountBinding binding;

    @Inject
    private Config config;

    @Inject
    private IEdxEnvironment environment;

    @Inject
    private LoginPrefs loginPrefs;

    private DataManager mDataManager;

    public ObservableField<String> notificationBladge = new ObservableField<>();
    public ObservableField<String> notificationTooltipText = new ObservableField<>();
    public ObservableInt notficationTooltipGravity = new ObservableInt();


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        registerEventBus();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false);
        mDataManager = DataManager.getInstance(getActivity());

        binding.showNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotifications();
            }
        });
        getNotificationCount();

        binding.tvNotificationBadge.setText(notificationBladge.get());

        if (config.isUserProfilesEnabled()) {
            binding.profileBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    environment.getRouter().showUserProfile(getActivity(),
                            loginPrefs.getUsername());
                }
            });
        } else {
            binding.profileBtn.setVisibility(View.GONE);
        }

        binding.settingsBtn.setOnClickListener(v -> environment.getRouter().showSettings(getActivity()));

        binding.certificateBtn.setOnClickListener(v -> {
            mDataManager.getPrograms(new OnResponseCallback<List<Program>>() {
                @Override
                public void onSuccess(List<Program> data) {
                    if (data!=null && data.size()>0){
                        if (data.get(0).getId()!=null && data.get(0).getId()!=""){
                            String url = environment.getConfig().getApiHostURL() + "certificates/user/" /*+  "https://staging-dmm.humana.school/certificates/user/"*/ + environment.getLoginPrefs().getCurrentUserProfile().id + "/course/" + data.get(0).getId() /*"course-v1:humana+hm11+2020-22"*/;
                            environment.getRouter().showAuthenticatedWebviewActivity(getActivity(),
                                    url ,"View Certificate");
                        }
                    }
                }

                @Override
                public void onFailure(Exception e) {

                    ActivityUtil.gotoPage(getActivity(), LandingActivity.class,
                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
            });
        });

        binding.feedbackBtn.setOnClickListener(v ->
                environment.getRouter()
                .showFeedbackScreen(getActivity(), getString(R.string.email_subject)));

        binding.logoutBtn.setOnClickListener(v ->{

                AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        builder1.setMessage("Do you really want to logout.");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                (dialog, id) -> {
                    environment
                            .getRouter().performManualLogout(getActivity(),
                            environment.getAnalyticsRegistry(),
                            environment.getNotificationDelegate());
                    dialog.cancel();

                });

        builder1.setNegativeButton(
                "No",
                (dialog, id) -> dialog.cancel());

            AlertDialog alert11 = builder1.create();
            alert11.show();
                });

        binding.activateTutorialBtn.setOnClickListener(v -> enableTutorials());


        binding.changeProgBtn.setOnClickListener(v -> {

            mDataManager.getPrograms(new OnResponseCallback<List<Program>>() {
                @Override
                public void onSuccess(List<Program> data) {
                    if (data.size() == 0){
                        ActivityUtil.gotoPage(getActivity(), LandingActivity.class,
                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    }
                    else if (data.size() == 1) {
                        mDataManager.getLoginPrefs().setProgramTitle(data.get(0).getTitle());
                        mDataManager.getLoginPrefs().setProgramId(data.get(0).getId());
                        Constants.isSinglePrg = true;
                        mDataManager.getLoginPrefs().setParentId(data.get(0).getParent_id());

                        getSection();
                    } else {

                        ActivityUtil.gotoPage(getActivity(), SelectProgramActivity.class,
                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    }

                }

                @Override
                public void onFailure(Exception e) {

                    ActivityUtil.gotoPage(getActivity(), LandingActivity.class,
                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
            });
        });


        binding.tvVersionNo.setText(String.format("%s %s %s", getString(R.string.label_version),
                BuildConfig.VERSION_NAME, environment.getConfig().getEnvironmentDisplayName()));

//        if (!mDataManager.getLoginPrefs().isProfileTootipSeen()) {
//            showTooltip();
//        }

        return binding.getRoot();
    }
    private void getSection() {
        mDataManager.getSections(mDataManager.getLoginPrefs().getProgramId(), new OnResponseCallback<List<Section>>() {
            @Override
            public void onSuccess(List<Section> data) {

                if (data.size() == 1) {
                    mDataManager.getLoginPrefs().setSectionId(data.get(0).getId());
                    mDataManager.getLoginPrefs().setRole(data.get(0).getRole());

                    Constants.isSingleRow = true;
                    Snackbar.make(binding.clMain, R.string.single_program_text,
                            Snackbar.LENGTH_LONG).show();

                } else {
                    ActivityUtil.gotoPage(getActivity(), SelectSectionActivity.class,
                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                }

            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }
    public void showNotifications() {
        ActivityUtil.gotoPage(getActivity(),NotificationActivity.class);
        binding.tvNotificationBadge.setVisibility(View.GONE);
    }


    private void showTooltip(){
        notificationTooltipText.set("View all notification here");
        notficationTooltipGravity.set(Gravity.BOTTOM);

        new MxTooltip.Builder(getActivity())
                .anchorView(binding.showNotifications)
                .text(notificationTooltipText.get())
                .gravity(notficationTooltipGravity.get())
                .animated(true)
                .transparentOverlay(true)
                .arrowDrawable(R.drawable.up_arrow)
                .build()
                .show();

        mDataManager.getLoginPrefs().setProfileTootipSeen(true);
    }

    private void enableTutorials(){
        mDataManager.getLoginPrefs().setProfileTootipSeen(false);
        mDataManager.getLoginPrefs().setScheduleTootipSeen(false);
        mDataManager.getLoginPrefs().setUnitTootipSeen(false);
        mDataManager.getLoginPrefs().setPendingTootipSeen(false);
        mDataManager.getLoginPrefs().setStudentTootipSeen(false);
        showTooltip();
    }

    private void setNotificationBladge(){
        if (mDataManager.getLoginPrefs().getNotificationCount() > 9){
            binding.tvNotificationBadge.setVisibility(View.VISIBLE);
            binding.tvNotificationBadge.setText("9+");
        }else if (mDataManager.getLoginPrefs().getNotificationCount()==0 ||
                mDataManager.getLoginPrefs().getNotificationCount()==-1){
            binding.tvNotificationBadge.setVisibility(View.GONE);
        }
        else {
            binding.tvNotificationBadge.setVisibility(View.VISIBLE);
            binding.tvNotificationBadge.setText(String.valueOf(mDataManager.getLoginPrefs().getNotificationCount()));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterEventBus();
    }

//    @SuppressWarnings("unused")
    public void onEventMainThread(EventNotificationCount event) {
       if (event !=null){
           if (event.isCountChanged()){
                getNotificationCount();
           }
       }
    }

    @Override
    public void onResume() {
        super.onResume();
        getNotificationCount();
    }

    public void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
    }


    private void getNotificationCount(){
        mDataManager.getNotificationCount(mDataManager.getLoginPrefs().getProgramId(),
                new OnResponseCallback<NotificationCountResponse>() {
            @Override
            public void onSuccess(NotificationCountResponse response) {

                mDataManager.getLoginPrefs().setNotificationCount(response.getUnReadCount());
                setNotificationBladge();
//                EventBus.getDefault().post(new EventNotificationCount(true));
            }

            @Override
            public void onFailure(Exception e) {

                e.printStackTrace();
            }
        });
    }
}
