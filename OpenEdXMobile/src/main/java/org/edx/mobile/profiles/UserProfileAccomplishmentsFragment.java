package org.edx.mobile.profiles;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.databinding.FragmentUserProfileAccomplishmentsBinding;
import org.edx.mobile.model.profile.BadgeAssertion;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.user.UserService;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.images.ShareUtils;
import org.edx.mobile.view.PresenterFragment;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UserProfileAccomplishmentsFragment extends PresenterFragment<UserProfileAccomplishmentsPresenter, UserProfileAccomplishmentsPresenter.ViewInterface> implements ScrollingPreferenceChild {

    @Inject
    UserService userService;

    @Inject
    LoginPrefs loginPrefs;

    FragmentUserProfileAccomplishmentsBinding binding;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserProfileAccomplishmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @NonNull
    @Override
    protected UserProfileAccomplishmentsPresenter createPresenter() {
        final String username = ((UserProfileBioTabParent) getParentFragment()).getBioInteractor().getUsername();
        return new UserProfileAccomplishmentsPresenter(
                userService, username, username.equalsIgnoreCase(loginPrefs.getUsername()));
    }

    @NonNull
    @Override
    protected UserProfileAccomplishmentsPresenter.ViewInterface createView() {
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.list.setLayoutManager(linearLayoutManager);
        binding.list.addOnScrollListener(new InfiniteScrollUtils.RecyclerViewOnScrollListener(linearLayoutManager, new Runnable() {
            @Override
            public void run() {
                presenter.onScrolledToEnd();
            }
        }));
        final AccomplishmentListAdapter adapter = createAdapter(
                new AccomplishmentListAdapter.Listener() {
                    @Override
                    public void onShare(@NonNull BadgeAssertion badgeAssertion) {
                        presenter.onClickShare(badgeAssertion);
                    }
                });
        binding.list.setAdapter(adapter);
        return new UserProfileAccomplishmentsPresenter.ViewInterface() {
            @Override
            public void setModel(@NonNull UserProfileAccomplishmentsPresenter.ViewModel model) {
                adapter.setItems(model.badges);
                adapter.setPageLoading(model.pageLoading);
                adapter.setSharingEnabled(model.enableSharing);
            }

            @Override
            public void startBadgeShareIntent(@NonNull String badgeUrl) {
                final Map<String, CharSequence> shareTextParams = new HashMap<>();
                shareTextParams.put("platform_name", getString(R.string.platform_name));
                shareTextParams.put("badge_url", badgeUrl);
                final String shareText = ResourceUtil.getFormattedString(getResources(), R.string.share_accomplishment_message, shareTextParams).toString();
                startActivity(ShareUtils.newShareIntent(shareText));
            }
        };
    }

    @NonNull
    @VisibleForTesting
    protected AccomplishmentListAdapter createAdapter(@NonNull AccomplishmentListAdapter.Listener listener) {
        return new AccomplishmentListAdapter(
                MainApplication.getEnvironment(getContext()).getConfig().getApiHostURL(),
                listener);
    }

    @Override
    public boolean prefersScrollingHeader() {
        return true;
    }
}
