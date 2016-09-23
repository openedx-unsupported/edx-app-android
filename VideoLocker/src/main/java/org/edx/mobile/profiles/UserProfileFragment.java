package org.edx.mobile.profiles;

import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.inject.Inject;
import com.google.inject.Injector;

import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentUserProfileBinding;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.user.UserService;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.images.ErrorUtils;
import org.edx.mobile.view.PresenterFragment;
import org.edx.mobile.view.Router;
import org.edx.mobile.view.adapters.StaticFragmentPagerAdapter;

import java.util.LinkedList;
import java.util.List;

import de.greenrobot.event.EventBus;
import roboguice.RoboGuice;

public class UserProfileFragment extends PresenterFragment<UserProfilePresenter, UserProfilePresenter.ViewInterface> implements UserProfileBioTabParent, ScrollingPreferenceParent {

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
                injector.getInstance(ISegment.class),
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
        return new StaticFragmentPagerAdapter(getChildFragmentManager());
    }

    @NonNull
    @Override
    protected UserProfilePresenter.ViewInterface createView() {
        viewHolder = DataBindingUtil.getBinding(getView());

        viewHolder.profileSectionPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                onChildScrollingPreferenceChanged();
            }
        });

        final StaticFragmentPagerAdapter adapter = createTabAdapter();
        viewHolder.profileSectionPager.setAdapter(adapter);
        viewHolder.profileSectionTabs.setupWithViewPager(viewHolder.profileSectionPager);

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
            }

            @Override
            public void showLoading() {
                ((AppBarLayout.LayoutParams) viewHolder.profileHeader.getLayoutParams()).setScrollFlags(0);
                viewHolder.profileBody.setBackgroundColor(getResources().getColor(R.color.edx_brand_gray_x_back));
                viewHolder.profileSectionTabs.setVisibility(View.GONE);
                viewHolder.contentError.getRoot().setVisibility(View.GONE);
                viewHolder.profileBodyContent.setVisibility(View.GONE);
                viewHolder.contentLoadingIndicator.getRoot().setVisibility(View.VISIBLE);
            }

            @Override
            public void showError(@NonNull Throwable error) {
                ((AppBarLayout.LayoutParams) viewHolder.profileHeader.getLayoutParams()).setScrollFlags(0);
                viewHolder.profileBody.setBackgroundColor(getResources().getColor(R.color.edx_brand_gray_x_back));
                viewHolder.profileSectionTabs.setVisibility(View.GONE);
                viewHolder.contentLoadingIndicator.getRoot().setVisibility(View.GONE);
                viewHolder.profileBodyContent.setVisibility(View.GONE);
                viewHolder.contentError.getRoot().setVisibility(View.VISIBLE);
                viewHolder.contentError.contentErrorText.setText(ErrorUtils.getErrorMessage(error, getContext()));
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
            public void setName(@NonNull String name) {
                viewHolder.nameText.setText(name);
                viewHolder.nameText.setContentDescription(ResourceUtil.getFormattedString(
                        getResources(), R.string.profile_name_description, "name", name));
            }

            @Override
            public void navigateToProfileEditor(@NonNull String username) {
                router.showUserProfileEditor(getActivity(), username);
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
        if (position >= 0 && ((ScrollingPreferenceChild) ((StaticFragmentPagerAdapter) viewHolder.profileSectionPager.getAdapter()).getFragment(position))
                .prefersScrollingHeader()) {
            scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;
        } else {
            scrollFlags = 0;
        }
        final AppBarLayout.LayoutParams p = (AppBarLayout.LayoutParams) viewHolder.profileHeader.getLayoutParams();
        p.setScrollFlags(scrollFlags);
        viewHolder.profileHeader.setLayoutParams(p);
    }

    @NonNull
    @VisibleForTesting
    public static List<StaticFragmentPagerAdapter.Item> pagerItemsFromProfileTabs(@NonNull List<UserProfileTab> tabs, @NonNull Resources resources) {
        final List<StaticFragmentPagerAdapter.Item> pages = new LinkedList<>();
        for (UserProfileTab tab : tabs) {
            pages.add(new StaticFragmentPagerAdapter.Item(tab.getFragmentClass(), resources.getString(tab.getDisplayName())));
        }
        return pages;
    }
}
