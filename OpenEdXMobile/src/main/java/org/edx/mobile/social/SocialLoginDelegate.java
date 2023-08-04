package org.edx.mobile.social;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.edx.mobile.R;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.core.EdxDefaultModule;
import org.edx.mobile.exception.LoginErrorMessage;
import org.edx.mobile.exception.LoginException;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.authentication.AuthResponse;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.social.facebook.FacebookAuth;
import org.edx.mobile.social.facebook.FacebookProvider;
import org.edx.mobile.social.google.GoogleOauth2;
import org.edx.mobile.social.google.GoogleProvider;
import org.edx.mobile.social.microsoft.MicrosoftAuth;
import org.edx.mobile.social.microsoft.MicrosoftProvide;
import org.edx.mobile.task.Task;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.ConfigUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.ICommonUI;

import java.net.HttpURLConnection;
import java.util.HashMap;

import dagger.hilt.android.EntryPointAccessors;

/**
 * Code refactored from Login Activity, for the logic of login to social site are the same
 * for both login and registration.
 */
public class SocialLoginDelegate {

    protected final Logger logger = new Logger(getClass().getName());

    public enum Feature {SIGN_IN, REGISTRATION}

    private Activity activity;
    private MobileLoginCallback callback;
    private ISocial google, facebook, microsoft;
    private final LoginPrefs loginPrefs;

    private Feature feature;

    public SocialLoginDelegate(@NonNull Activity activity, @Nullable Bundle savedInstanceState,
                               @NonNull MobileLoginCallback callback, @NonNull Config config,
                               @NonNull LoginPrefs loginPrefs, @NonNull Feature feature) {

        this.activity = activity;
        this.callback = callback;
        this.loginPrefs = loginPrefs;
        this.feature = feature;

        google = getInstance(SocialAuthSource.GOOGLE, config);
        google.setCallback(accessToken -> {
            logger.debug("Google logged in; token= " + accessToken);
            onSocialLoginSuccess(accessToken, LoginPrefs.BACKEND_GOOGLE);
        });

        facebook = getInstance(SocialAuthSource.FACEBOOK, config);
        facebook.setCallback(accessToken -> {
            logger.debug("Facebook logged in; token= " + accessToken);
            onSocialLoginSuccess(accessToken, LoginPrefs.BACKEND_FACEBOOK);
        });

        microsoft = getInstance(SocialAuthSource.MICROSOFT, config);
        microsoft.setCallback(new ISocial.Callback() {
            @Override
            public void onCancel() {
                if (activity instanceof ICommonUI)
                    ((ICommonUI) activity).tryToSetUIInteraction(true);
            }

            @Override
            public void onError(@Nullable Exception exception) {
                if (activity instanceof ICommonUI)
                    ((ICommonUI) activity).tryToSetUIInteraction(true);
            }

            @Override
            public void onLogin(String accessToken) {
                logger.debug("Microsoft logged in; token= " + accessToken);
                onSocialLoginSuccess(accessToken, LoginPrefs.BACKEND_MICROSOFT);
            }
        });
        google.onActivityCreated(activity, savedInstanceState);
        facebook.onActivityCreated(activity, savedInstanceState);
        microsoft.onActivityCreated(activity, savedInstanceState);
    }

    public void onActivityDestroyed() {
        google.onActivityDestroyed(activity);
        facebook.onActivityDestroyed(activity);
        microsoft.onActivityDestroyed(activity);
    }

    public void onActivitySaveInstanceState(Bundle outState) {
        google.onActivitySaveInstanceState(activity, outState);
        facebook.onActivitySaveInstanceState(activity, outState);
        microsoft.onActivitySaveInstanceState(activity, outState);
    }

    public void onActivityStarted() {
        google.onActivityStarted(activity);
        facebook.onActivityStarted(activity);
        microsoft.onActivityStarted(activity);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        google.onActivityResult(requestCode, resultCode, data);
        facebook.onActivityResult(requestCode, resultCode, data);
        microsoft.onActivityResult(requestCode, resultCode, data);
    }

