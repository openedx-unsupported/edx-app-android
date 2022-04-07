package org.edx.mobile.view.profiles;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.verify;

import androidx.databinding.DataBindingUtil;

import org.edx.mobile.databinding.FragmentUserProfileBioBinding;
import org.edx.mobile.profiles.UserProfileBioFragment;
import org.edx.mobile.profiles.UserProfileBioPresenter;
import org.edx.mobile.view.base.PresenterFragmentTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.HiltTestApplication;

@HiltAndroidTest
@Config(application = HiltTestApplication.class)
@RunWith(RobolectricTestRunner.class)
public class UserProfileBioFragmentTest extends PresenterFragmentTest<UserProfileBioFragment, UserProfileBioPresenter, UserProfileBioPresenter.ViewInterface> {

    @Rule()
    public HiltAndroidRule hiltAndroidRule = new HiltAndroidRule(this);

    @Before
    public void init() {
        hiltAndroidRule.inject();
    }

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
