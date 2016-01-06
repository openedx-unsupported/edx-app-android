package org.edx.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.Session;
import com.facebook.SessionState;
import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.event.ProfilePhotoUpdatedEvent;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.facebook.IUiLifecycleHelper;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.user.Account;
import org.edx.mobile.user.GetAccountTask;
import org.edx.mobile.user.ProfileImage;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.EmailUtil;
import org.edx.mobile.util.PropertyUtil;
import org.edx.mobile.view.dialog.IDialogCallback;
import org.edx.mobile.view.dialog.NetworkCheckDialogFragment;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;


public class NavigationFragment extends RoboFragment {

    private static final String TAG = "NavigationFragment";

    @Inject
    IEdxEnvironment environment;

    @Inject
    Config config;

    private PrefManager pref;
    private final Logger logger = new Logger(getClass().getName());
    private PrefManager socialPref;
    private NetworkCheckDialogFragment newFragment;

    private IUiLifecycleHelper uiLifecycleHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
        }
    };

    @Nullable
    private GetAccountTask getAccountTask;

    @Nullable
    private ProfileImage profileImage;

    ProfileModel profile;

    @Nullable
    ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiLifecycleHelper = IUiLifecycleHelper.Factory.getInstance(getActivity(), callback);
        uiLifecycleHelper.onCreate(savedInstanceState);
        Context context = getActivity().getBaseContext();
        socialPref = new PrefManager(context, PrefManager.Pref.FEATURES);
        pref = new PrefManager(context, PrefManager.Pref.LOGIN);
        profile = pref.getCurrentUserProfile();
        if (config.isUserProfilesEnabled() && profile != null && profile.username != null) {
            getAccountTask = new GetAccountTask(getActivity(), profile.username) {
                @Override
                protected void onSuccess(@NonNull Account account) throws Exception {
                    NavigationFragment.this.profileImage = account.getProfileImage();
                    if (null != imageView) {
                        loadProfileImage(account.getProfileImage(), imageView);
                    }
                }
            };
            getAccountTask.setTaskProcessCallback(null); // Disable global loading indicator
            getAccountTask.execute();
        }
        EventBus.getDefault().register(this);
    }

    private void loadProfileImage(@NonNull ProfileImage profileImage, @NonNull ImageView imageView) {
        if (profileImage.hasImage()) {
            Glide.with(NavigationFragment.this)
                    .load(profileImage.getImageUrlLarge())
                    .into(imageView);
        } else {
            Glide.with(NavigationFragment.this)
                    .load(R.drawable.xsie)
                    .into(imageView);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View layout = inflater.inflate(R.layout.drawer_navigation, container, false);

        final TextView name_tv = (TextView) layout.findViewById(R.id.name_tv);
        final TextView email_tv = (TextView) layout.findViewById(R.id.email_tv);
        final FrameLayout nameLayout = (FrameLayout) layout.findViewById(R.id.name_layout);
        imageView = (ImageView) layout.findViewById(R.id.profile_image);
        if (config.isUserProfilesEnabled()) {
            if (null != profileImage) {
                loadProfileImage(profileImage, imageView);
            }
            if (profile != null && profile.username != null) {
                nameLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final BaseFragmentActivity act = (BaseFragmentActivity) getActivity();
                        act.closeDrawer();

                        if (!(act instanceof UserProfileActivity)) {
                            environment.getRouter().showUserProfileWithNavigationDrawer(getActivity(), profile.username);

                            if (!(act instanceof MyCoursesListActivity)) {
                                act.finish();
                            }
                        }
                    }
                });
            }
        } else {
            imageView.setVisibility(View.GONE);

            // Disable any on-tap effects
            nameLayout.setClickable(false);
            nameLayout.setForeground(null);
        }

        TextView tvMyCourses = (TextView) layout.findViewById(R.id.drawer_option_my_courses);
        tvMyCourses.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Activity act = getActivity();
                ((BaseFragmentActivity) act).closeDrawer();

                if (!(act instanceof MyCoursesListActivity)) {
                    environment.getRouter().showMyCourses(act);
                    act.finish();
                }
            }
        });

        TextView tvMyVideos = (TextView) layout.findViewById(R.id.drawer_option_my_videos);
        tvMyVideos.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity act = getActivity();
                ((BaseFragmentActivity) act).closeDrawer();

                if (!(act instanceof MyVideosTabActivity)) {
                    environment.getRouter().showMyVideos(act);
                    //Finish need not be called if the current activity is MyCourseListing
                    // as on returning back from FindCourses,
                    // the student should be returned to the MyCourses screen
                    if (!(act instanceof MyCoursesListActivity)) {
                        act.finish();
                    }
                }
            }
        });


        TextView tvFindCourses = (TextView) layout.findViewById(R.id.drawer_option_find_courses);
        tvFindCourses.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ISegment segIO = environment.getSegment();
                segIO.trackUserFindsCourses();
                FragmentActivity act = getActivity();
                ((BaseFragmentActivity) act).closeDrawer();
                if (!(act instanceof WebViewFindCoursesActivity || act instanceof NativeFindCoursesActivity)) {
                    environment.getRouter().showFindCourses(act);

                    //Finish need not be called if the current activity is MyCourseListing
                    // as on returning back from FindCourses,
                    // the student should be returned to the MyCourses screen
                    if (!(act instanceof MyCoursesListActivity)) {
                        act.finish();
                    }
                }
            }
        });

        TextView tvMyGroups = (TextView) layout.findViewById(R.id.drawer_option_my_groups);
        tvMyGroups.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity act = getActivity();
                ((BaseFragmentActivity) act).closeDrawer();

                if (!(act instanceof MyGroupsListActivity)) {
                    environment.getRouter().showMyGroups(act);

                    if (!(act instanceof MyCoursesListActivity)) {
                        act.finish();
                    }
                }

            }
        });

        TextView tvSettings = (TextView) layout.findViewById(R.id.drawer_option_my_settings);
        tvSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity act = getActivity();
                ((BaseFragmentActivity) act).closeDrawer();

                if (!(act instanceof SettingsActivity)) {
                    environment.getRouter().showSettings(act);

                    if (!(act instanceof MyCoursesListActivity)) {
                        act.finish();
                    }
                }
            }
        });

        TextView tvSubmitFeedback = (TextView) layout.findViewById(R.id.drawer_option_submit_feedback);
        tvSubmitFeedback.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String to = environment.getConfig().getFeedbackEmailAddress();
                String subject = getString(R.string.email_subject);
                String email = "";
                EmailUtil.sendEmail(getActivity(), to, subject, email, environment.getConfig());
            }
        });


        if (profile != null) {
            if (profile.name != null) {
                name_tv.setText(profile.name);
            }
            if (profile.email != null) {
                email_tv.setText(profile.email);
            }
        }

        Button logout_btn = (Button) layout.findViewById(R.id.logout_button);
        logout_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                environment.getRouter().forceLogout(getActivity(), environment.getSegment(), environment.getNotificationDelegate());
            }
        });


        TextView version_tv = (TextView) layout.findViewById(R.id.tv_version_no);
        try {
            String versionName = PropertyUtil.getManifestVersionName(getActivity());

            if (versionName != null) {
                String envDisplayName = environment.getConfig().getEnvironmentDisplayName();
                String text = String.format("%s %s %s",
                        getString(R.string.label_version), versionName, envDisplayName);
                version_tv.setText(text);
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        uiLifecycleHelper.onResume();

        if (getView() != null) {
            View groupsItemView = getView().findViewById(R.id.drawer_option_my_groups);
            boolean allowSocialFeatures = socialPref.getBoolean(PrefManager.Key.ALLOW_SOCIAL_FEATURES, true);
            groupsItemView.setVisibility(allowSocialFeatures ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiLifecycleHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiLifecycleHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiLifecycleHelper.onDestroy();
        if (null != getAccountTask) {
            getAccountTask.cancel(true);
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        imageView = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        uiLifecycleHelper.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiLifecycleHelper.onSaveInstanceState(outState);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public static String getNavigationFragmentTag() {
        return TAG;
    }

    private void updateWifiSwitch(View layout) {
        final PrefManager wifiPrefManager = new PrefManager(
                getActivity().getBaseContext(), PrefManager.Pref.WIFI);
        Switch wifi_switch = (Switch) layout.findViewById(R.id.wifi_setting);

        wifi_switch.setOnCheckedChangeListener(null);
        wifi_switch.setChecked(wifiPrefManager.getBoolean(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, true));
        wifi_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, true);
                } else {
                    showWifiDialog();
                }
            }
        });
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull ProfilePhotoUpdatedEvent event) {
        if (event.getUsername().equalsIgnoreCase(profile.username)) {
            if (null == event.getUri()) {
                Glide.with(NavigationFragment.this)
                        .load(R.drawable.xsie)
                        .into(imageView);
            } else {
                Glide.with(NavigationFragment.this)
                        .load(event.getUri())
                        .skipMemoryCache(true) // URI is re-used in subsequent events; disable caching
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(imageView);
            }
        }
    }

    protected void showWifiDialog() {
        newFragment = NetworkCheckDialogFragment.newInstance(getString(R.string.wifi_dialog_title_help), getString(R.string.wifi_dialog_message_help), new IDialogCallback() {
            @Override
            public void onPositiveClicked() {
                try {
                    PrefManager wifiPrefManager = new PrefManager
                            (getActivity().getBaseContext(), PrefManager.Pref.WIFI);
                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, false);
                    updateWifiSwitch(getView());
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }

            @Override
            public void onNegativeClicked() {
                try {
                    PrefManager wifiPrefManager = new PrefManager(
                            getActivity().getBaseContext(), PrefManager.Pref.WIFI);

                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, true);
                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_OFF_WIFI_SHOW_DIALOG_FLAG, true);

                    updateWifiSwitch(getView());
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        });
        newFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        newFragment.show(getFragmentManager(), "dialog");
        newFragment.setCancelable(false);
    }
}
