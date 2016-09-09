package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.Session;
import com.facebook.SessionState;
import com.google.inject.Inject;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.DrawerNavigationBinding;
import org.edx.mobile.event.AccountDataLoadedEvent;
import org.edx.mobile.event.ProfilePhotoUpdatedEvent;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.facebook.IUiLifecycleHelper;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.profiles.UserProfileActivity;
import org.edx.mobile.user.Account;
import org.edx.mobile.user.GetAccountTask;
import org.edx.mobile.user.ProfileImage;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.EmailUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.my_videos.MyVideosActivity;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;


public class NavigationFragment extends BaseFragment {

    private static final String TAG = "NavigationFragment";
    private DrawerNavigationBinding drawerNavigationBinding;
    private final Logger logger = new Logger(getClass().getName());
    @Nullable
    private GetAccountTask getAccountTask;
    @Nullable
    private ProfileImage profileImage;
    ProfileModel profile;
    @Nullable
    ImageView imageView;
    private IUiLifecycleHelper uiLifecycleHelper;

    @Inject
    IEdxEnvironment environment;
    @Inject
    Config config;
    @Inject
    LoginPrefs loginPrefs;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiLifecycleHelper = IUiLifecycleHelper.Factory.getInstance(getActivity(), callback);
        uiLifecycleHelper.onCreate(savedInstanceState);
        profile = loginPrefs.getCurrentUserProfile();
        if (config.isUserProfilesEnabled() && profile != null && profile.username != null) {
            getAccountTask = new GetAccountTask(getActivity(), profile.username);
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
                    .load(R.drawable.profile_photo_placeholder)
                    .into(imageView);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        drawerNavigationBinding = DataBindingUtil.inflate(inflater, R.layout.drawer_navigation, container, false);
        if (config.isUserProfilesEnabled()) {
            if (null != profileImage) {
                loadProfileImage(profileImage, drawerNavigationBinding.profileImage);
            }
            if (profile != null && profile.username != null) {
                drawerNavigationBinding.profileImage.setOnClickListener(new OnClickListener() {
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
            drawerNavigationBinding.profileImage.setVisibility(View.GONE);
            drawerNavigationBinding.navigationHeaderLayout.setClickable(false);
            drawerNavigationBinding.navigationHeaderLayout.setForeground(null);
        }

        drawerNavigationBinding.drawerOptionMyCourses.setOnClickListener(new OnClickListener() {
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

        drawerNavigationBinding.drawerOptionMyVideos.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity act = getActivity();
                ((BaseFragmentActivity) act).closeDrawer();

                if (!(act instanceof MyVideosActivity)) {
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

        if (environment.getConfig().getCourseDiscoveryConfig().isCourseDiscoveryEnabled()) {
            drawerNavigationBinding.drawerOptionFindCourses.setOnClickListener(new OnClickListener() {
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
        } else {
            drawerNavigationBinding.drawerOptionFindCourses.setVisibility(View.GONE);
        }

        drawerNavigationBinding.drawerOptionMySettings.setOnClickListener(new OnClickListener() {
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

        drawerNavigationBinding.drawerOptionSubmitFeedback.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String to = environment.getConfig().getFeedbackEmailAddress();
                String subject = getString(R.string.email_subject);

                String osVersionText = String.format("%s %s", getString(R.string.android_os_version), android.os.Build.VERSION.RELEASE);
                String appVersionText = String.format("%s %s", getString(R.string.app_version), BuildConfig.VERSION_NAME);
                String deviceModelText = String.format("%s %s", getString(R.string.android_device_model), Build.MODEL);
                String feedbackText = getString(R.string.insert_feedback);
                String body = osVersionText + "\n" + appVersionText + "\n" + deviceModelText + "\n\n" + feedbackText;
                EmailUtil.openEmailClient(getActivity(), to, subject, body, environment.getConfig());
            }
        });


        if (profile != null) {
            if (profile.name != null) {
                drawerNavigationBinding.nameTv.setText(profile.name);
            }
            if (profile.email != null) {
                drawerNavigationBinding.emailTv.setText(profile.email);
            }
            Map<String,CharSequence> map = new HashMap<>();
            map.put("username", profile.name);
            map.put("email", profile.email);
            drawerNavigationBinding.userInfoLayout.setContentDescription(ResourceUtil.getFormattedString(getResources(), R.string.navigation_header, map));
        }

        drawerNavigationBinding.logoutButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                environment.getRouter().forceLogout(getActivity(), environment.getSegment(), environment.getNotificationDelegate());
            }
        });

        drawerNavigationBinding.tvVersionNo.setText(String.format("%s %s %s",
                getString(R.string.label_version), BuildConfig.VERSION_NAME, environment.getConfig().getEnvironmentDisplayName()));

        return drawerNavigationBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        uiLifecycleHelper.onResume();
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

    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull ProfilePhotoUpdatedEvent event) {
        if (event.getUsername().equalsIgnoreCase(profile.username)) {
            if (null == event.getUri()) {
                Glide.with(NavigationFragment.this)
                        .load(R.drawable.profile_photo_placeholder)
                        .into(drawerNavigationBinding.profileImage);
            } else {
                Glide.with(NavigationFragment.this)
                        .load(event.getUri())
                        .skipMemoryCache(true) // URI is re-used in subsequent events; disable caching
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(drawerNavigationBinding.profileImage);
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull AccountDataLoadedEvent event) {
        final Account account = event.getAccount();
        if (account.getUsername().equalsIgnoreCase(profile.username)) {
            profileImage = account.getProfileImage();
            if (drawerNavigationBinding.profileImage != null) {
                loadProfileImage(account.getProfileImage(), drawerNavigationBinding.profileImage);
            }
        }
    }
}
