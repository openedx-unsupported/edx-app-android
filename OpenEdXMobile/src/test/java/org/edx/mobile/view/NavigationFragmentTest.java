package org.edx.mobile.view;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import org.edx.mobile.R;
import org.edx.mobile.event.AccountDataLoadedEvent;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.user.Account;
import org.edx.mobile.user.ProfileImage;
import org.junit.Test;
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil;

import de.greenrobot.event.EventBus;
import roboguice.RoboGuice;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NavigationFragmentTest extends UiTest {
    @Test
    public void testProfileImageUpdation_onGlobalAccountEvent() {
        final NavigationFragment fragment = new NavigationFragment();
        SupportFragmentTestUtil.startVisibleFragment(fragment);
        final View view = fragment.getView();
        assertNotNull(view);

        final ImageView profileImage = (ImageView) view.findViewById(R.id.profile_image);
        assertNotNull(profileImage);

        // Assert: Profile pic not updated when a non-logged in user's account object is broadcasted
        Drawable previousDrawable = profileImage.getDrawable();
        Account account = configureMockAccount("not_logged_in_user");
        EventBus.getDefault().post(new AccountDataLoadedEvent(account));
        assertEquals(previousDrawable, profileImage.getDrawable());

        // Assert: Profile pic is updated when a logged-in user's account object is broadcasted
        UserPrefs userPrefs = RoboGuice.getInjector(context).getInstance(UserPrefs.class);
        final String loggedInUser = userPrefs.getProfile().username;
        account = configureMockAccount(loggedInUser);
        EventBus.getDefault().post(new AccountDataLoadedEvent(account));
        assertNotEquals(previousDrawable, profileImage.getDrawable());
    }


    @NonNull
    private Account configureMockAccount(String username) {
        final ProfileImage profileImage = mock(ProfileImage.class);
        when(profileImage.hasImage()).thenReturn(true);
        when(profileImage.getImageUrlLarge()).thenReturn("file:///mnt/sdcard/dummy.jpg");
        final Account account = mock(Account.class);
        when(account.getUsername()).thenReturn(username);
        when(account.getProfileImage()).thenReturn(profileImage);
        return account;
    }
}
