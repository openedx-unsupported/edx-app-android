package org.edx.mobile.login;

import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.view.LoginActivity;
import org.edx.mobile.view.PresenterActivityTest;
import org.edx.mobile.view.login.LoginPresenter;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginActivityTest extends PresenterActivityTest<LoginActivity, LoginPresenter, LoginPresenter.LoginViewInterface> {

    @Before
    public void setup() {
        startActivity(LoginActivity.newIntent(null));
    }

    @Test
    public void testSetSocialLoginButtons_withFacebookEnabled_facebookButtonIsVisible() {
        view.setSocialLoginButtons(false, true , false);
        assertThat(activity.findViewById(R.id.panel_login_social).getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(activity.findViewById(R.id.google_button).getVisibility()).isNotEqualTo(View.VISIBLE);
        assertThat(activity.findViewById(R.id.microsoft_button).getVisibility()).isNotEqualTo(View.VISIBLE);
        assertThat(activity.findViewById(R.id.facebook_button).getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void testSetSocialLoginButtons_withGoogleEnabled_googleButtonIsVisible() {
        view.setSocialLoginButtons(true, false, false);
        assertThat(activity.findViewById(R.id.panel_login_social).getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(activity.findViewById(R.id.google_button).getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(activity.findViewById(R.id.microsoft_button).getVisibility()).isNotEqualTo(View.VISIBLE);
        assertThat(activity.findViewById(R.id.facebook_button).getVisibility()).isNotEqualTo(View.VISIBLE);
    }

    @Test
    public void testSetSocialLoginButtons_withMicrosoftEnabled_microsoftButtonIsVisible() {
        view.setSocialLoginButtons(false, false, true);
        assertThat(activity.findViewById(R.id.panel_login_social).getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(activity.findViewById(R.id.google_button).getVisibility()).isNotEqualTo(View.VISIBLE);
        assertThat(activity.findViewById(R.id.facebook_button).getVisibility()).isNotEqualTo(View.VISIBLE);
        assertThat(activity.findViewById(R.id.microsoft_button).getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void testSetSocialLoginButtons_withSocialLoginEnabled_socialLoginButtonsAreVisible() {
        view.setSocialLoginButtons(true, true, true);
        assertThat(activity.findViewById(R.id.panel_login_social).getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(activity.findViewById(R.id.google_button).getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(activity.findViewById(R.id.facebook_button).getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(activity.findViewById(R.id.microsoft_button).getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void testSetSocialLoginButtons_withSocialLoginNotEnabled_socialLoginButtonsNotVisible() {
        view.setSocialLoginButtons(false, false, false);
        assertThat(activity.findViewById(R.id.panel_login_social).getVisibility()).isNotEqualTo(View.VISIBLE);
    }
}