    public void onActivityStopped() {
        google.onActivityStopped(activity);
        facebook.onActivityStopped(activity);
        microsoft.onActivityStopped(activity);
    }

    private void socialLogin(SocialAuthSource socialAuthSource) {
        switch (socialAuthSource) {
            case FACEBOOK:
                facebook.login();
                break;
            case GOOGLE:
                google.login();
                break;
            case MICROSOFT:
                microsoft.login();
                break;
        }
    }

    private void socialLogout(SocialAuthSource socialAuthSource) {
        switch (socialAuthSource) {
            case FACEBOOK:
                facebook.logout();
                break;
            case GOOGLE:
                google.logout();
                break;
            case MICROSOFT:
                microsoft.logout();
                break;
        }
    }

    /**
     * called with you to use social login
     *
     * @param accessToken
     * @param backend
     */
    public void onSocialLoginSuccess(String accessToken, String backend) {
        loginPrefs.saveSocialLoginToken(accessToken, backend);
        Task<?> task = new ProfileTask(activity, accessToken, backend);
        callback.onSocialLoginSuccess(accessToken, backend, task);
        task.execute();
    }

    public void getUserInfo(SocialAuthSource socialAuthSource, String accessToken, final SocialUserInfoCallback userInfoCallback) {
        SocialProvider socialProvider = null;
        if (socialAuthSource == SocialAuthSource.FACEBOOK) {
            socialProvider = new FacebookProvider();
        } else if (socialAuthSource == SocialAuthSource.GOOGLE) {
            socialProvider = new GoogleProvider((GoogleOauth2) google);
        } else if (socialAuthSource == SocialAuthSource.MICROSOFT) {
            socialProvider = new MicrosoftProvide();
        }

        if (socialProvider != null) {
            socialProvider.getUserInfo(activity, accessToken, userInfoCallback);
        }

    }

    public ISocial getInstance(SocialAuthSource source, Config config) {
        if (ConfigUtil.isSocialFeatureEnabled(source, config)) {
            return switch (source) {
                case GOOGLE -> new GoogleOauth2(activity);
                case FACEBOOK -> new FacebookAuth(activity);
                case MICROSOFT -> new MicrosoftAuth(activity);
                case UNKNOWN -> new ISocialEmptyImpl();
            };
        }
        return new ISocialEmptyImpl();
    }

    class ProfileTask extends Task<AuthResponse> {

        private String accessToken;
        private String backend;

        LoginAPI loginAPI;

        public ProfileTask(Context context, String accessToken, String backend) {
            super(context);
            this.accessToken = accessToken;
            this.backend = backend;
            loginAPI = EntryPointAccessors
                    .fromApplication(context, EdxDefaultModule.ProviderEntryPoint.class)
                    .getLoginAPI();
        }

        @Override
        public void onException(Exception ex) {
            callback.onUserLoginFailure(ex, this.accessToken, this.backend);
        }

        @Override
        protected void onPostExecute(AuthResponse result) {
            super.onPostExecute(result);
            if (result != null) {
                if (feature == Feature.REGISTRATION) {
                    environment.getLoginPrefs().setAlreadyRegisteredLoggedIn(true);
                }
                callback.onUserLoginSuccess();
            }
        }

        @Override
        protected AuthResponse doInBackground(Void... voids) {
            final AuthResponse auth;
            try {
                if (backend.equalsIgnoreCase(LoginPrefs.BACKEND_FACEBOOK)) {
                    try {
                        auth = loginAPI.logInUsingFacebook(accessToken);
                    } catch (LoginAPI.AccountNotLinkedException e) {
                        throw new LoginException(makeLoginErrorMessage(e));
                    }
                } else if (backend.equalsIgnoreCase(LoginPrefs.BACKEND_GOOGLE)) {
                    try {
                        auth = loginAPI.logInUsingGoogle(accessToken);
                    } catch (LoginAPI.AccountNotLinkedException e) {
                        throw new LoginException(makeLoginErrorMessage(e));
                    }
                } else if (backend.equalsIgnoreCase(LoginPrefs.BACKEND_MICROSOFT)) {
                    try {
                        auth = loginAPI.logInUsingMicrosoft(accessToken);
                    } catch (LoginAPI.AccountNotLinkedException e) {
                        throw new LoginException(makeLoginErrorMessage(e));
                    }
                } else {
                    throw new IllegalArgumentException("Unknown backend: " + backend);
                }
                return auth;
            } catch (Exception ex) {
                handleException(ex);
                return null;
            }
        }

