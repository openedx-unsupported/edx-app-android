package org.edx.mobile.view;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.event.AccountDataLoadedEvent;
import org.edx.mobile.event.DiscoveryTabSelectedEvent;
import org.edx.mobile.event.MoveToDiscoveryTabEvent;
import org.edx.mobile.event.ProfilePhotoUpdatedEvent;
import org.edx.mobile.event.ScreenArgumentsEvent;
import org.edx.mobile.model.FragmentItemModel;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.user.Account;
import org.edx.mobile.user.ProfileImage;
import org.edx.mobile.user.UserAPI;
import org.edx.mobile.user.UserService;
import org.edx.mobile.util.ConfigUtil;
import org.edx.mobile.util.UserProfileUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import retrofit2.Call;

public class MainTabsDashboardFragment extends TabsBaseFragment {

    private ProfileModel profile;

    private ToolbarCallbacks toolbarCallbacks;

    @Nullable
    private Call<Account> getAccountCall;

    @Inject
    private LoginPrefs loginPrefs;

    @Inject
    private UserService userService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final boolean isUserProfileEnabled = environment.getConfig().isUserProfilesEnabled();
        if (isUserProfileEnabled) {
            profile = loginPrefs.getCurrentUserProfile();
            sendGetUpdatedAccountCall();
            toolbarCallbacks.getProfileView().setVisibility(View.VISIBLE);
        } else {
            toolbarCallbacks.getProfileView().setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.my_courses, menu);
        menu.findItem(R.id.menu_item_account).setVisible(true);
        menu.findItem(R.id.menu_item_account).setIcon(
                new IconDrawable(getContext(), FontAwesomeIcons.fa_gear)
                        .colorRes(getContext(), R.color.toolbar_controls_color)
                        .actionBarSize(getContext()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_account: {
                environment.getRouter().showAccountActivity(getActivity());
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        toolbarCallbacks = (ToolbarCallbacks) getActivity();
    }

    public void sendGetUpdatedAccountCall() {
        getAccountCall = userService.getAccount(profile.username);
        getAccountCall.enqueue(new UserAPI.AccountDataUpdatedCallback(
                getActivity(),
                profile.username,
                null, // Disable global loading indicator
                null)); // No place to show an error notification
    }

    @Override
    protected boolean showTitleInTabs() {
        return true;
    }

    @Override
    public List<FragmentItemModel> getFragmentItems() {
        ArrayList<FragmentItemModel> items = new ArrayList<>();

        items.add(new FragmentItemModel(MyCoursesListFragment.class,
                getResources().getString(R.string.label_my_courses), FontAwesomeIcons.fa_list_alt,
                new FragmentItemModel.FragmentStateListener() {
                    @Override
                    public void onFragmentSelected() {
                        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.MY_COURSES);
                    }
                }));

        if (environment.getConfig().getProgramConfig().isEnabled()) {
            items.add(new FragmentItemModel(WebViewProgramFragment.class,
                    getResources().getString(R.string.label_my_programs), FontAwesomeIcons.fa_clone,
                    WebViewProgramFragment.makeArguments(environment.getConfig().getProgramConfig().getUrl(),
                            null, true),
                    new FragmentItemModel.FragmentStateListener() {
                        @Override
                        public void onFragmentSelected() {

                        }
                    }));
        }

        if (ConfigUtil.Companion.isCourseDiscoveryEnabled(environment) ||
                ConfigUtil.Companion.isProgramDiscoveryEnabled(environment)) {
            items.add(new FragmentItemModel(MainDiscoveryFragment.class,
                    getResources().getString(R.string.label_discovery), FontAwesomeIcons.fa_search,
                    getArguments(),
                    () -> EventBus.getDefault().post(new DiscoveryTabSelectedEvent())
            ));
        }

        return items;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull MoveToDiscoveryTabEvent event) {
        if (!ConfigUtil.Companion.isCourseDiscoveryEnabled(environment)) {
            return;
        }
        if (binding != null) {
            binding.viewPager2.setCurrentItem(binding.viewPager2.getAdapter().getItemCount() - 1);
            if (event.getScreenName() != null) {
                EventBus.getDefault().post(ScreenArgumentsEvent.Companion.getNewInstance(event.getScreenName()));
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull ProfilePhotoUpdatedEvent event) {
        if (!environment.getConfig().isUserProfilesEnabled()) {
            return;
        }
        final ImageView profileImage = toolbarCallbacks.getProfileView();
        if (event.getUsername().equalsIgnoreCase(profile.username)) {
            UserProfileUtils.loadProfileImage(getContext(), event, profileImage);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull AccountDataLoadedEvent event) {
        if (!environment.getConfig().isUserProfilesEnabled()) {
            return;
        }
        final Account account = event.getAccount();
        if (account.getUsername().equalsIgnoreCase(profile.username)) {
            final ImageView profileImage = toolbarCallbacks.getProfileView();
            if (profileImage != null) {
                loadProfileImage(account.getProfileImage(), profileImage);
            }
        }
    }

    private void loadProfileImage(@NonNull ProfileImage profileImage, @NonNull ImageView imageView) {
        if (profileImage.hasImage()) {
            Glide.with(this)
                    .load(profileImage.getImageUrlMedium())
                    .into(imageView);
        } else {
            Glide.with(this)
                    .load(R.drawable.profile_photo_placeholder)
                    .into(imageView);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != getAccountCall) {
            getAccountCall.cancel();
        }
        EventBus.getDefault().unregister(this);
    }
}
