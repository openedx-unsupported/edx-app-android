package org.edx.mobile.view.profiles;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.annotation.SuppressLint;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import org.edx.mobile.databinding.FragmentUserProfileAccomplishmentsBinding;
import org.edx.mobile.profiles.AccomplishmentListAdapter;
import org.edx.mobile.profiles.BadgeAssertion;
import org.edx.mobile.profiles.UserProfileAccomplishmentsFragment;
import org.edx.mobile.profiles.UserProfileAccomplishmentsPresenter;
import org.edx.mobile.view.PresenterFragmentTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

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
        Java6Assertions.assertThat(binding).isNotNull();
    }

    @Test
    public void adapterListener_onShare_callsPresenter() {
        assertThat(fragment.listener).isNotNull();
        final BadgeAssertion badgeAssertion = new BadgeAssertion();
        fragment.listener.onShare(badgeAssertion);
        verify(presenter).onClickShare(badgeAssertion);
    }

    @Test
    public void setModel_withZeroItemsAndLoadingTrue_setsAdapterItemsAndLoading() {
        final List<BadgeAssertion> items = Collections.emptyList();
        view.setModel(new UserProfileAccomplishmentsPresenter.ViewModel(items, true, false));
        verify(mockAdapter).setItems(items);
        verify(mockAdapter).setPageLoading(true);
    }

    @Test
    public void setModel_withZeroItemsAndLoadingFalse_setsAdapterItemsAndLoading() {
        final List<BadgeAssertion> items = Collections.emptyList();
        view.setModel(new UserProfileAccomplishmentsPresenter.ViewModel(items, false, false));
        verify(mockAdapter).setItems(items);
        verify(mockAdapter).setPageLoading(false);
    }

    @Test
    public void test_startBadgeShareIntent_startsShareActivity() {
        final String badgeUrl = "http://example.com";
        view.startBadgeShareIntent(badgeUrl);
        assertThat(shadowOf(fragment.getActivity()).getNextStartedActivity())
                .hasAction(Intent.ACTION_SEND);
    }

    @Test
    public void prefersScrollingHeader_isTrue() {
        assertThat(fragment.prefersScrollingHeader()).isTrue();
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
