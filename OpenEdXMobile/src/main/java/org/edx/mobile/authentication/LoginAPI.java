package org.edx.mobile.authentication;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.exception.AuthException;
import org.edx.mobile.http.HttpResponseStatusException;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.constants.ApiConstants;
import org.edx.mobile.model.api.FormFieldMessageBody;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.module.notification.NotificationDelegate;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.tta.data.model.authentication.FieldInfo;
import org.edx.mobile.tta.data.model.authentication.MobileNumberVerificationResponse;
import org.edx.mobile.tta.data.model.authentication.RegisterResponse;
import org.edx.mobile.tta.data.model.authentication.RegistrationError;
import org.edx.mobile.tta.data.model.authentication.ResetForgotedPasswordResponse;
import org.edx.mobile.tta.data.model.authentication.SendOTPResponse;
import org.edx.mobile.tta.data.model.authentication.VerifyOTPForgotedPasswordResponse;
import org.edx.mobile.tta.data.model.authentication.VerifyOTPResponse;
import org.edx.mobile.tta.data.model.profile.UpdateMyProfileResponse;
import org.edx.mobile.tta.data.model.profile.UserAddressResponse;
import org.edx.mobile.tta.firebase.FirebaseUpdateTokenResponse;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.observer.BasicObservable;
import org.edx.mobile.util.observer.Observable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Response;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static org.edx.mobile.http.util.CallUtil.executeStrict;

@Singleton
public class LoginAPI {

    @NonNull
    private final LoginService loginService;

    @NonNull
    private final Config config;

    @NonNull
    private final LoginPrefs loginPrefs;

    @NonNull
    private final AnalyticsRegistry analyticsRegistry;

    @NonNull
    private final NotificationDelegate notificationDelegate;

    @NonNull
    private final BasicObservable<LogInEvent> logInEvents = new BasicObservable<>();

    @NonNull
    private final Gson gson;

    @Inject
    public LoginAPI(@NonNull LoginService loginService,
                    @NonNull Config config,
                    @NonNull LoginPrefs loginPrefs,
                    @NonNull AnalyticsRegistry analyticsRegistry,
                    @NonNull NotificationDelegate notificationDelegate,
                    @NonNull Gson gson) {
        this.loginService = loginService;
        this.config = config;
        this.loginPrefs = loginPrefs;
        this.analyticsRegistry = analyticsRegistry;
        this.notificationDelegate = notificationDelegate;
        this.gson = gson;
    }

    @NonNull
    public Response<AuthResponse> getAccessToken(@NonNull String username,
                                       @NonNull String password) throws IOException {
        String grantType = "password";
        String clientID = config.getOAuthClientId();
        return loginService.getAccessToken(grantType, clientID, username, password).execute();
    }

    @NonNull
    public AuthResponse logInUsingMobileNumber(String loginMobileNumber,String password ) throws Exception {
        final Response<AuthResponse> response = getAccessToken(loginMobileNumber, password);
        if (!response.isSuccessful()) {
            throw new AuthException(response.message());
        }
        final AuthResponse data = response.body();
        if (!data.isSuccess()) {
            throw new AuthException(data.error);
        }
        finishLogIn(data, LoginPrefs.AuthBackend.PASSWORD, loginMobileNumber.trim());
        return data;
    }

    @NonNull
    public AuthResponse logInUsingEmail(@NonNull String email, @NonNull String password) throws Exception {
        final Response<AuthResponse> response = getAccessToken(email, password);
        if (!response.isSuccessful()) {
            throw new HttpStatusException(response);
        }
        finishLogIn(response.body(), LoginPrefs.AuthBackend.PASSWORD, email.trim());
        return response.body();
    }

    @NonNull
    public AuthResponse logInUsingFacebook(String accessToken) throws Exception {
        return finishSocialLogIn(accessToken, LoginPrefs.AuthBackend.FACEBOOK);
    }

