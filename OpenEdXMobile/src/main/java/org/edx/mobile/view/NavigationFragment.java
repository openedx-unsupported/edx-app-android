package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
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

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.DrawerNavigationBinding;
import org.edx.mobile.event.AccountDataLoadedEvent;
import org.edx.mobile.event.ProfilePhotoUpdatedEvent;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.module.facebook.IUiLifecycleHelper;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.profiles.UserProfileActivity;
import org.edx.mobile.user.Account;
import org.edx.mobile.user.ProfileImage;
import org.edx.mobile.user.UserAPI;
import org.edx.mobile.user.UserService;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.ResourceUtil;

import de.greenrobot.event.EventBus;
import retrofit2.Call;

public class NavigationFragment extends BaseFragment {

    private static final String TAG = "NavigationFragment";
    private DrawerNavigationBinding drawerNavigationBinding;
    private final Logger logger = new Logger(getClass().getName());
    @Nullable
    private Call<Account> getAccountCall;
    @Nullable
    private ProfileImage profileImage;
    ProfileModel profile;
    @Nullable
    ImageView imageView;
    private IUiLifecycleHelper uiLifecycleHelper;

    @Inject
    private UserService userService;

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
            getAccountCall = userService.getAccount(profile.username);
            getAccountCall.enqueue(new UserAPI.AccountDataUpdatedCallback(
                    getActivity(),
                    profile.username,
                    null, // Disable global loading indicator
                    null)); // No place to show an error notification
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

        if (environment.getConfig().getCourseDiscoveryConfig().isCourseDiscoveryEnabled()) {
            drawerNavigationBinding.drawerOptionFindCourses.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AnalyticsRegistry analyticsRegistry = environment.getAnalyticsRegistry();
                    analyticsRegistry.trackUserFindsCourses();
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
            if (config.getCourseDiscoveryConfig().isWebviewCourseDiscoveryEnabled()) {
                drawerNavigationBinding.drawerOptionFindCourses.setText(R.string.label_discover);
            } else {
                drawerNavigationBinding.drawerOptionFindCourses.setText(R.string.label_find_courses);
            }
        } else {
            drawerNavigationBinding.drawerOptionFindCourses.setVisibility(View.GONE);
        }

        drawerNavigationBinding.drawerOptionAccount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity act = getActivity();
                ((BaseFragmentActivity) act).closeDrawer();

                if (!(act instanceof AccountActivity)) {
                    environment.getRouter().showAccountActivity(act);

                    if (!(act instanceof MyCoursesListActivity)) {
                        act.finish();
                    }
                }
            }
        });

        if (profile != null) {
            if (profile.name != null) {
                drawerNavigationBinding.nameTv.setText(profile.name);
                drawerNavigationBinding.nameTv.setContentDescription(
                        ResourceUtil.getFormattedString(getResources(),
                                R.string.navigation_header, "username", profile.name));
            }
        }

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
        if (null != getAccountCall) {
            getAccountCall.cancel();
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
