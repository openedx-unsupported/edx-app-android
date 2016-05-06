package org.edx.mobile.profiles;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.inject.Injector;

import org.edx.mobile.R;
import org.edx.mobile.core.EdxEnvironment;
import org.edx.mobile.databinding.FragmentUserProfileAccomplishmentsBinding;
import org.edx.mobile.user.UserAPI;
import org.edx.mobile.util.images.ShareUtils;
import org.edx.mobile.view.PresenterFragment;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;

import roboguice.RoboGuice;

public class UserProfileAccomplishmentsFragment extends PresenterFragment<UserProfileAccomplishmentsPresenter, InfiniteScrollUtils.ListContentController<BadgeAssertion>> {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return DataBindingUtil.inflate(inflater, R.layout.fragment_user_profile_accomplishments, container, false).getRoot();
    }

    @NonNull
    @Override
    protected UserProfileAccomplishmentsPresenter createPresenter() {
        final Injector injector = RoboGuice.getInjector(getActivity());
        return new UserProfileAccomplishmentsPresenter(
                injector.getInstance(UserAPI.class),
                ((UserProfileBioTabParent) getParentFragment()).getBioInteractor().getUsername());
    }

    @NonNull
    @Override
    protected InfiniteScrollUtils.ListContentController<BadgeAssertion> createView() {
        final FragmentUserProfileAccomplishmentsBinding binding = DataBindingUtil.getBinding(getView());
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.list.setLayoutManager(linearLayoutManager);
        binding.list.addOnScrollListener(new InfiniteScrollUtils.RecyclerViewOnScrollListener(linearLayoutManager, new Runnable() {
            @Override
            public void run() {
                presenter.onScrolledToEnd();
            }
        }));
        final AccomplishmentListAdapter adapter = new AccomplishmentListAdapter(
                RoboGuice.getInjector(getContext()).getInstance(EdxEnvironment.class).getConfig().getApiHostURL(),
                new AccomplishmentListAdapter.Listener() {
                    @Override
                    public void onShare(@NonNull BadgeAssertion badgeAssertion) {
                        startActivity(ShareUtils.newShareIntent(badgeAssertion.getAssertionUrl()));
                    }
                });
        binding.list.setAdapter(adapter);
        return adapter;
    }
}
