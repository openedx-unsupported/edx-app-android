package org.edx.mobile.profiles;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.verify;

import androidx.databinding.DataBindingUtil;

import org.edx.mobile.databinding.FragmentUserProfileBioBinding;
import org.edx.mobile.view.PresenterFragmentTest;
import org.junit.Before;
import org.junit.Test;

public class UserProfileBioFragmentTest extends PresenterFragmentTest<UserProfileBioFragment, UserProfileBioPresenter, UserProfileBioPresenter.ViewInterface> {

    FragmentUserProfileBioBinding binding;

    @Before
    public void before() {
        startFragment(UserProfileBioFragment.newInstance());
        binding = DataBindingUtil.getBinding(fragment.getView());
        assertThat(binding).isNotNull();
    }

    @Test
    public void click_onIncompleteEditProfileButton_callsEditProfile() {
        binding.incompleteEditProfileButton.performClick();
        verify(presenter).onEditProfile();
    }

}