    @NonNull
    public AuthResponse logInUsingGoogle(String accessToken) throws Exception {
        return finishSocialLogIn(accessToken, LoginPrefs.AuthBackend.GOOGLE);
    }

    @NonNull
    private AuthResponse finishSocialLogIn(@NonNull String accessToken, @NonNull LoginPrefs.AuthBackend authBackend) throws Exception {
        final String backend = ApiConstants.getOAuthGroupIdForAuthBackend(authBackend);
        final Response<AuthResponse> response = loginService.exchangeAccessToken(accessToken, config.getOAuthClientId(), backend).execute();
        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED || response.code() == HTTP_BAD_REQUEST) {
            // TODO: Introduce a more explicit error code to indicate that an account is not linked.
            throw new AccountNotLinkedException(response.code());
        }
        if (!response.isSuccessful()) {
            throw new HttpStatusException(response);
        }
        final AuthResponse data = response.body();
        if (data.error != null && data.error.equals(Integer.toString(HttpURLConnection.HTTP_UNAUTHORIZED))) {
            throw new AccountNotLinkedException(HttpURLConnection.HTTP_UNAUTHORIZED);
        }
        finishLogIn(data, authBackend, "");
        return data;
    }

    private void finishLogIn(@NonNull AuthResponse response, @NonNull LoginPrefs.AuthBackend authBackend, @NonNull String usernameUsedToLogIn) throws Exception {
        loginPrefs.storeAuthTokenResponse(response, authBackend);
        try {
            response.profile = getProfile();
        } catch (Throwable e) {
            // The app doesn't properly handle the scenario that we are logged in but we don't have
            // a cached profile. So if we fail to fetch the profile, let's erase the stored token.
            // TODO: A better approach might be to fetch the profile *before* storing the token.
            loginPrefs.clearAuthTokenResponse();
            throw e;
        }
        loginPrefs.setLastAuthenticatedEmail(usernameUsedToLogIn);
        analyticsRegistry.identifyUser(
                response.profile.id.toString(),
                response.profile.email,
                usernameUsedToLogIn);
        final String backendKey = loginPrefs.getAuthBackendKeyForSegment();
        if (backendKey != null) {
            analyticsRegistry.trackUserLogin(backendKey);
        }
        notificationDelegate.resubscribeAll();
        logInEvents.sendData(new LogInEvent());
    }

    public void logOut() {
        final AuthResponse currentAuth = loginPrefs.getCurrentAuth();
        if (currentAuth != null && currentAuth.refresh_token != null) {
            loginService.revokeAccessToken(config.getOAuthClientId(),
                    currentAuth.refresh_token, ApiConstants.TOKEN_TYPE_REFRESH);
        }
    }

    @NonNull
    public AuthResponse registerUsingEmail(@NonNull Bundle parameters) throws Exception {
        register(parameters);
        return logInUsingEmail(parameters.getString("username"), parameters.getString("password"));
    }

    @NonNull
    public AuthResponse registerUsingGoogle(@NonNull Bundle parameters, @NonNull String accessToken) throws Exception {
        register(parameters);
        return logInUsingGoogle(accessToken);
    }

    @NonNull
    public AuthResponse registerUsingFacebook(@NonNull Bundle parameters, @NonNull String accessToken) throws Exception {
        register(parameters);
        return logInUsingFacebook(accessToken);
    }

    @NonNull
    public Observable<LogInEvent> getLogInEvents() {
        return logInEvents;
    }

    /*@NonNull
    private void register(Bundle parameters) throws Exception {
        final Map<String, String> parameterMap = new HashMap<>();
        for (String key : parameters.keySet()) {
            parameterMap.put(key, parameters.getString(key));
        }
        Response<ResponseBody> response = loginService.register(parameterMap).execute();
        if (!response.isSuccessful()) {
            final int errorCode = response.code();
            final String errorBody = response.errorBody().string();
            if ((errorCode == HttpStatus.BAD_REQUEST || errorCode == HttpStatus.CONFLICT) && !android.text.TextUtils.isEmpty(errorBody)) {
                try {
                    final FormFieldMessageBody body = gson.fromJson(errorBody, FormFieldMessageBody.class);
                    if (body != null && body.size() > 0) {
                        throw new RegistrationException(body);
                    }
                } catch (JsonSyntaxException ex) {
                    // Looks like the response does not contain form validation errors.
                }
            }
            throw new HttpStatusException(response);
        }
    }*/

    @NonNull
    private RegisterResponse register(Bundle parameters) throws Exception {
        RegisterResponse mx_registerMeResponse=new RegisterResponse();

        final Map<String, String> parameterMap = new HashMap<>();
        for (String key : parameters.keySet()) {
            parameterMap.put(key, parameters.getString(key));
        }
        Response<ResponseBody> response = loginService.register(parameterMap).execute();
        if (!response.isSuccessful()) {
            final int errorCode = response.code();
            final String errorBody = response.errorBody().string();
            if ((errorCode == HttpStatus.BAD_REQUEST || errorCode == HttpStatus.CONFLICT) && !android.text.TextUtils.isEmpty(errorBody)) {
                try {
                    final FormFieldMessageBody body = gson.fromJson(errorBody, FormFieldMessageBody.class);
                    if (body != null && body.size() > 0) {
                        throw new RegistrationException(body);
                    }
                } catch (JsonSyntaxException ex) {
                    // Looks like the response does not contain form validation errors.
                }
            }
            throw new HttpResponseStatusException(errorCode);
        }
        else
            mx_registerMeResponse.setSuccess(false);

        return mx_registerMeResponse;
    }

    @NonNull
    public ProfileModel getProfile() throws Exception {
        ProfileModel data = executeStrict(loginService.getProfile());
        loginPrefs.storeUserProfile(data);
        return data;
    }

    public static class AccountNotLinkedException extends Exception {
        /** HTTP status code. */
        private int responseCode;

        public AccountNotLinkedException(int responseCode) {
            this.responseCode = responseCode;
        }

        /**
         * @return HTTP status code.
         */
        public int getResponseCode() {
            return responseCode;
        }
    }

    public static class RegistrationException extends Exception {
        @NonNull
        private final FormFieldMessageBody formErrorBody;

        public RegistrationException(@NonNull FormFieldMessageBody formErrorBody) {
            this.formErrorBody = formErrorBody;
        }

        @NonNull
        public FormFieldMessageBody getFormErrorBody() {
            return formErrorBody;
        }
    }

    //TTA

    public static class RegistrationFieldErrorException extends Exception {
        @NonNull
        private final RegistrationError formErrorBody;

        public RegistrationFieldErrorException(@NonNull RegistrationError formErrorBody) {
            this.formErrorBody = formErrorBody;
        }

        @NonNull
        public RegistrationError getFormErrorBody() {
            return formErrorBody;
        }
    }

    @NonNull
    public MobileNumberVerificationResponse mobileNumberVerification(@NonNull Bundle parameters) throws Exception {
        return mxMobileNumberVerification(parameters);
    }

    @NonNull
    public VerifyOTPForgotedPasswordResponse OTPVerification_For_ForgotedPassword(@NonNull Bundle parameters) throws Exception {
        return mxOTPVerification_For_ForgotedPassword(parameters);
    }

    @NonNull
    public ResetForgotedPasswordResponse ResetForgotedPassword(@NonNull Bundle parameters) throws Exception {
        return mxResetForgotedPassword(parameters);
    }

    @NonNull
    public SendOTPResponse generateOTP(@NonNull Bundle parameters) throws Exception {
        return mxGrenerateOtp(parameters);
    }

    @NonNull
    public RegisterResponse registerUsingMobileNumber(@NonNull Bundle parameters) throws Exception {
        return register(parameters);
    }

    @NonNull
    public VerifyOTPResponse verifyOTP(@NonNull Bundle parameters) throws Exception {
        return mxVerifyOtp(parameters);
    }

    @NonNull
    public UserAddressResponse getUserAddress(@NonNull Bundle parameters) throws Exception {
        return mxGetUserAddress(parameters);
    }

    @NonNull
    public UpdateMyProfileResponse updateMyProfile(@NonNull Bundle parameters, @NonNull String username) throws Exception {
        return mxUpdateMyProfile(parameters,username);
    }

    @NonNull
    public FirebaseUpdateTokenResponse updateFireBaseTokenToServer(Bundle parameters) throws Exception {
        return  Mx_updateTA_FirebaseToken(parameters);
    }

    @NonNull
    public FieldInfo  getCustomStateFieldAttributes() throws Exception {
        return mxGetCustomStateFieldAttributes();
    }

    @NonNull
    private MobileNumberVerificationResponse mxMobileNumberVerification(Bundle parameters) throws Exception
    {
        final Map<String, String> parameterMap = new HashMap<>();
        for (String key : parameters.keySet()) {
            parameterMap.put(key, parameters.getString(key));
        }

        Response<MobileNumberVerificationResponse> res = loginService.mxMobileNumberVerification(parameterMap).execute();

        if (!res.isSuccessful()) {
            final int errorCode = res.code();
            final String errorBody = res.errorBody().string();
            if ((errorCode == HttpStatus.BAD_REQUEST || errorCode == HttpStatus.CONFLICT) && !android.text.TextUtils.isEmpty(errorBody)) {
                try {
                    final FormFieldMessageBody body = gson.fromJson(errorBody, FormFieldMessageBody.class);
                    if (body != null && body.size() > 0) {
                        throw new RegistrationException(body);
                    }
                } catch (JsonSyntaxException ex) {
                    // Looks like the response does not contain form validation errors.
                }
            }
            throw new HttpResponseStatusException(errorCode);
        }

        return res.body();
    }

    @NonNull
    private VerifyOTPForgotedPasswordResponse mxOTPVerification_For_ForgotedPassword(Bundle parameters) throws Exception
    {
        final Map<String, String> parameterMap = new HashMap<>();
        for (String key : parameters.keySet()) {
            parameterMap.put(key, parameters.getString(key));
        }

        Response<VerifyOTPForgotedPasswordResponse> res = loginService.mxOTPVerification_For_ForgotedPassword(parameterMap).execute();

        if (!res.isSuccessful()) {
            final int errorCode = res.code();
            final String errorBody = res.errorBody().string();
            if ((errorCode == HttpStatus.BAD_REQUEST || errorCode == HttpStatus.CONFLICT) && !android.text.TextUtils.isEmpty(errorBody)) {
                try {
                    final FormFieldMessageBody body = gson.fromJson(errorBody, FormFieldMessageBody.class);
                    if (body != null && body.size() > 0) {
                        throw new RegistrationException(body);
                    }
                } catch (JsonSyntaxException ex) {
                    // Looks like the response does not contain form validation errors.
                }
            }
            throw new HttpResponseStatusException(errorCode);
        }

        return res.body();
    }

    @NonNull
    private ResetForgotedPasswordResponse mxResetForgotedPassword(Bundle parameters) throws Exception
    {
        final Map<String, String> parameterMap = new HashMap<>();
        for (String key : parameters.keySet()) {
            parameterMap.put(key, parameters.getString(key));
        }

        Response<ResetForgotedPasswordResponse> res = loginService.mxResetForgotedPassword(parameterMap).execute();

        if (!res.isSuccessful()) {
            final int errorCode = res.code();
            final String errorBody = res.errorBody().string();
            if ((errorCode == HttpStatus.BAD_REQUEST || errorCode == HttpStatus.CONFLICT) && !android.text.TextUtils.isEmpty(errorBody)) {
                try {
                    final FormFieldMessageBody body = gson.fromJson(errorBody, FormFieldMessageBody.class);
                    if (body != null && body.size() > 0) {
                        throw new RegistrationException(body);
                    }
                } catch (JsonSyntaxException ex) {
                    // Looks like the response does not contain form validation errors.
                }
            }
            throw new HttpResponseStatusException(errorCode);
        }

        return res.body();
    }

    @NonNull
    private SendOTPResponse mxGrenerateOtp(Bundle parameters) throws Exception
    {
        final Map<String, String> parameterMap = new HashMap<>();
        for (String key : parameters.keySet()) {
            parameterMap.put(key, parameters.getString(key));
        }
        Response<SendOTPResponse> res = loginService.mxGenerateOTP(parameterMap).execute();

        if (!res.isSuccessful()) {
            final int errorCode = res.code();
            final String errorBody = res.errorBody().string();
            if ((errorCode == HttpStatus.BAD_REQUEST || errorCode == HttpStatus.CONFLICT) && !android.text.TextUtils.isEmpty(errorBody)) {
                try {
                    final FormFieldMessageBody body = gson.fromJson(errorBody, FormFieldMessageBody.class);
                    if (body != null && body.size() > 0) {
                        throw new RegistrationException(body);
                    }
                } catch (JsonSyntaxException ex) {
                    // Looks like the response does not contain form validation errors.
                }
            }
            throw new HttpResponseStatusException(errorCode);
        }
        return res.body();
    }

    @NonNull
    private VerifyOTPResponse mxVerifyOtp(Bundle parameters) throws Exception
    {
        final Map<String, String> parameterMap = new HashMap<>();
        for (String key : parameters.keySet()) {
            parameterMap.put(key, parameters.getString(key));
        }
        Response<VerifyOTPResponse> res = loginService.mxVerifyOTP(parameterMap).execute();

        if (!res.isSuccessful()) {
            final int errorCode = res.code();
            final String errorBody = res.errorBody().string();
            if ((errorCode == HttpStatus.BAD_REQUEST || errorCode == HttpStatus.CONFLICT) && !android.text.TextUtils.isEmpty(errorBody)) {
                try {
                    final FormFieldMessageBody body = gson.fromJson(errorBody, FormFieldMessageBody.class);
                    if (body != null && body.size() > 0) {
                        throw new RegistrationException(body);
                    }
                } catch (JsonSyntaxException ex) {
                    // Looks like the response does not contain form validation errors.
                }
            }
            throw new HttpResponseStatusException(errorCode);
        }
        return res.body();
    }

    private UserAddressResponse mxGetUserAddress (Bundle parameters) throws Exception {
        final Map<String, String> parameterMap = new HashMap<>();
        for (String key : parameters.keySet()) {
            parameterMap.put(key, parameters.getString(key));
        }

        Response<UserAddressResponse> res = loginService.mxGetUserAddress(parameterMap).execute();

        if (!res.isSuccessful()) {
            final int errorCode = res.code();
            final String errorBody = res.errorBody().string();
            if ((errorCode == HttpStatus.BAD_REQUEST || errorCode == HttpStatus.CONFLICT) && !android.text.TextUtils.isEmpty(errorBody)) {
                try {
                    final FormFieldMessageBody body = gson.fromJson(errorBody, FormFieldMessageBody.class);
                    if (body != null && body.size() > 0) {
                        throw new RegistrationException(body);
                    }
                } catch (JsonSyntaxException ex) {
                    // Looks like the response does not contain form validation errors.
                }
            }
            throw new HttpResponseStatusException(errorCode);
        }
        return res.body();
    }

    @NonNull
    private UpdateMyProfileResponse mxUpdateMyProfile(Bundle parameters,String username) throws Exception
    {
        final Map<String, String> parameterMap = new HashMap<>();
        for (String key : parameters.keySet()) {
            parameterMap.put(key, parameters.getString(key));
        }
        Response<UpdateMyProfileResponse> res = loginService.mxUpdateProfile(parameterMap).execute();

        if (!res.isSuccessful()) {
            final int errorCode = res.code();
            final String errorBody = res.errorBody().string();
            if ((errorCode == HttpStatus.BAD_REQUEST || errorCode == HttpStatus.CONFLICT) && !android.text.TextUtils.isEmpty(errorBody)) {
                try {
                    final FormFieldMessageBody body = gson.fromJson(errorBody, FormFieldMessageBody.class);
                    if (body != null && body.size() > 0) {
                        throw new RegistrationException(body);
                    }
                } catch (JsonSyntaxException ex) {
                    // Looks like the response does not contain form validation errors.
                }
            }
            else  if(res.code()==HttpStatus.NOT_ACCEPTABLE)
            {
                try {
                    final RegistrationError body = gson.fromJson(errorBody, RegistrationError.class);
                    if (body != null) {
                        throw new RegistrationFieldErrorException(body);
                    }
                } catch (JsonSyntaxException ex) {
                    // Looks like the response does not contain form validation errors.
                }
            }

            throw new HttpResponseStatusException(errorCode);
        }
        return res.body();
    }

    //set shaired pref for connect::login user
    @NonNull
    public void setConnectCookiesAndTimeStamp(@NonNull String cookie,  @NonNull String timeStamp)
    {
        loginPrefs.storeMxConnectCookie(cookie);
        loginPrefs.storeMxConnectCookieTimeStamp(timeStamp);
    }


    public String getConnectCookies() {
        return loginPrefs.getMxConnectCookie();
    }
    public String getConnectCookiesTimeStamp() {
        return loginPrefs.getMxConnectCookieTimeStamp();
    }

    //Firebase token update & restore
    @NonNull
    private FirebaseUpdateTokenResponse Mx_updateTA_FirebaseToken(Bundle parameters) throws Exception {

        final Map<String, String> parameterMap = new HashMap<>();
        for (String key : parameters.keySet()) {
            parameterMap.put(key, parameters.getString(key));
        }

        Response<FirebaseUpdateTokenResponse> response = loginService.updateFirebaseToken(parameterMap).execute();

        if (!response.isSuccessful()) {
            final int errorCode = response.code();
            final String errorBody = response.errorBody().string();
            if ((errorCode == HttpStatus.BAD_REQUEST || errorCode == HttpStatus.CONFLICT) && !android.text.TextUtils.isEmpty(errorBody)) {
                try {
                    final FormFieldMessageBody body = gson.fromJson(errorBody, FormFieldMessageBody.class);
                    if (body != null && body.size() > 0) {
                        throw new LoginAPI.RegistrationException(body);
                    }
                } catch (JsonSyntaxException ex) {
                    // Looks like the response does not contain form validation errors.
                }
            }
            throw new HttpResponseStatusException(errorCode);
        }
        return response.body();
    }

    @NonNull
    private FieldInfo  mxGetCustomStateFieldAttributes() throws Exception
    {

        Response<FieldInfo> res = loginService.mxGetCustomStateFieldAttributes().execute();

        if (!res.isSuccessful()) {
            final int errorCode = res.code();
            final String errorBody = res.errorBody().string();
            if ((errorCode == HttpStatus.BAD_REQUEST || errorCode == HttpStatus.CONFLICT) && !android.text.TextUtils.isEmpty(errorBody)) {
                try {
                    final FormFieldMessageBody body = gson.fromJson(errorBody, FormFieldMessageBody.class);
                    if (body != null && body.size() > 0) {
                        throw new RegistrationException(body);
                    }
                } catch (JsonSyntaxException ex) {
                    // Looks like the response does not contain form validation errors.
                }
            }
            throw new HttpResponseStatusException(errorCode);
        }
        return res.body();
    }
}
