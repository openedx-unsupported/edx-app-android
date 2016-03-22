package org.edx.mobile.profiles;

import android.databinding.DataBindingUtil;
import android.widget.TextView;

import org.assertj.core.api.Assertions;
import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentUserProfileBinding;
import org.edx.mobile.util.images.ErrorUtils;
import org.edx.mobile.view.PresenterFragmentTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

public class UserProfileFragmentTest extends PresenterFragmentTest<UserProfileFragment, UserProfilePresenter, UserProfilePresenter.ViewInterface> {

    FragmentUserProfileBinding binding;

    @Before
    public void before() {
        startFragment(UserProfileFragment.newInstance(ProfileValues.USERNAME));
        binding = DataBindingUtil.getBinding(fragment.getView());
        Assertions.assertThat(binding).isNotNull();
    }

    @Test
    public void setName_updatesTextView() {
        view.setName(ProfileValues.USERNAME);
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
    public void click_onParentalConsentEditProfileButton_callsEditProfile() {
        view.setEditProfileMenuButtonVisible(true);
        binding.parentalConsentEditProfileButton.performClick();
        verify(presenter).onEditProfile();
    }

    @Test
    public void click_onIncompleteEditProfileButton_callsEditProfile() {
        view.setEditProfileMenuButtonVisible(true);
        binding.incompleteEditProfileButton.performClick();
        verify(presenter).onEditProfile();
    }

    @Test
    public void showProfile_withNoAboutMeOrCountryOrLanguage_showsNoAboutMeAndNoCountryAndNoLanguage() {
        view.showProfile(new UserProfileViewModel(
                UserProfileViewModel.LimitedProfileMessage.NONE,
                null,
                null,
                UserProfileViewModel.ContentType.NO_ABOUT_ME,
                null
        ));
        assertThat(binding.profileBodyContent).isVisible();
        assertThat(binding.noAboutMe).isVisible();
        assertThat(binding.languageContainer).isNotVisible();
        assertThat(binding.locationContainer).isNotVisible();
    }

    @Test
    public void showProfile_withAboutMeAndCountryAndLanguage_showsAboutMeAndCountryAndLanguage() {
        view.showProfile(new UserProfileViewModel(
                UserProfileViewModel.LimitedProfileMessage.NONE,
                ProfileValues.LANGUAGE_NAME,
                ProfileValues.COUNTRY_NAME,
                UserProfileViewModel.ContentType.ABOUT_ME,
                ProfileValues.ABOUT_ME
        ));
        assertThat(binding.profileBodyContent).isVisible();
        assertThat(binding.bioText).isVisible().hasText(ProfileValues.ABOUT_ME);
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
}
