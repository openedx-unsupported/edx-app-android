package org.edx.mobile.profiles;

import static org.mockito.Mockito.verify;

import androidx.databinding.DataBindingUtil;

import org.assertj.core.api.Java6Assertions;
import org.edx.mobile.base.PresenterFragmentTest;
import org.edx.mobile.databinding.FragmentUserProfileBioBinding;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class UserProfileBioFragmentTest extends PresenterFragmentTest<UserProfileBioFragment, UserProfileBioPresenter, UserProfileBioPresenter.ViewInterface> {

    FragmentUserProfileBioBinding binding;

    @Before
    public void before() {
        startFragment(UserProfileBioFragment.newInstance());
        binding = DataBindingUtil.getBinding(fragment.getView());
        Java6Assertions.assertThat(binding).isNotNull();
    }

    @Test
    public void click_onIncompleteEditProfileButton_callsEditProfile() {
        binding.incompleteEditProfileButton.performClick();
        verify(presenter).onEditProfile();
    }

}