        public LoginErrorMessage makeLoginErrorMessage(@NonNull LoginAPI.AccountNotLinkedException e) throws LoginException {
            final boolean isFacebook = backend.equalsIgnoreCase(LoginPrefs.BACKEND_FACEBOOK);
            final boolean isMicrosoft = backend.equalsIgnoreCase(LoginPrefs.BACKEND_MICROSOFT);
            if (feature == Feature.SIGN_IN && e.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                final String title = activity.getResources().getString(R.string.login_error);
                final CharSequence desc = ResourceUtil.getFormattedString(context.get().getResources(),
                        isFacebook ? R.string.error_account_not_linked_desc_fb_2 :
                                isMicrosoft ? R.string.error_account_not_linked_desc_microsoft_2 :
                                        R.string.error_account_not_linked_desc_google_2,
                        "platform_name", environment.getConfig().getPlatformName());
                throw new LoginException(new LoginErrorMessage(title, desc.toString()));
            }
            final CharSequence title = ResourceUtil.getFormattedString(context.get().getResources(),
                    isFacebook ? R.string.error_account_not_linked_title_fb :
                            isMicrosoft ? R.string.error_account_not_linked_title_microsoft :
                                    R.string.error_account_not_linked_title_google,
                    "platform_name", environment.getConfig().getPlatformName());
            final HashMap<String, CharSequence> descParamsDesc = new HashMap<>();
            descParamsDesc.put("platform_name", environment.getConfig().getPlatformName());
            descParamsDesc.put("platform_destination", environment.getConfig().getPlatformDestinationName());
            final CharSequence desc = ResourceUtil.getFormattedString(context.get().getResources(),
                    isFacebook ? R.string.error_account_not_linked_desc_fb :
                            isMicrosoft ? R.string.error_account_not_linked_desc_microsoft : R.string.error_account_not_linked_desc_google,
                    descParamsDesc);
            return new LoginErrorMessage(title.toString(), desc.toString());
        }
    }

    public SocialButtonClickHandler createSocialButtonClickHandler(SocialAuthSource socialAuthSource) {
        return new SocialButtonClickHandler(socialAuthSource);
    }

    public class SocialButtonClickHandler implements View.OnClickListener {
        private final SocialAuthSource socialAuthSource;

        private SocialButtonClickHandler(SocialAuthSource socialAuthSource) {
            this.socialAuthSource = socialAuthSource;
        }

        @Override
        public void onClick(View v) {
            if (!NetworkUtil.isConnected(activity)) {
                callback.showAlertDialog(activity.getString(R.string.no_connectivity),
                        activity.getString(R.string.network_not_connected));
            } else {
                @SuppressLint("StaticFieldLeak")
                Task<Void> logout = new Task<Void>(activity) {

                    @Override
                    protected Void doInBackground(Void... voids) {
                        socialLogout(socialAuthSource);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void unused) {
                        super.onPostExecute(unused);
                        try {
                            socialLogin(socialAuthSource);
                        } catch (Exception ex) {
                            handleException(ex);
                        }
                    }

                    @Override
                    public void onException(Exception ex) {
                        if (activity instanceof ICommonUI)
                            ((ICommonUI) activity).tryToSetUIInteraction(true);
                    }
                };
                if (activity instanceof ICommonUI)
                    ((ICommonUI) activity).tryToSetUIInteraction(false);
                logout.execute();
            }
        }
    }


    public interface MobileLoginCallback {
        void onSocialLoginSuccess(String accessToken, String backend, Task task);

        void onUserLoginFailure(Exception ex, String accessToken, String backend);

        void onUserLoginSuccess();

        void showAlertDialog(String header, String message);
    }

    public interface SocialUserInfoCallback {
        void setSocialUserInfo(String email, String name);
    }

}
