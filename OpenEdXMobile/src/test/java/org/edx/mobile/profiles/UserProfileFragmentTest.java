package org.edx.mobile.profiles;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import org.assertj.core.api.Assertions;
import org.edx.mobile.R;
import org.edx.mobile.base.profile.ProfileValues;
import org.edx.mobile.databinding.FragmentUserProfileBinding;
import org.edx.mobile.util.images.ErrorUtils;
import org.edx.mobile.view.adapters.StaticFragmentPagerAdapter;
import org.edx.mobile.view.base.PresenterFragmentTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.HiltTestApplication;

@HiltAndroidTest
@Config(application = HiltTestApplication.class)
@RunWith(RobolectricTestRunner.class)
public class UserProfileFragmentTest extends PresenterFragmentTest<
        UserProfileFragmentTest.TestableUserProfileFragment,
        UserProfilePresenter,
        UserProfilePresenter.ViewInterface> {

    @Rule()
    public HiltAndroidRule hiltAndroidRule = new HiltAndroidRule(this);

    @Before
    public void init() {
        hiltAndroidRule.inject();
    }

    FragmentUserProfileBinding binding;

    @Before
    public void before() {
        startFragment(TestableUserProfileFragment.newInstance(ProfileValues.USERNAME));
        binding = DataBindingUtil.getBinding(fragment.getView());
        assertNotNull(binding);
    }

    @Test
    public void setName_updatesTextView() {
        view.setUsername(ProfileValues.USERNAME);
        Assertions.assertThat(binding.nameText.getText()).isEqualToIgnoringCase(ProfileValues.USERNAME);
    }

    @Test
    public void setEditProfileMenuButtonVisible_withTrue_showsEditProfileOption() {
        view.setEditProfileMenuButtonVisible(true);
        assertNotNull(fragment.getActivity().findViewById(R.id.edit_profile));
    }

    @Test
    public void setEditProfileMenuButtonVisible_withFalse_hidesEditProfileOption() {
        view.setEditProfileMenuButtonVisible(false);
        assertNull(fragment.getActivity().findViewById(R.id.edit_profile));
    }

    @Test
    public void setProfile_withLoadingContentType_showsLoadingIndicatorAndHidesContent() {
        view.showLoading();
        Assertions.assertThat(binding.contentLoadingIndicator.getRoot().getVisibility()).isEqualTo(View.VISIBLE);
        Assertions.assertThat(binding.profileBodyContent.getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void click_onEditProfileOption_callsEditProfile() {
        view.setEditProfileMenuButtonVisible(true);
        Shadows.shadowOf(fragment.getActivity()).clickMenuItem(R.id.edit_profile);
        verify(presenter).onEditProfile();
    }

    @Test
    public void click_onHomeButton_doesNotCallEditProfile() {
        Shadows.shadowOf(fragment.getActivity()).clickMenuItem(android.R.id.home);
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
        Assertions.assertThat(binding.profileBodyContent.getVisibility()).isEqualTo(View.VISIBLE);
//        assertThat(binding.noAboutMe).isVisible();
        Assertions.assertThat(binding.languageContainer.getVisibility()).isNotEqualTo(View.VISIBLE);
        Assertions.assertThat(binding.locationContainer.getVisibility()).isNotEqualTo(View.VISIBLE);
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
        Assertions.assertThat(binding.profileBodyContent.getVisibility()).isEqualTo(View.VISIBLE);
//        assertThat(binding.bioText).isVisible().hasText(ProfileValues.ABOUT_ME);
        Assertions.assertThat(binding.languageContainer.getVisibility()).isEqualTo(View.VISIBLE);
        Assertions.assertThat(binding.languageText.getText()).isEqualTo(ProfileValues.LANGUAGE_NAME);
        Assertions.assertThat(binding.locationContainer.getVisibility()).isEqualTo(View.VISIBLE);
        Assertions.assertThat(binding.locationText.getText()).isEqualTo(ProfileValues.COUNTRY_NAME);
    }

    @Test
    public void showError_withRuntimeException_showsErrorTextAndHidesContent() {
        final RuntimeException error = new RuntimeException();
        view.showError(error);
        Assertions.assertThat(binding.contentError.getRoot().getVisibility()).isEqualTo(View.VISIBLE);
        Assertions.assertThat(binding.contentError.contentErrorText.getText()).isEqualTo(ErrorUtils.getErrorMessage(error, fragment.getActivity()));
        Assertions.assertThat(binding.profileBodyContent.getVisibility()).isNotEqualTo(View.VISIBLE);
        Assertions.assertThat(binding.contentLoadingIndicator.getRoot().getVisibility()).isNotEqualTo(View.VISIBLE);
    }

    @Test
    public void showTabs_withBioTabOnly() {
        final UserProfileTab userProfileTab = new UserProfileTab(R.string.profile_tab_bio, UserProfileBioFragment.class);
        final List<UserProfileTab> tabs = Collections.singletonList(userProfileTab);
        view.showTabs(tabs);
        verify(fragment.mockAdapter).setItems(UserProfileFragment.pagerItemsFromProfileTabs(tabs, fragment.getResources()));
        Assertions.assertThat(binding.profileSectionTabs.getVisibility()).isNotEqualTo(View.VISIBLE);
    }

    @Test
    public void showTabs_withBioAndAccomplishmentsTab() {
        final UserProfileTab userProfileTab = new UserProfileTab(R.string.profile_tab_bio, UserProfileBioFragment.class);
        final UserProfileTab accomplishmentsTab = new UserProfileTab(R.string.profile_tab_accomplishment, UserProfileAccomplishmentsFragment.class);
        final List<UserProfileTab> tabs = Arrays.asList(userProfileTab, accomplishmentsTab);
        view.showTabs(tabs);
        verify(fragment.mockAdapter).setItems(UserProfileFragment.pagerItemsFromProfileTabs(tabs, fragment.getResources()));
        Assertions.assertThat(binding.profileSectionTabs.getVisibility()).isNotEqualTo(View.VISIBLE);
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
