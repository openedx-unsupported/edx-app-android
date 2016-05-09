package org.edx.mobile.profiles;

import android.annotation.SuppressLint;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.assertj.core.api.Assertions;
import org.edx.mobile.databinding.FragmentUserProfileAccomplishmentsBinding;
import org.edx.mobile.view.PresenterFragmentTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;

public class UserProfileAccomplishmentsFragmentTest extends PresenterFragmentTest<
        UserProfileAccomplishmentsFragmentTest.TestableUserProfileAccomplishmentsFragment,
        UserProfileAccomplishmentsPresenter,
        UserProfileAccomplishmentsPresenter.ViewInterface> {

    FragmentUserProfileAccomplishmentsBinding binding;

    @Mock
    AccomplishmentListAdapter mockAdapter;

    @Before
    public void before() {
        startFragment(new TestableUserProfileAccomplishmentsFragment(mockAdapter));
        binding = DataBindingUtil.getBinding(fragment.getView());
        Assertions.assertThat(binding).isNotNull();
    }

    @Test
    public void adapterListener_onShare_callsPresenter() {
        Assertions.assertThat(fragment.listener).isNotNull();
        final BadgeAssertion badgeAssertion = new BadgeAssertion();
        fragment.listener.onShare(badgeAssertion);
        verify(presenter).onClickShare(badgeAssertion);
    }

    @Test
    public void setModel_withZeroItemsAndLoadingTrue_setsAdapterItemsAndLoading() {
        final List<BadgeAssertion> items = Collections.emptyList();
        view.setModel(new UserProfileAccomplishmentsPresenter.ViewModel(items, true));
        verify(mockAdapter).setItems(items);
        verify(mockAdapter).setPageLoading(true);
    }

    @Test
    public void setModel_withZeroItemsAndLoadingFalse_setsAdapterItemsAndLoading() {
        final List<BadgeAssertion> items = Collections.emptyList();
        view.setModel(new UserProfileAccomplishmentsPresenter.ViewModel(items, false));
        verify(mockAdapter).setItems(items);
        verify(mockAdapter).setPageLoading(false);
    }

    @Test
    public void prefersScrollingHeader_isTrue() {
        Assertions.assertThat(fragment.prefersScrollingHeader()).isTrue();
    }

    @SuppressLint("ValidFragment")
    public static class TestableUserProfileAccomplishmentsFragment extends UserProfileAccomplishmentsFragment {

        @NonNull
        private final AccomplishmentListAdapter mockAdapter;

        @Nullable
        private AccomplishmentListAdapter.Listener listener;

        public TestableUserProfileAccomplishmentsFragment(@NonNull AccomplishmentListAdapter mockAdapter) {
            this.mockAdapter = mockAdapter;
        }

        @NonNull
        @Override
        protected AccomplishmentListAdapter createAdapter(@NonNull AccomplishmentListAdapter.Listener listener) {
            this.listener = listener;
            return mockAdapter;
        }
    }
}
