package org.edx.mobile.profiles;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentUserProfileBinding;
import org.edx.mobile.util.images.ErrorUtils;
import org.edx.mobile.view.PresenterFragmentTest;
import org.edx.mobile.view.adapters.StaticFragmentPagerAdapter;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

public class UserProfileFragmentTest extends PresenterFragmentTest<
        UserProfileFragmentTest.TestableUserProfileFragment,
        UserProfilePresenter,
        UserProfilePresenter.ViewInterface> {

    FragmentUserProfileBinding binding;

    @Before
    public void before() {
        startFragment(TestableUserProfileFragment.newInstance(ProfileValues.USERNAME));
        binding = DataBindingUtil.getBinding(fragment.getView());
        assertThat(binding).isNotNull();
    }

    @Test
    public void setName_updatesTextView() {
        view.setUsername(ProfileValues.USERNAME);
        assertThat((TextView) binding.nameText).hasText(ProfileValues.USERNAME);
    }

    @Test
    public void setEditProfileMenuButtonVisible_withTrue_showsEditProfileOption() {
        view.setEditProfileMenuButtonVisible(true);
        assertThat(fragment.getActivity().findViewById(R.id.edit_profile)).isNotNull();
    }

    @Test
    public void setEditProfileMenuButtonVisible_withFalse_hidesEditProfileOption() {
        view.setEditProfileMenuButtonVisible(false);
        assertThat(fragment.getActivity().findViewById(R.id.edit_profile)).isNull();
    }

    @Test
    public void setProfile_withLoadingContentType_showsLoadingIndicatorAndHidesContent() {
        view.showLoading();
        assertThat(binding.contentLoadingIndicator.getRoot()).isVisible();
        assertThat(binding.profileBodyContent).isGone();
    }

    @Test
    public void click_onEditProfileOption_callsEditProfile() {
        view.setEditProfileMenuButtonVisible(true);
        shadowOf(fragment.getActivity()).clickMenuItem(R.id.edit_profile);
        verify(presenter).onEditProfile();
    }

    @Test
    public void click_onHomeButton_doesNotCallEditProfile() {
        shadowOf(fragment.getActivity()).clickMenuItem(android.R.id.home);
        verify(presenter, never()).onEditProfile();
    }

    @Test
    public void showProfile_withNoAboutMeOrCountryOrLanguage_showsNoAboutMeAndNoCountryAndNoLanguage() {
        view.showProfile(new UserProfileViewModel(
                UserProfileViewModel.LimitedProfileMessage.NONE,
                null,
                null,
                new UserProfileBioModel(
                        UserProfileBioModel.ContentType.NO_ABOUT_ME,
                        null
                )
        ));
        assertThat(binding.profileBodyContent).isVisible();
//        assertThat(binding.noAboutMe).isVisible();
        assertThat(binding.languageContainer).isNotVisible();
        assertThat(binding.locationContainer).isNotVisible();
    }

    @Test
    public void showProfile_withAboutMeAndCountryAndLanguage_showsAboutMeAndCountryAndLanguage() {
        view.showProfile(new UserProfileViewModel(
                UserProfileViewModel.LimitedProfileMessage.NONE,
                ProfileValues.LANGUAGE_NAME,
                ProfileValues.COUNTRY_NAME,
                new UserProfileBioModel(
                        UserProfileBioModel.ContentType.ABOUT_ME,
                        ProfileValues.ABOUT_ME)
        ));
        assertThat(binding.profileBodyContent).isVisible();
//        assertThat(binding.bioText).isVisible().hasText(ProfileValues.ABOUT_ME);
        assertThat(binding.languageContainer).isVisible();
        assertThat(binding.languageText).hasText(ProfileValues.LANGUAGE_NAME);
        assertThat(binding.locationContainer).isVisible();
        assertThat(binding.locationText).hasText(ProfileValues.COUNTRY_NAME);
    }

    @Test
    public void showError_withRuntimeException_showsErrorTextAndHidesContent() {
        final RuntimeException error = new RuntimeException();
        view.showError(error);
        assertThat(binding.contentError.getRoot()).isVisible();
        assertThat(binding.contentError.contentErrorText).hasText(ErrorUtils.getErrorMessage(error, fragment.getActivity()));
        assertThat(binding.profileBodyContent).isNotVisible();
        assertThat(binding.contentLoadingIndicator.getRoot()).isNotVisible();
    }

    @Test
    public void showTabs_withBioTabOnly() {
        final UserProfileTab userProfileTab = new UserProfileTab(R.string.profile_tab_bio, UserProfileBioFragment.class);
        final List<UserProfileTab> tabs = Collections.singletonList(userProfileTab);
        view.showTabs(tabs);
        verify(fragment.mockAdapter).setItems(UserProfileFragment.pagerItemsFromProfileTabs(tabs, fragment.getResources()));
        assertThat(binding.profileSectionTabs).isNotVisible();
    }

    @Test
    public void showTabs_withBioAndAccomplishmentsTab() {
        final UserProfileTab userProfileTab = new UserProfileTab(R.string.profile_tab_bio, UserProfileBioFragment.class);
        final UserProfileTab accomplishmentsTab = new UserProfileTab(R.string.profile_tab_accomplishment, UserProfileAccomplishmentsFragment.class);
        final List<UserProfileTab> tabs = Arrays.asList(userProfileTab, accomplishmentsTab);
        view.showTabs(tabs);
        verify(fragment.mockAdapter).setItems(UserProfileFragment.pagerItemsFromProfileTabs(tabs, fragment.getResources()));
        assertThat(binding.profileSectionTabs).isVisible();
    }

    public static class TestableUserProfileFragment extends UserProfileFragment {

        private StaticFragmentPagerAdapter mockAdapter = mock(StaticFragmentPagerAdapter.class);

        @NonNull
        public static TestableUserProfileFragment newInstance(@NonNull String username) {
            final TestableUserProfileFragment fragment = new TestableUserProfileFragment();
            fragment.setArguments(createArguments(username));
            return fragment;
        }

        @NonNull
        @Override
        protected StaticFragmentPagerAdapter createTabAdapter() {
            return mockAdapter;
        }
    }
}
