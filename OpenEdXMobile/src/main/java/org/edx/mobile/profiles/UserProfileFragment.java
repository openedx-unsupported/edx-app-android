package org.edx.mobile.profiles;

import android.content.res.Resources;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.google.android.material.appbar.AppBarLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.joanzapata.iconify.Icon;

import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentUserProfileBinding;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.http.notifications.SnackbarErrorNotification;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.FragmentItemModel;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.user.UserService;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.images.ErrorUtils;
import org.edx.mobile.view.PresenterFragment;
import org.edx.mobile.view.Router;
import org.edx.mobile.view.adapters.StaticFragmentPagerAdapter;

import java.util.LinkedList;
import java.util.List;

import de.greenrobot.event.EventBus;
import roboguice.RoboGuice;

public class UserProfileFragment
        extends PresenterFragment<UserProfilePresenter, UserProfilePresenter.ViewInterface>
        implements UserProfileBioTabParent, ScrollingPreferenceParent, RefreshListener, StaticFragmentPagerAdapter.FragmentLifecycleCallbacks {

    @NonNull
    public static UserProfileFragment newInstance(@NonNull String username) {
        final UserProfileFragment fragment = new UserProfileFragment();
        fragment.setArguments(createArguments(username));
        return fragment;
    }

    @NonNull
    @VisibleForTesting
    public static Bundle createArguments(@NonNull String username) {
        final Bundle bundle = new Bundle();
        bundle.putString(UserProfileActivity.EXTRA_USERNAME, username);
        return bundle;
    }

    @Inject
    private Router router;

    protected final Logger logger = new Logger(getClass().getName());

    private SnackbarErrorNotification snackbarErrorNotification;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return DataBindingUtil.inflate(inflater, R.layout.fragment_user_profile, container, false).getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.edit_profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_profile: {
                presenter.onEditProfile();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @NonNull
    private String getUsername() {
        return getArguments().getString(UserProfileActivity.EXTRA_USERNAME);
    }

    @NonNull
    @Override
    protected UserProfilePresenter createPresenter() {
        final Injector injector = RoboGuice.getInjector(getActivity());
        final String username = getUsername();
        return new UserProfilePresenter(
                injector.getInstance(AnalyticsRegistry.class),
                new UserProfileInteractor(
                        username,
                        injector.getInstance(UserService.class),
                        injector.getInstance(EventBus.class),
                        injector.getInstance(UserPrefs.class)),
                new UserProfileTabsInteractor(
                        username,
                        injector.getInstance(UserService.class),
                        injector.getInstance(Config.class)
                ));
    }

    FragmentUserProfileBinding viewHolder;

    @VisibleForTesting
    @NonNull
    protected StaticFragmentPagerAdapter createTabAdapter() {
        return new StaticFragmentPagerAdapter(this , this);
    }

    @NonNull
    @Override
    protected UserProfilePresenter.ViewInterface createView() {
        viewHolder = DataBindingUtil.getBinding(getView());

        snackbarErrorNotification = new SnackbarErrorNotification(viewHolder.getRoot());
        // Disable viewpager swipe
        viewHolder.profileSectionPager.setUserInputEnabled(false);
        viewHolder.profileSectionPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                onChildScrollingPreferenceChanged();
            }
        });

        final StaticFragmentPagerAdapter adapter = createTabAdapter();
        viewHolder.profileSectionPager.setAdapter(adapter);

        // Attach Tab layout with viewpager2
        new TabLayoutMediator(viewHolder.profileSectionTabs, viewHolder.profileSectionPager, (tab, position) -> {
            tab.setText(adapter.getPageTitle(position));
        }).attach();

        return new UserProfilePresenter.ViewInterface() {
            @Override
            public void setEditProfileMenuButtonVisible(boolean visible) {
                setHasOptionsMenu(visible);
            }

            @Override
            public void showProfile(@NonNull UserProfileViewModel profile) {
                if (profile.limitedProfileMessage == UserProfileViewModel.LimitedProfileMessage.NONE) {
                    viewHolder.sharingLimited.setVisibility(View.GONE);
                } else {
                    viewHolder.sharingLimited.setVisibility(View.VISIBLE);
                    viewHolder.sharingLimited.setText(profile.limitedProfileMessage == UserProfileViewModel.LimitedProfileMessage.OWN_PROFILE
                            ? R.string.profile_sharing_limited_by_you
                            : R.string.profile_sharing_limited_by_other_user);
                }

                if (TextUtils.isEmpty(profile.language)) {
                    viewHolder.languageContainer.setVisibility(View.GONE);
                } else {
                    viewHolder.languageText.setText(profile.language);
                    viewHolder.languageText.setContentDescription(ResourceUtil.getFormattedString(
                            getResources(), R.string.profile_language_description, "language", profile.language));
                    viewHolder.languageContainer.setVisibility(View.VISIBLE);
                }

                if (TextUtils.isEmpty(profile.location)) {
                    viewHolder.locationContainer.setVisibility(View.GONE);
                } else {
                    viewHolder.locationText.setText(profile.location);
                    viewHolder.locationText.setContentDescription(ResourceUtil.getFormattedString(
                            getResources(), R.string.profile_location_description, "location", profile.location));
                    viewHolder.locationContainer.setVisibility(View.VISIBLE);
                }

                viewHolder.contentLoadingIndicator.getRoot().setVisibility(View.GONE);
                viewHolder.contentError.getRoot().setVisibility(View.GONE);
                viewHolder.profileBodyContent.setVisibility(View.VISIBLE);
                viewHolder.profileHeader.setVisibility(View.VISIBLE);

                onFinish();
            }

            @Override
            public void showLoading() {
                ((AppBarLayout.LayoutParams) viewHolder.profileHeader.getLayoutParams()).setScrollFlags(0);
                viewHolder.profileBody.setBackgroundColor(getResources().getColor(R.color.neutralLight));
                viewHolder.profileSectionTabs.setVisibility(View.GONE);
                viewHolder.contentError.getRoot().setVisibility(View.GONE);
                viewHolder.profileBodyContent.setVisibility(View.GONE);
                viewHolder.contentLoadingIndicator.getRoot().setVisibility(View.VISIBLE);
                viewHolder.profileHeader.setVisibility(View.GONE);
            }

            @Override
            public void showError(@NonNull Throwable error) {
                ((AppBarLayout.LayoutParams) viewHolder.profileHeader.getLayoutParams()).setScrollFlags(0);
                viewHolder.profileBody.setBackgroundColor(getResources().getColor(R.color.neutralLight));
                viewHolder.profileSectionTabs.setVisibility(View.GONE);
                viewHolder.contentLoadingIndicator.getRoot().setVisibility(View.GONE);
                viewHolder.profileBodyContent.setVisibility(View.GONE);

                final Icon errorIcon = ErrorUtils.getErrorIcon(error);
                viewHolder.contentError.getRoot().setVisibility(View.VISIBLE);
                if (errorIcon != null) {
                    viewHolder.contentError.contentErrorIcon.setIcon(errorIcon);
                }
                viewHolder.contentError.contentErrorText.setText(ErrorUtils.getErrorMessage(error, getContext()));
                viewHolder.contentError.contentErrorAction.setText(R.string.lbl_reload);
                viewHolder.contentError.contentErrorAction.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (NetworkUtil.isConnected(getContext())) {
                                    onRefresh();
                                }
                            }
                        });
                viewHolder.contentError.contentErrorAction.setVisibility(View.VISIBLE);
                viewHolder.profileHeader.setVisibility(View.GONE);

                onFinish();
            }

            @Override
            public void showTabs(@NonNull List<UserProfileTab> tabs) {
                adapter.setItems(pagerItemsFromProfileTabs(tabs, getResources()));
                viewHolder.profileSectionTabs.setVisibility(tabs.size() < 2 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void setPhotoImage(@NonNull UserProfileImageViewModel model) {
                if (null == model.uri) {
                    Glide.with(UserProfileFragment.this)
                            .load(R.drawable.profile_photo_placeholder)
                            .into(viewHolder.profileImage);

                } else if (model.shouldReadFromCache) {
                    Glide.with(UserProfileFragment.this)
                            .load(model.uri)
                            .into(viewHolder.profileImage);
                } else {
                    Glide.with(UserProfileFragment.this)
                            .load(model.uri)
                            .skipMemoryCache(true) // URI is re-used in subsequent events; disable caching
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(viewHolder.profileImage);
                }
            }

            @Override
            public void setUsername(@NonNull String username) {
                viewHolder.nameText.setText(username);
                viewHolder.nameText.setContentDescription(ResourceUtil.getFormattedString(
                        getResources(), R.string.profile_username_description, "username", username));
            }

            @Override
            public void navigateToProfileEditor(@NonNull String username) {
                router.showUserProfileEditor(getActivity(), username);
            }

            private void onFinish() {
                if (!EventBus.getDefault().isRegistered(UserProfileFragment.this)) {
                    EventBus.getDefault().registerSticky(UserProfileFragment.this);
                }
            }
        };
    }

    @Override
    public UserProfileBioInteractor getBioInteractor() {
        return presenter.getBioInteractor();
    }

    @Override
    public void onChildScrollingPreferenceChanged() {
        final int position = viewHolder.profileSectionTabs.getSelectedTabPosition();
        @AppBarLayout.LayoutParams.ScrollFlags
        final int scrollFlags;
        final Fragment fragment = (((StaticFragmentPagerAdapter) viewHolder.profileSectionPager.getAdapter()).getFragment(position));
        if (fragment != null) {
            if (position >= 0 &&
                    ((ScrollingPreferenceChild) fragment).prefersScrollingHeader()) {
                scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;
            } else {
                scrollFlags = 0;
            }
            final AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) viewHolder.profileHeader.getLayoutParams();
            params.setScrollFlags(scrollFlags);
            viewHolder.profileHeader.setLayoutParams(params);
        }
    }

    @NonNull
    @VisibleForTesting
    public static List<FragmentItemModel> pagerItemsFromProfileTabs(@NonNull List<UserProfileTab> tabs, @NonNull Resources resources) {
        final List<FragmentItemModel> pages = new LinkedList<>();
        for (UserProfileTab tab : tabs) {
            pages.add(new FragmentItemModel(tab.getFragmentClass(), resources.getString(tab.getDisplayName())));
        }
        return pages;
    }

    @Override
    public void onRefresh() {
        view.showLoading();
        presenter.onRefresh();
    }

    @SuppressWarnings("unused")
    public void onEvent(NetworkConnectivityChangeEvent event) {
        if (!NetworkUtil.isConnected(getContext())) {
            if (viewHolder.contentError.getRoot().getVisibility() != View.VISIBLE) {
                snackbarErrorNotification.showOfflineError(this);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onRevisit() {
        if (NetworkUtil.isConnected(getActivity())) {
            snackbarErrorNotification.hideError();
        }
    }

    @Override
    public void onFragmentInstantiate() {
        onChildScrollingPreferenceChanged();
    }
}
