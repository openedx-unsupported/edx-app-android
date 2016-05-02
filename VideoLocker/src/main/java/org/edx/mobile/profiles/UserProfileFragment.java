package org.edx.mobile.profiles;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
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
import org.edx.mobile.user.UserAPI;
import org.edx.mobile.util.images.ErrorUtils;
import org.edx.mobile.view.PresenterFragment;
import org.edx.mobile.view.Router;
import org.edx.mobile.view.adapters.StaticFragmentPagerAdapter;

import java.util.LinkedList;
import java.util.List;

import de.greenrobot.event.EventBus;
import roboguice.RoboGuice;

public class UserProfileFragment extends PresenterFragment<UserProfilePresenter, UserProfilePresenter.ViewInterface> implements UserProfileBioTabParent {

    public static UserProfileFragment newInstance(@NonNull String username) {
        final Bundle bundle = new Bundle();
        bundle.putString(UserProfileActivity.EXTRA_USERNAME, username);
        final UserProfileFragment fragment = new UserProfileFragment();
        fragment.setArguments(bundle);
        return fragment;
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
    @Override
    protected UserProfilePresenter createPresenter() {
        final Injector injector = RoboGuice.getInjector(getActivity());
        return new UserProfilePresenter(
                injector.getInstance(ISegment.class),
                new UserProfileInteractor(
                        getArguments().getString(UserProfileActivity.EXTRA_USERNAME),
                        injector.getInstance(UserAPI.class),
                        injector.getInstance(EventBus.class),
                        injector.getInstance(UserPrefs.class))
        );
    }

    @NonNull
    @Override
    protected UserProfilePresenter.ViewInterface createView() {
        final FragmentUserProfileBinding viewHolder = DataBindingUtil.getBinding(getView());

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
                    viewHolder.languageContainer.setVisibility(View.VISIBLE);
                }

                if (TextUtils.isEmpty(profile.location)) {
                    viewHolder.locationContainer.setVisibility(View.GONE);
                } else {
                    viewHolder.locationText.setText(profile.location);
                    viewHolder.locationContainer.setVisibility(View.VISIBLE);
                }

                viewHolder.contentLoadingIndicator.getRoot().setVisibility(View.GONE);
                viewHolder.contentError.getRoot().setVisibility(View.GONE);
                viewHolder.profileBodyContent.setVisibility(View.VISIBLE);
            }

            @Override
            public void showLoading() {
                viewHolder.profileBody.setBackgroundColor(getResources().getColor(R.color.edx_grayscale_neutral_xx_light));
                viewHolder.contentError.getRoot().setVisibility(View.GONE);
                viewHolder.profileBodyContent.setVisibility(View.GONE);
                viewHolder.contentLoadingIndicator.getRoot().setVisibility(View.VISIBLE);
            }

            @Override
            public void showError(@NonNull Throwable error) {
                viewHolder.profileBody.setBackgroundColor(getResources().getColor(R.color.edx_grayscale_neutral_xx_light));
                viewHolder.contentLoadingIndicator.getRoot().setVisibility(View.GONE);
                viewHolder.profileBodyContent.setVisibility(View.GONE);
                viewHolder.contentError.getRoot().setVisibility(View.VISIBLE);
                viewHolder.contentError.contentErrorText.setText(ErrorUtils.getErrorMessage(error, getContext()));
            }

            @Override
            public void showTabs(@NonNull List<UserProfileTab> tabs) {
                List<StaticFragmentPagerAdapter.Item> pages = new LinkedList<>();
                for(UserProfileTab tab : tabs) {
                    pages.add(new StaticFragmentPagerAdapter.Item(tab.getFragmentClass()));
                }
                PagerAdapter adapter = new StaticFragmentPagerAdapter(getChildFragmentManager(), pages);
                viewHolder.profileSectionPager.setAdapter(adapter);
                viewHolder.profileSectionTabs.setupWithViewPager(viewHolder.profileSectionPager);
                viewHolder.profileSectionTabs.setVisibility(tabs.size() < 2 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void setPhotoImage(@NonNull UserProfileImageViewModel model) {
                if (null == model.uri) {
                    Glide.with(UserProfileFragment.this)
                            .load(R.drawable.xsie)
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
}
